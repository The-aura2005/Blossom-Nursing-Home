package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountantController {

    @GetMapping("/accountant-dashboard")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String accountantDashboard() {
        return "accountant-dashboard";
    }
}
