package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.services.AssignedTaskService;

@Controller
public class StaffDashboardController {

    private final AssignedTaskService assignedTaskService;

    public StaffDashboardController(AssignedTaskService assignedTaskService) {
        this.assignedTaskService = assignedTaskService;
    }

    @GetMapping("/staff-dashboard")
    @PreAuthorize("hasRole('STAFF')")
    public String staffDashboard() {
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
        return "redirect:/VitalLogging";
    }

    @GetMapping("/myReports")
    @PreAuthorize("hasRole('STAFF')")
    public String myreports() {
        return "myReports";
    }

    @GetMapping("/staff-dashboard-layout")
    @PreAuthorize("hasRole('STAFF')")
    public String staffDashboardLayout() {
        return "staff-dashboard-layout";
    }
}
