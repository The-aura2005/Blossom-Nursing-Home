package nursing_home.example.demo.inventory.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InventoryManagerPageController {

    @GetMapping("/inventory-manager")
    @PreAuthorize("hasRole('INVENTORY_MANAGER')")
    public String inventoryManagerDashboard() {
        return "inventory-manager";
    }
}
