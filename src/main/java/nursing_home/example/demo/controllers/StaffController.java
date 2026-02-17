package nursing_home.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.AllArgsConstructor;
import nursing_home.example.demo.model.Staff;
import nursing_home.example.demo.services.StaffService;

@Controller
@AllArgsConstructor
public class StaffController {
    private StaffService staffService;

    @GetMapping("/addStaff")
    public String addStaff(Model model){
        model.addAttribute("staff",new Staff());
        return "addStaff";
    }
    @PostMapping("/staffs")
    public String saveStaff(@ModelAttribute Staff staff){
        staffService.addStaff(staff);
        staffService.updateStaff(staff);
        return "redirect:/staffs";
    }

    @GetMapping("/staffs")
    public String viewStaff(Model model){
        model.addAttribute("staffs",staffService.viewStaffs());
        return "staffs";
    }
    
    @PostMapping("/staffs/delete")
    public String deleteStaff(Long id){
        staffService.deleteStaff(id);
        return "redirect:/staffs";
    }
    @GetMapping("/editStaff")
    public String editStaff(Long id,Model model){
        Staff staff = staffService.getStaffById(id);
        model.addAttribute("staff",staff);
        return "editStaff";

    }
    @PostMapping("/staffs/update")
    public String updateStaff(@ModelAttribute Staff staff){
        staffService.updateStaff(staff);
        return "redirect:/staffs";
    }

    
}
