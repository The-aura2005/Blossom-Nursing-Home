package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public String settingsPage() {
        return "settings";
    }

    @GetMapping("/billingreports")
    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public String billingReportsPage() {
        return "billingreports";
    }
}
