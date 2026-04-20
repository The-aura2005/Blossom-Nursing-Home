package nursing_home.example.demo.inventory.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nursing_home.example.demo.inventory.model.InventoryIssueRequest;
import nursing_home.example.demo.inventory.model.InventoryItem;
import nursing_home.example.demo.inventory.service.InventoryService;

@RestController
@RequestMapping("/api/items")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @PreAuthorize("hasRole('INVENTORY_MANAGER')")
    public List<InventoryItem> getAllItems() {
        return inventoryService.getAllItems();
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('STAFF')")
    public List<InventoryItem> getAvailableItemsForIssuance() {
        return inventoryService.getAllItems().stream()
                .filter(item -> item.getQuantity() > 0)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<InventoryItem> addItem(@RequestBody InventoryItem item) {
        return ResponseEntity.ok(inventoryService.addItem(item));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<InventoryItem> updateItem(@PathVariable Long id, @RequestBody InventoryItem item) {
        return ResponseEntity.ok(inventoryService.updateItem(id, item));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INVENTORY_MANAGER')")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        inventoryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('INVENTORY_MANAGER')")
    public List<InventoryItem> getLowStockItems() {
        return inventoryService.getLowStockItems();
    }

    @GetMapping("/out-of-stock")
    @PreAuthorize("hasRole('INVENTORY_MANAGER')")
    public List<InventoryItem> getOutOfStockItems() {
        return inventoryService.getOutOfStockItems();
    }

    @PostMapping("/issue")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<InventoryItem> issueItem(@RequestBody InventoryIssueRequest request,
            Authentication authentication) {
        InventoryItem updated = inventoryService.issueItemToResident(
                request.getItemId(),
                request.getResidentId(),
                request.getQuantity(),
                authentication.getName(),
                request.getNotes());
        return ResponseEntity.ok(updated);
    }
}
