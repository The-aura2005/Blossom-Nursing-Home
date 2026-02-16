package nursing_home.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.AllArgsConstructor;
import nursing_home.example.demo.model.NursingHomeUser;
import nursing_home.example.demo.model.NursingHomeUserRole;
import nursing_home.example.demo.services.NursingHomeUserService;

@Controller
@AllArgsConstructor
public class LoginController {
    @Autowired
    private NursingHomeUserService nursingHomeUserService;

    @GetMapping("/loginn")
    public String login() {
        return "loginn"; // loads login.html
    }

    @PostMapping("/loginn")
    public String processLogin(@RequestParam String username, @RequestParam String password) {
        NursingHomeUser user = nursingHomeUserService.login(username, password);
        if (user == null) {
            return "redirect:/login-error";
        }
        NursingHomeUserRole role = user.getNursingHomeUserRole();
        if (role == NursingHomeUserRole.ADMIN) {
            return "redirect:/admin-dashboard";
        } else if (role == NursingHomeUserRole.STAFF) {
            return "redirect:/staff-dashboard";
        } else if (role == NursingHomeUserRole.ACCOUNTANT) {
            return "redirect:/accountant-dashboard";
        } else {
            return "redirect:/login-error";
        }

    }
}
