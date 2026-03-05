package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.AllArgsConstructor;
import nursing_home.example.demo.services.VitalsService;

@Controller
@AllArgsConstructor
public class VitalController {
    private VitalsService vitalsService;

    @GetMapping("/VitalLoggingTable")
    @PreAuthorize("hasRole('STAFF')")
    public String vitalLoggingTable() {
        return "VitalLoggingTable";
    }

    @PostMapping("/VitalLoggingTable")
    @PreAuthorize("hasRole('STAFF')")
    public String addVitals() {
        vitalsService.addVitals(null, null);
        return "redirect:/VitalLoggingTable";
    }
}
