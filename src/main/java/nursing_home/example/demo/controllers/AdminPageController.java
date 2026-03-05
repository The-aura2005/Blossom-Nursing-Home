package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping("/billingreports")
    @PreAuthorize("hasRole('ADMIN')")
    public String billingReportsPage() {
        return "billingreports";
    }

    @GetMapping("/medical")
    @PreAuthorize("hasRole('ADMIN')")
    public String medicalPage() {
        return "medical";
    }

    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    public String settingsPage() {
        return "settings";
    }
}
