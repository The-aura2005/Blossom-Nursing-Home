package nursing_home.example.demo.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.inventory.model.InventoryItem;
import nursing_home.example.demo.inventory.model.Supplier;
import nursing_home.example.demo.inventory.repository.InventoryRepository;
import nursing_home.example.demo.inventory.repository.SupplierRepository;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final ActivityLogService activityLogService;

    public InventoryService(InventoryRepository inventoryRepository,
            SupplierRepository supplierRepository,
            ActivityLogService activityLogService) {
        this.inventoryRepository = inventoryRepository;
        this.supplierRepository = supplierRepository;
        this.activityLogService = activityLogService;
    }

    public InventoryItem addItem(InventoryItem item) {
        item.setSupplier(resolveSupplier(item.getSupplier()));
        InventoryItem saved = inventoryRepository.save(item);
        activityLogService.logAction("ADD", saved.getName());
        return saved;
    }

    public InventoryItem updateItem(Long id, InventoryItem updatedItem) {
        InventoryItem existing = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));

        existing.setName(updatedItem.getName());
        existing.setCategory(updatedItem.getCategory());
        existing.setQuantity(updatedItem.getQuantity());
        existing.setUnit(updatedItem.getUnit());
        existing.setMinThreshold(updatedItem.getMinThreshold());
        existing.setSupplier(resolveSupplier(updatedItem.getSupplier()));

        InventoryItem saved = inventoryRepository.save(existing);
        activityLogService.logAction("UPDATE", saved.getName());
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

    private Supplier resolveSupplier(Supplier incomingSupplier) {
        if (incomingSupplier == null || incomingSupplier.getId() == null) {
            return null;
        }
        return supplierRepository.findById(incomingSupplier.getId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    }
}
