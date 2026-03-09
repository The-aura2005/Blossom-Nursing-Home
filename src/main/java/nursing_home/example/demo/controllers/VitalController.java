package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.services.ResidentService;
import nursing_home.example.demo.services.VitalsService;

import java.util.Collections;

@Controller
public class VitalController {
    private final VitalsService vitalsService;
    private final ResidentService residentService;

    public VitalController(VitalsService vitalsService, ResidentService residentService) {
        this.vitalsService = vitalsService;
        this.residentService = residentService;
    }

    @GetMapping("/VitalLogging")
    @PreAuthorize("hasRole('STAFF')")
    public String vitalLogging(Model model) {
        try {
            model.addAttribute("residents", residentService.viewResidents());
        } catch (IllegalStateException ex) {
            model.addAttribute("residents", Collections.emptyList());
        }
        return "VitalLogging";
    }

    @GetMapping("/VitalLoggingTable")
    @PreAuthorize("hasRole('STAFF')")
    public String vitalLoggingTable(Model model) {
        model.addAttribute("vitalsList", vitalsService.getAllVitals());
        return "VitalLoggingTable";
    }

    @PostMapping("/vitals")
    @PreAuthorize("hasRole('STAFF')")
    public String addVitals(
            @RequestParam Long residentId,
            @RequestParam int temperature,
            @RequestParam String bloodPressure,
            @RequestParam int weight,
            @RequestParam(required = false) String notes,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        vitalsService.addVitals(residentId, temperature, bloodPressure, weight, notes, authentication.getName());
        redirectAttributes.addFlashAttribute("vitalsMessage", "Vitals logged successfully.");
        return "redirect:/VitalLoggingTable";
    }
}
