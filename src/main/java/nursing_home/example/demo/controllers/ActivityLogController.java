package nursing_home.example.demo.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.services.ActivityLogService;
import nursing_home.example.demo.services.ResidentService;

@Controller
public class ActivityLogController {

    private final ActivityLogService activityLogService;
    private final ResidentService residentService;

    public ActivityLogController(ActivityLogService activityLogService, ResidentService residentService) {
        this.activityLogService = activityLogService;
        this.residentService = residentService;
    }

    @GetMapping("/activitiesLogging")
    @PreAuthorize("hasRole('STAFF')")
    public String activitiesLogging(Model model, Authentication authentication) {
        try {
            model.addAttribute("residents", residentService.viewResidents());
        } catch (IllegalStateException ex) {
            model.addAttribute("residents", Collections.emptyList());
        }

        model.addAttribute("activityHistory", activityLogService.getActivityHistory());
        model.addAttribute("loggedInUser", authentication.getName());
        return "activitiesLogging";
    }

    @PostMapping("/activitiesLogging")
    @PreAuthorize("hasRole('STAFF')")
    public String submitActivity(
            @RequestParam Long residentId,
            @RequestParam String activityType,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate activityDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime activityTime,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
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
}
