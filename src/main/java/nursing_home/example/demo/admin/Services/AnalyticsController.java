package nursing_home.example.demo.admin.Services;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import nursing_home.example.demo.analytics.AnalyticsService;
import nursing_home.example.demo.analytics.DashboardResponse;
import nursing_home.example.demo.analytics.AnalyticsService.ResidentNotFoundException;

@RestController
public class AnalyticsController {
    private final AnalyticsService analyticsService;
    private final AssignedTaskService assignedTaskService;

    public AnalyticsController(AnalyticsService analyticsService, AssignedTaskService assignedTaskService) {
        this.analyticsService = analyticsService;
        this.assignedTaskService = assignedTaskService;
    }

    @GetMapping("/api/residents/{residentId}/analytics")
    @PreAuthorize("hasRole('STAFF')")
    public DashboardResponse analytics(@PathVariable Long residentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        if (!assignedTaskService.isResidentAssignedToStaff(authentication.getName(), residentId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Resident is not assigned to this staff member");
        }
        try {
            return analyticsService.getDashboard(residentId,
                    startDate == null ? LocalDate.now().minusDays(6) : startDate,
                    endDate == null ? LocalDate.now() : endDate);
        } catch (AnalyticsService.ResidentNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resident not found");
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}