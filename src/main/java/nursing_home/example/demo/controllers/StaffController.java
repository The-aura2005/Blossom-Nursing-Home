package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.model.Staff;
import nursing_home.example.demo.services.StaffService;

@Controller
public class StaffController {
    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping("/addStaff")
    @PreAuthorize("hasRole('ADMIN')")
    public String addStaff(Model model) {
        model.addAttribute("staff", new Staff());
        return "addStaff";
    }

    @PostMapping("/staffs")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveStaff(@ModelAttribute Staff staff, RedirectAttributes redirectAttributes) {
        StaffService.StaffCreationResult created = staffService.addStaff(staff);
        redirectAttributes.addFlashAttribute(
                "staffCreatedMessage",
                "Staff added. Login created: username=" + created.username() + ", temporary password="
                        + created.temporaryPassword());
        return "redirect:/staffs";
    }

    @GetMapping("/staffs")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewStaff(Model model) {
        model.addAttribute("staffs", staffService.viewStaffs());
        return "staffs";
    }

    @PostMapping("/staffs/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteStaff(Long id) {
        staffService.deleteStaff(id);
        return "redirect:/staffs";
    }

    @GetMapping("/editStaff")
    @PreAuthorize("hasRole('ADMIN')")
    public String editStaff(Long id, Model model) {
        Staff staff = staffService.getStaffById(id);
        model.addAttribute("staff", staff);
        return "editStaff";
    }

    @PostMapping("/staffs/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStaff(@ModelAttribute Staff staff) {
        staffService.updateStaff(staff);
        return "redirect:/staffs";
    }
}
