package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.model.services.AssignedTaskService;
import nursing_home.example.demo.model.services.VitalsService;

import java.util.List;

@Controller
public class VitalController {
    private final VitalsService vitalsService;
    private final AssignedTaskService assignedTaskService;

    public VitalController(VitalsService vitalsService, AssignedTaskService assignedTaskService) {
        this.vitalsService = vitalsService;
        this.assignedTaskService = assignedTaskService;
    }

    @GetMapping("/VitalLogging")
    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    public String vitalLogging(
            @RequestParam(value = "residentId", required = false) Long residentId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");

        List<AssignedTaskService.ResidentTaskSummary> assignedResidents = assignedTaskService
                .getAssignedResidentsForStaff(username);

        if (residentId == null) {
            redirectAttributes.addFlashAttribute("vitalsMessage",
                    "Please click Log Vitals from a resident card to add vitals for that resident.");
            return "redirect:/MyAssignedResidents";
        }

        if (!isAdmin && residentId != null && !assignedTaskService.isResidentAssignedToStaff(username, residentId)) {
            redirectAttributes.addFlashAttribute("vitalsMessage",
                    "You can only log vitals for residents assigned to you.");
            return "redirect:/MyAssignedResidents";
        }

        model.addAttribute("assignedResidents", assignedResidents);
        model.addAttribute("selectedResident",
                assignedTaskService.getAssignedResidentForStaff(username, residentId).orElse(null));
        model.addAttribute("selectedResidentId", residentId);
        return "VitalLogging";
    }

    @GetMapping("/VitalLoggingTable")
    @PreAuthorize("hasRole('STAFF')")
    public String vitalLoggingTable(Model model, Authentication authentication) {
        model.addAttribute("vitalsList", vitalsService.getVitalsLoggedBy(authentication.getName()));
        return "VitalLoggingTable";
    }

    @GetMapping("/admin/VitalLoggingTable")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminVitalLoggingTable(Model model) {
        model.addAttribute("vitalsList", vitalsService.getAllVitals());
        return "admin-vital-logging-table";
    }

    @PostMapping("/vitals")
    @PreAuthorize("hasAnyRole('STAFF')")
    public String addVitals(
            @RequestParam Long residentId,
            @RequestParam int temperature,
            @RequestParam String bloodPressure,
            @RequestParam int weight,
            @RequestParam(required = false) String notes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (!hasRole(authentication, "ROLE_ADMIN")
                && !assignedTaskService.isResidentAssignedToStaff(authentication.getName(), residentId)) {
            redirectAttributes.addFlashAttribute("vitalsMessage",
                    "You can only log vitals for residents assigned to you.");
            return "redirect:/MyAssignedResidents";
        }

        vitalsService.addVitals(residentId, temperature, bloodPressure, weight, notes, authentication.getName());
        redirectAttributes.addFlashAttribute("vitalsMessage", "Vitals logged successfully.");
        return "redirect:/VitalLoggingTable";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
