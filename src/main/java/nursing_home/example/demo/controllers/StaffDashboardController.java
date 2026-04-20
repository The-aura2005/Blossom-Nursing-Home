package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import nursing_home.example.demo.model.AssignedTask;
import nursing_home.example.demo.model.services.AssignedTaskService;
import nursing_home.example.demo.model.services.MedicationLogService;

@Controller
public class StaffDashboardController {

    private final AssignedTaskService assignedTaskService;
    private final MedicationLogService medicationLogService;

    public StaffDashboardController(AssignedTaskService assignedTaskService,
            MedicationLogService medicationLogService) {
        this.assignedTaskService = assignedTaskService;
        this.medicationLogService = medicationLogService;
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
        return "myReports";
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
