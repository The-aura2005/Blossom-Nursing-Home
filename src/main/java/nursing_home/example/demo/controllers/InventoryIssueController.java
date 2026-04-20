package nursing_home.example.demo.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.inventory.model.InventoryItem;
import nursing_home.example.demo.inventory.service.InventoryService;
import nursing_home.example.demo.model.services.AssignedTaskService;

@Controller
public class InventoryIssueController {

    private final AssignedTaskService assignedTaskService;
    private final InventoryService inventoryService;

    public InventoryIssueController(AssignedTaskService assignedTaskService, InventoryService inventoryService) {
        this.assignedTaskService = assignedTaskService;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory/issue/new/{residentId}")
    @PreAuthorize("hasRole('STAFF')")
    public String showIssueForm(@PathVariable Long residentId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (!assignedTaskService.isResidentAssignedToStaff(authentication.getName(), residentId)) {
            redirectAttributes.addFlashAttribute("inventoryError",
                    "You can only issue items for residents assigned to you.");
            return "redirect:/MyAssignedResidents";
        }

        List<InventoryItem> availableItems = inventoryService.getAllItems().stream()
                .filter(item -> item.getQuantity() > 0)
                .toList();

        model.addAttribute("selectedResident",
                assignedTaskService.getAssignedResidentForStaff(authentication.getName(), residentId).orElse(null));
        model.addAttribute("availableItems", availableItems);
        return "inventory_issue_form";
    }

    @PostMapping("/inventory/issue/save")
    @PreAuthorize("hasRole('STAFF')")
    public String saveIssue(
            @RequestParam Long residentId,
            @RequestParam Long itemId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String notes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (!assignedTaskService.isResidentAssignedToStaff(authentication.getName(), residentId)) {
            redirectAttributes.addFlashAttribute("inventoryError",
                    "You can only issue items for residents assigned to you.");
            return "redirect:/MyAssignedResidents";
        }

        try {
            inventoryService.issueItemToResident(itemId, residentId, quantity, authentication.getName(), notes);
            redirectAttributes.addFlashAttribute("inventoryMessage", "Inventory item issued and stock updated.");
            return "redirect:/medications";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("inventoryError", ex.getMessage());
            return "redirect:/inventory/issue/new/" + residentId;
        }
    }
}
