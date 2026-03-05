package nursing_home.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import nursing_home.example.demo.model.Resident;
import nursing_home.example.demo.services.ResidentService;
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

    @GetMapping("/addResident")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public String addResident(Model model) {
        model.addAttribute("resident", new Resident());
        return "addResident";
    }

    @PostMapping("/residents")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public String saveResident(@ModelAttribute Resident resident) {
        residentService.addResident(resident);
        residentService.updateResident(resident);
        return "redirect:/residents";
    }

    @GetMapping("/residents")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewResidents(Model model) {
        model.addAttribute("residents", residentService.viewResidents());
        return "residents";
    }

    @PostMapping("/residents/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteResident(@RequestParam Long id) {
        residentService.deleteResident(id);
        return "redirect:/residents";
    }

    @GetMapping("/editResident")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public String editResident(@RequestParam Long id, Model model) {
        Resident resident = residentService.getResidentById(id);
        model.addAttribute("resident", resident);
        return "editResident";
    }

    @PostMapping("/residents/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public String updateResident(@ModelAttribute Resident resident) {
        residentService.updateResident(resident);
        return "redirect:/residents";
    }
}
