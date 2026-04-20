package nursing_home.example.demo.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.model.ResidentMedicalCondition.ConditionType;
import nursing_home.example.demo.model.services.MedicalReportService;
import nursing_home.example.demo.model.services.MedicalReportService.MedicalReportView;

@Controller
public class MedicalReportController {

    private final MedicalReportService medicalReportService;

    public MedicalReportController(MedicalReportService medicalReportService) {
        this.medicalReportService = medicalReportService;
    }

    @GetMapping("/medical")
    @PreAuthorize("hasRole('ADMIN')")
    public String medicalPage(
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {
        MedicalReportView report = medicalReportService.buildReport(residentId, fromDate, toDate);
        model.addAttribute("residents", medicalReportService.getResidents());
        model.addAttribute("report", report);
        return "medical";
    }

    @PostMapping("/medical/conditions")
    @PreAuthorize("hasRole('ADMIN')")
    public String addCondition(
            @RequestParam Long residentId,
            @RequestParam ConditionType conditionType,
            @RequestParam String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            RedirectAttributes redirectAttributes) {
        try {
            medicalReportService.addCondition(residentId, conditionType, description);
            redirectAttributes.addFlashAttribute("medicalMessage", "Condition saved successfully.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("medicalError", ex.getMessage());
        }

        return "redirect:/medical?residentId=" + residentId + buildDateQuery(fromDate, toDate);
    }

    @PostMapping("/medical/medications")
    @PreAuthorize("hasRole('ADMIN')")
    public String addMedication(
            @RequestParam Long residentId,
            @RequestParam String medicationName,
            @RequestParam String dosage,
            @RequestParam String administeredByUsername,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            RedirectAttributes redirectAttributes) {
        try {
            medicalReportService.addMedication(residentId, medicationName, dosage, administeredByUsername, notes);
            redirectAttributes.addFlashAttribute("medicalMessage", "Medication entry saved successfully.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("medicalError", ex.getMessage());
        }

        return "redirect:/medical?residentId=" + residentId + buildDateQuery(fromDate, toDate);
    }

    private String buildDateQuery(LocalDate fromDate, LocalDate toDate) {
        StringBuilder query = new StringBuilder();
        if (fromDate != null) {
            query.append("&fromDate=").append(fromDate);
        }
        if (toDate != null) {
            query.append("&toDate=").append(toDate);
        }
        return query.toString();
    }
}
