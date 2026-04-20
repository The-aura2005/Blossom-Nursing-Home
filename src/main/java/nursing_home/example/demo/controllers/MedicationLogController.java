package nursing_home.example.demo.controllers;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.model.services.AssignedTaskService;
import nursing_home.example.demo.model.services.MedicationLogService;

@Controller
public class MedicationLogController {

    private final MedicationLogService medicationLogService;
    private final AssignedTaskService assignedTaskService;

    public MedicationLogController(MedicationLogService medicationLogService, AssignedTaskService assignedTaskService) {
        this.medicationLogService = medicationLogService;
        this.assignedTaskService = assignedTaskService;
    }

    @GetMapping("/medications")
    @PreAuthorize("hasRole('STAFF')")
    public String medications(Model model, Authentication authentication) {
        model.addAttribute("medicationLogs", medicationLogService.getLogsByStaffUsername(authentication.getName()));
        return "medications";
    }

    @GetMapping("/admin/medications")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminMedications(Model model) {
        model.addAttribute("medicationLogs", medicationLogService.getAllLogs());
        return "admin-medications";
    }

    @GetMapping("/medications/new/{residentId}")
    @PreAuthorize("hasRole('STAFF')")
    public String medicationForm(@PathVariable Long residentId, Authentication authentication, Model model,
            RedirectAttributes redirectAttributes) {
        if (!assignedTaskService.isResidentAssignedToStaff(authentication.getName(), residentId)) {
            redirectAttributes.addFlashAttribute("medicationError",
                    "You can only log medication for residents assigned to you.");
            return "redirect:/MyAssignedResidents";
        }

        model.addAttribute("selectedResident",
                assignedTaskService.getAssignedResidentForStaff(authentication.getName(), residentId).orElse(null));
        return "medication_form";
    }

    @PostMapping("/medications/save")
    @PreAuthorize("hasRole('STAFF')")
    public String saveMedication(
            @RequestParam Long residentId,
            @RequestParam String medicationName,
            @RequestParam String dosage,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timeGiven,
            @RequestParam(required = false) String notes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (!assignedTaskService.isResidentAssignedToStaff(authentication.getName(), residentId)) {
            redirectAttributes.addFlashAttribute("medicationError",
                    "You can only log medication for residents assigned to you.");
            return "redirect:/MyAssignedResidents";
        }

        medicationLogService.saveMedicationLog(residentId, authentication.getName(), medicationName, dosage, timeGiven,
                notes);
        redirectAttributes.addFlashAttribute("medicationMessage", "Medication logged successfully.");
        return "redirect:/medications";
    }
}
