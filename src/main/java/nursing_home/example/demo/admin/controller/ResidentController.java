package nursing_home.example.demo.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.admin.Services.ResidentService;
import nursing_home.example.demo.staff.service.VitalsService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResidentController {
    @Autowired
    private ResidentService residentService;

    @Autowired
    private VitalsService vitalsService;

    @GetMapping("/addResident")
    @PreAuthorize("hasRole('ADMIN')")
    public String addResident(Model model) {
        model.addAttribute("resident", new Resident());
        return "addResident";
    }

    @PostMapping("/residents")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveResident(@ModelAttribute Resident resident) {
        residentService.addResident(resident);
        return "redirect:/residents";
    }

    @GetMapping("/residents")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewResidents(Model model) {
        // adding a string attribute to the model
        model.addAttribute("residents", residentService.viewResidents());
        return "residents";
    }

    @GetMapping("/admin/resident-detail")
    @PreAuthorize("hasRole('ADMIN')")
    public String residentDetail(
            @RequestParam(value = "residentId", required = false) Long residentId,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (residentId == null) {
            redirectAttributes.addFlashAttribute("residentMessage", "Resident ID is missing.");
            return "redirect:/residents";
        }

        Resident resident = residentService.getResidentById(residentId);
        if (resident == null) {
            redirectAttributes.addFlashAttribute("residentMessage", "Resident not found.");
            return "redirect:/residents";
        }

        String admissionDate = resident.getAdmissionDate() != null
                ? resident.getAdmissionDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "Not recorded";
        model.addAttribute("resident", resident);
        model.addAttribute("loggedInUser", authentication.getName());
        model.addAttribute("residentPrimaryNurse", "Not recorded");
        model.addAttribute("residentAllergies", "Not recorded");
        model.addAttribute("residentDiet", "Not recorded");
        model.addAttribute("residentMobility", "Not recorded");
        model.addAttribute("residentAdmissionDate", admissionDate);
        model.addAttribute("residentVitals", vitalsService.getVitalsForResident(residentId).stream().limit(5).toList());
        return "resident-detailPage";
    }

    @PostMapping("/residents/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteResident(@RequestParam Long id) {
        residentService.deleteResident(id);
        return "redirect:/residents";
    }

    @GetMapping("/editResident")
    @PreAuthorize("hasRole('ADMIN')")
    public String editResident(@RequestParam Long id, Model model) {
        Resident resident = residentService.getResidentById(id);
        model.addAttribute("resident", resident);
        return "editResident";
    }

    @PostMapping("/residents/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateResident(@ModelAttribute Resident resident) {
        residentService.updateResident(resident);
        return "redirect:/residents";
    }
}
