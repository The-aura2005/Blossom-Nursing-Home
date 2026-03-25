package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountantDashboard {
    @GetMapping("/accountant-dashboard")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String accountantDashboard() {
        return "accountant-dashboard";
    }

    @GetMapping("/resident-payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String residentPayments() {
        return "resident-payments";
    }

    @GetMapping("/staff-payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String staffPayments() {
        return "staff-payments";
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String ivoices() {
        return "invoices";
    }

    @GetMapping("/inventory-payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String inventoryPayments() {
        return "inventory-payments";
    }

    @GetMapping("/inventory-manager")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String inventoryManager() {
        return "inventory-manager";
    }

    @GetMapping("/report-payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String reportPayments() {
        return "report-payments";
    }

}
