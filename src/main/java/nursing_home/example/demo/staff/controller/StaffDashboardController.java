package nursing_home.example.demo.staff.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import nursing_home.example.demo.admin.Model.AssignedTask;
import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.admin.Services.AssignedTaskService;
import nursing_home.example.demo.admin.Services.ResidentService;
import nursing_home.example.demo.staff.service.MedicationLogService;
import nursing_home.example.demo.staff.service.IncidentReportService;
import nursing_home.example.demo.staff.service.VitalsService;
import nursing_home.example.demo.staff.model.IncidentReport;
import nursing_home.example.demo.staff.model.Vitals;

@Controller
public class StaffDashboardController {

    private final AssignedTaskService assignedTaskService;
    private final MedicationLogService medicationLogService;
    private final ResidentService residentService;
    private final VitalsService vitalsService;
    private final IncidentReportService incidentReportService;

    public StaffDashboardController(AssignedTaskService assignedTaskService,
            MedicationLogService medicationLogService,
            ResidentService residentService,
            VitalsService vitalsService,
            IncidentReportService incidentReportService) {
        this.assignedTaskService = assignedTaskService;
        this.medicationLogService = medicationLogService;
        this.residentService = residentService;
        this.vitalsService = vitalsService;
        this.incidentReportService = incidentReportService;
    }

    @GetMapping("/staff-dashboard")
    @PreAuthorize("hasRole('STAFF')")
    public String staffDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<AssignedTask> tasks = assignedTaskService.getTasksForStaff(username);
        var assignedResidents = assignedTaskService.getAssignedResidentsForStaff(username);

        model.addAttribute("loggedInUser", username);
        model.addAttribute("tasks", tasks);
        model.addAttribute("dashboardTasks", tasks.stream().limit(5).toList());
        model.addAttribute("assignedResidents", assignedResidents);
        model.addAttribute("assignedTaskCount", tasks.size());
        model.addAttribute("assignedResidentCount", assignedResidents.size());
        model.addAttribute("pendingCount", assignedTaskService.getPendingCountForStaff(username));
        model.addAttribute("completedCount", assignedTaskService.getCompletedCountForStaff(username));
        var medicationLogs = medicationLogService.getLogsByStaffUsername(username);
        model.addAttribute("medicationLogCount", medicationLogs.size());
        model.addAttribute("recentMedicationLogs", medicationLogs.stream().limit(5).toList());
        model.addAttribute("dashboardMedicationLogs", medicationLogs.stream().limit(10).toList());
        return "staff-dashboard";
    }

    @GetMapping("/MyTask")
    @PreAuthorize("hasRole('STAFF')")
    public String myTasks(Authentication authentication, Model model) {
        String username = authentication.getName();
        model.addAttribute("tasks", assignedTaskService.getTasksForStaff(username));
        model.addAttribute("pendingCount", assignedTaskService.getPendingCountForStaff(username));
        model.addAttribute("completedCount", assignedTaskService.getCompletedCountForStaff(username));
        model.addAttribute("loggedInUser", username);
        return "MyTask";
    }

    @PostMapping("/MyTask/complete")
    @PreAuthorize("hasRole('STAFF')")
    public String completeTask(
            @RequestParam("taskId") Long taskId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        assignedTaskService.completeTask(taskId, authentication.getName());
        redirectAttributes.addFlashAttribute("taskMessage", "Task marked as completed. Log the activity below.");
        return "redirect:/activitiesLogging";
    }

    @GetMapping("/MyAssignedResidents")
    @PreAuthorize("hasRole('STAFF')")
    public String myAssignedResidents(Authentication authentication, Model model) {
        String username = authentication.getName();
        model.addAttribute("assignedResidents", assignedTaskService.getAssignedResidentsForStaff(username));
        model.addAttribute("loggedInUser", username);
        return "MyAssignedResidents";
    }

    @GetMapping("/resident-detailPage")
    @PreAuthorize("hasRole('STAFF')")
    public String residentDetailPage(
            @RequestParam(value = "residentId", required = false) Long residentId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        String username = authentication.getName();

        if (residentId == null) {
            redirectAttributes.addFlashAttribute("residentMessage",
                    "Resident details are unavailable because this assignment has no resident ID.");
            return "redirect:/MyAssignedResidents";
        }

        if (!assignedTaskService.isResidentAssignedToStaff(username, residentId)) {
            redirectAttributes.addFlashAttribute("residentMessage",
                    "You can only view details for residents assigned to you.");
            return "redirect:/MyAssignedResidents";
        }

        Resident resident = residentService.getResidentById(residentId);
        if (resident == null) {
            redirectAttributes.addFlashAttribute("residentMessage", "Resident not found.");
            return "redirect:/MyAssignedResidents";
        }

        String admissionDate = resident.getAdmissionDate() != null
                ? resident.getAdmissionDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "Not recorded";

        model.addAttribute("resident", resident);
        model.addAttribute("residentPrimaryNurse", "Not recorded");
        model.addAttribute("residentAllergies", "Not recorded");
        model.addAttribute("residentDiet", "Not recorded");
        model.addAttribute("residentMobility", "Not recorded");
        model.addAttribute("residentAdmissionDate", admissionDate);
        model.addAttribute("residentVitals", vitalsService.getVitalsForResident(residentId).stream().limit(5).toList());
        return "resident-detailPage";
    }

    @GetMapping("/staff-vital-logging")
    @PreAuthorize("hasRole('STAFF')")
    public String vitalLogging() {
        return "redirect:/VitalLoggingTable";
    }

    @GetMapping("/myReports")
    @PreAuthorize("hasRole('STAFF')")
    public String myreports(
            Authentication authentication,
            @RequestParam(value = "residentId", required = false) Long residentId,
            Model model) {
        String username = authentication.getName();
        List<AssignedTask> tasks = assignedTaskService.getTasksForStaff(username);
        List<AssignedTask> scopedTasks = tasks.stream()
                .filter(task -> residentId == null || residentId.equals(task.getResidentId()))
                .toList();

        List<AssignedTask> completedTasks = scopedTasks.stream()
                .filter(task -> "COMPLETED".equalsIgnoreCase(task.getStatus()))
                .limit(10)
                .toList();

        var assignedResidents = assignedTaskService.getAssignedResidentsForStaff(username).stream()
                .filter(resident -> residentId == null || residentId.equals(resident.residentId()))
                .toList();

        long pendingCount = scopedTasks.stream()
                .filter(task -> "PENDING".equalsIgnoreCase(task.getStatus()))
                .count();

        long completedCount = scopedTasks.stream()
                .filter(task -> "COMPLETED".equalsIgnoreCase(task.getStatus()))
                .count();

        model.addAttribute("loggedInUser", username);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("assignedResidents", assignedResidents);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("selectedResidentId", residentId);
        model.addAttribute("attentionAlerts", buildAttentionAlerts(username, scopedTasks));
        return "myReports";
    }

    private List<AttentionAlert> buildAttentionAlerts(String username, List<AssignedTask> tasks) {
        List<AttentionAlert> alerts = new ArrayList<>();
        List<Long> residentIds = tasks.stream().map(AssignedTask::getResidentId).filter(id -> id != null).distinct()
                .toList();
        for (Vitals vital : vitalsService.getVitalsForResidents(residentIds)) {
            if (!"NORMAL".equals(vital.getAlertStatus())) {
                alerts.add(new AttentionAlert(vital.getResident().getName(), vital.getAlertMessage(),
                        "/VitalLoggingTable"));
            }
        }
        tasks.stream().filter(task -> "NOT_COMPLETED".equals(task.getAttentionStatus())).forEach(task -> alerts.add(
                new AttentionAlert(task.getResidentName(), "Activity Not Completed: " + task.getTitle(), "/MyTask")));
        for (IncidentReport report : incidentReportService.getReportsByStaffUsername(username)) {
            alerts.add(new AttentionAlert(report.getResident().getName(), "Incident Reported - " + report.getSeverity(),
                    "/incident-reports"));
        }
        return alerts;
    }

    public record AttentionAlert(String residentName, String message, String link) {
    }

    @GetMapping("/my-reports")
    @PreAuthorize("hasRole('STAFF')")
    public String myReportsAlias() {
        return "redirect:/myReports";
    }

    @GetMapping("/MyReports")
    @PreAuthorize("hasRole('STAFF')")
    public String myReportsLegacyAlias() {
        return "redirect:/myReports";
    }

    @GetMapping("/staff-dashboard-layout")
    @PreAuthorize("hasRole('STAFF')")
    public String staffDashboardLayout() {
        return "staff-dashboard-layout";
    }
}
