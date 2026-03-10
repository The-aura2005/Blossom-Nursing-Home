package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.services.AssignedTaskService;
import nursing_home.example.demo.services.IncidentReportService;

@Controller
public class IncidentReportController {

    private final IncidentReportService incidentReportService;
    private final AssignedTaskService assignedTaskService;

    public IncidentReportController(IncidentReportService incidentReportService,
            AssignedTaskService assignedTaskService) {
        this.incidentReportService = incidentReportService;
        this.assignedTaskService = assignedTaskService;
    }

    @GetMapping("/incident-reports")
    @PreAuthorize("hasRole('STAFF')")
    public String incidentReports(Model model, Authentication authentication) {
        model.addAttribute("incidentReports",
                incidentReportService.getReportsByStaffUsername(authentication.getName()));
        return "incident_reports";
    }

    @GetMapping("/admin/incident-reports")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminIncidentReports(Model model) {
        model.addAttribute("incidentReports", incidentReportService.getAllReports());
        return "admin-incident-reports";
    }

    @GetMapping("/reports/new/{residentId}")
    @PreAuthorize("hasRole('STAFF')")
    public String reportForm(@PathVariable Long residentId, Authentication authentication, Model model,
            RedirectAttributes redirectAttributes) {
        if (!assignedTaskService.isResidentAssignedToStaff(authentication.getName(), residentId)) {
            redirectAttributes.addFlashAttribute("incidentError",
                    "You can only create reports for residents assigned to you.");
            return "redirect:/MyAssignedResidents";
        }

        model.addAttribute("selectedResident",
                assignedTaskService.getAssignedResidentForStaff(authentication.getName(), residentId).orElse(null));
        return "report_form";
    }

    @PostMapping("/reports/save")
    @PreAuthorize("hasRole('STAFF')")
    public String saveReport(
            @RequestParam Long residentId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String severity,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (!assignedTaskService.isResidentAssignedToStaff(authentication.getName(), residentId)) {
            redirectAttributes.addFlashAttribute("incidentError",
                    "You can only create reports for residents assigned to you.");
            return "redirect:/MyAssignedResidents";
        }

        incidentReportService.saveReport(residentId, authentication.getName(), title, description, severity);
        redirectAttributes.addFlashAttribute("incidentMessage", "Incident report submitted successfully.");
        return "redirect:/incident-reports";
    }
}
