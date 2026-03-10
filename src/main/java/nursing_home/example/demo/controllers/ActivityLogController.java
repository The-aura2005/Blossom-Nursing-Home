package nursing_home.example.demo.controllers;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.services.ActivityLogService;
import nursing_home.example.demo.services.AssignedTaskService;

@Controller
public class ActivityLogController {

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
        model.addAttribute("loggedInUser", username);
        return "activitiesLogging";
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
        return "ActivityLogging";
    }

    @PostMapping("/activitiesLogging/add")
    @PreAuthorize("hasRole('STAFF')")
    public String submitActivity(
            @RequestParam Long residentId,
            @RequestParam String activityType,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activityDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime activityTime,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (!assignedTaskService.isResidentAssignedToStaff(authentication.getName(), residentId)) {
            redirectAttributes.addFlashAttribute("activityError",
                    "You can only log activities for residents assigned to you.");
            return "redirect:/activitiesLogging";
        }

        try {
            activityLogService.logActivity(
                    residentId,
                    activityType,
                    notes,
                    activityDate,
                    activityTime,
                    authentication.getName());
            redirectAttributes.addFlashAttribute("activityMessage", "Activity logged successfully.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("activityError", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("activityError",
                    "Unable to submit activity. Please check the form values.");
        }
        return "redirect:/activitiesLogging";
    }

    @GetMapping("/admin/activitiesLogging")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminActivitiesLogging(Model model) {
        model.addAttribute("activityHistory", activityLogService.getActivityHistory());
        return "admin-activities-logging";
    }
}
