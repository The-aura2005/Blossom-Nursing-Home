package nursing_home.example.demo.staff.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.admin.Services.AssignedTaskService;
import nursing_home.example.demo.staff.service.ActivityLogService;

@Controller
public class ActivityLogController {
    // services for activity logging and assigned tasks.

    private final ActivityLogService activityLogService;
    private final AssignedTaskService assignedTaskService;

    public ActivityLogController(ActivityLogService activityLogService, AssignedTaskService assignedTaskService) {
        this.activityLogService = activityLogService;
        this.assignedTaskService = assignedTaskService;
    }

    @GetMapping("/activitiesLogging")
    @PreAuthorize("hasRole('STAFF')")
    public String activitiesLogging(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("activityHistory", activityLogService.getActivityHistoryForUser(username));
        model.addAttribute("activityTasks", assignedTaskService.getTasksForStaff(username));
        model.addAttribute("loggedInUser", username);
        return "activitiesLoggingReports";
    }

    @GetMapping("/ActivityLogging")
    @PreAuthorize("hasRole('STAFF')")
    public String addActivityPage(
            @RequestParam(value = "residentId", required = false) Long residentId,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        String username = authentication.getName();

        if (residentId == null) {
            redirectAttributes.addFlashAttribute("activityError",
                    "Please click Add Activity from a resident card to log for that resident.");
            return "redirect:/MyAssignedResidents";
        }

        if (!assignedTaskService.isResidentAssignedToStaff(username, residentId)) {
            redirectAttributes.addFlashAttribute("activityError",
                    "You can only log activities for residents assigned to you.");
            return "redirect:/MyAssignedResidents";
        }

        model.addAttribute("selectedResident",
                assignedTaskService.getAssignedResidentForStaff(username, residentId).orElse(null));
        model.addAttribute("selectedResidentId", residentId);
        model.addAttribute("loggedInUser", username);
        model.addAttribute("currentActivityDate", LocalDate.now());
        model.addAttribute("currentActivityTime", LocalTime.now().withSecond(0).withNano(0));
        return "ActivityLogging";
    }

    @PostMapping("/activitiesLogging/add")
    @PreAuthorize("hasRole('STAFF')")
    public String submitActivity(
            @RequestParam Long residentId,
            @RequestParam String activityType,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) Integer intakePercentage,
            @RequestParam(required = false) String activityDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime activityTime,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (!assignedTaskService.isResidentAssignedToStaff(authentication.getName(), residentId)) {
            redirectAttributes.addFlashAttribute("activityError",
                    "You can only log activities for residents assigned to you.");
            return "redirect:/activitiesLogging";
        }

        try {
            LocalDate parsedActivityDate = parseActivityDate(activityDate);
            activityLogService.logActivity(
                    residentId,
                    activityType,
                    notes,
                    intakePercentage,
                    parsedActivityDate,
                    activityTime,
                    authentication.getName());
            assignedTaskService.completeNextPendingTaskForResident(residentId, authentication.getName());
            redirectAttributes.addFlashAttribute("activityMessage", "Activity logged successfully.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("activityError", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("activityError",
                    "Unable to submit activity. Please check the form values.");
        }
        return "redirect:/activitiesLogging";
    }

    private LocalDate parseActivityDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.matches("8\\d{4}-\\d{2}-\\d{2}")) {
            normalized = normalized.substring(1);
        }
        try {
            return LocalDate.parse(normalized, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new IllegalStateException("Please enter a valid activity date.");
        }
    }

    @GetMapping("/admin/activitiesLogging")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminActivitiesLogging(Model model) {
        model.addAttribute("activityHistory", activityLogService.getActivityHistory());
        model.addAttribute("activityTasks", assignedTaskService.getAllTasks());
        return "admin-activities-logging";
    }
}
