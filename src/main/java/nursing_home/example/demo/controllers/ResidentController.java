package nursing_home.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.ui.Model;
import lombok.AllArgsConstructor;
import nursing_home.example.demo.model.Resident;
import nursing_home.example.demo.services.ResidentService;

@Controller
@AllArgsConstructor
public class ResidentController {
    private final ResidentService residentService;

    @GetMapping("/addResident")
    public String addResident(Model model) {
        model.addAttribute("resident", new Resident());
        return "addResident";
    }

    @PostMapping("/residents")
    public String saveResident(@ModelAttribute Resident resident) {
        residentService.addResident(resident);
        return "redirect:/residents";
    }

    @GetMapping("/residents")
    public String viewResidents(Model model) {
        model.addAttribute("residents", residentService.viewResidents());
        return "residents";
    }

}
