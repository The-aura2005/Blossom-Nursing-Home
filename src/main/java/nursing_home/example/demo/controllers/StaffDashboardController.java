package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffDashboardController {

    @GetMapping("/staff-dashboard")
    @PreAuthorize("hasRole('STAFF')")
    public String staffDashboard() {
        return "staff-dashboard";
    }

    @GetMapping("/MyTask")
    @PreAuthorize("hasRole('STAFF')")
    public String myTasks() {
        return "MyTask";
    }

    @GetMapping("/MyAssignedResidents")
    @PreAuthorize("hasRole('STAFF')")
    public String myAssignedResidents() {
        return "MyAssignedResidents";
    }

    @GetMapping("/VitalLogging")
    @PreAuthorize("hasRole('STAFF')")
    public String vitalLogging() {
        return "VitalLogging";
    }

    @GetMapping("/activitiesLogging")
    @PreAuthorize("hasRole('STAFF')")
    public String activitiesLogging() {
        return "activitiesLogging";
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
