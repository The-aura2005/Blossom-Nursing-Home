package nursing_home.example.demo.inventory.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nursing_home.example.demo.dao.ResidentRepository;
import nursing_home.example.demo.inventory.model.InventoryItem;
import nursing_home.example.demo.inventory.model.Supplier;
import nursing_home.example.demo.inventory.repository.InventoryRepository;
import nursing_home.example.demo.inventory.repository.SupplierRepository;
import nursing_home.example.demo.model.Resident;
import nursing_home.example.demo.model.services.SupplierExpenseService;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final ResidentRepository residentRepository;
    private final ActivityLogService activityLogService;
    private final SupplierExpenseService supplierExpenseService;

    public InventoryService(InventoryRepository inventoryRepository,
            SupplierRepository supplierRepository,
            ResidentRepository residentRepository,
            ActivityLogService activityLogService,
            SupplierExpenseService supplierExpenseService) {
        this.inventoryRepository = inventoryRepository;
        this.supplierRepository = supplierRepository;
        this.residentRepository = residentRepository;
        this.activityLogService = activityLogService;
        this.supplierExpenseService = supplierExpenseService;
    }

    public InventoryItem addItem(InventoryItem item) {
        item.setSupplier(resolveSupplier(item.getSupplier()));
        InventoryItem saved = inventoryRepository.save(item);
        activityLogService.logAction("ADD", saved.getName());
        recordSupplierExpense(saved, saved.getQuantity());
        return saved;
    }

    public InventoryItem updateItem(Long id, InventoryItem updatedItem) {
        InventoryItem existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));

        int previousQuantity = existing.getQuantity();

        existing.setName(updatedItem.getName());
        existing.setCategory(updatedItem.getCategory());
        existing.setQuantity(updatedItem.getQuantity());
        existing.setUnit(updatedItem.getUnit());
        existing.setMinThreshold(updatedItem.getMinThreshold());
        existing.setPurchaseCost(updatedItem.getPurchaseCost());
        existing.setSupplier(resolveSupplier(updatedItem.getSupplier()));

        InventoryItem saved = inventoryRepository.save(existing);
        activityLogService.logAction("UPDATE", saved.getName());
        int quantityAdded = saved.getQuantity() - previousQuantity;
        if (quantityAdded > 0) {
            recordSupplierExpense(saved, quantityAdded);
        }
        return saved;
    }

    public void deleteItem(Long id) {
        InventoryItem existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
        String itemName = existing.getName();
        inventoryRepository.delete(existing);
        activityLogService.logAction("DELETE", itemName);
    }

    public List<InventoryItem> getAllItems() {
        return inventoryRepository.findAll();
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryRepository.findLowStockItems();
    }

    public List<InventoryItem> getOutOfStockItems() {
        return inventoryRepository.findByQuantity(0);
    }

    @Transactional
    public InventoryItem issueItemToResident(Long itemId, Long residentId, Integer quantity, String staffUsername,
            String notes) {
        if (itemId == null) {
            throw new IllegalArgumentException("Item is required");
        }
        if (residentId == null) {
            throw new IllegalArgumentException("Resident is required");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        InventoryItem item = inventoryRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));

        if (item.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for item: " + item.getName());
        }

        item.setQuantity(item.getQuantity() - quantity);
        InventoryItem saved = inventoryRepository.save(item);

        activityLogService.logIssue(
                saved.getName(),
                quantity,
                staffUsername,
                resident.getId(),
                resident.getName(),
                notes);

        return saved;
    }

    private Supplier resolveSupplier(Supplier incomingSupplier) {
        if (incomingSupplier == null || incomingSupplier.getId() == null) {
            return null;
        }
        return supplierRepository.findById(incomingSupplier.getId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    }

    private void recordSupplierExpense(InventoryItem item, int quantityAdded) {
        if (item.getSupplier() == null || item.getPurchaseCost() == null || quantityAdded <= 0) {
            return;
        }

        BigDecimal totalCost = item.getPurchaseCost();
        if (totalCost.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        supplierExpenseService.saveExpense(
                item.getSupplier().getName(),
                item.getName(),
                totalCost,
                LocalDate.now());
    }
}
