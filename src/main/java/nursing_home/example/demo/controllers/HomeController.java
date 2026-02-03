package nursing_home.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import nursing_home.example.demo.model.NursingHomeUserRole;
import nursing_home.example.demo.services.NursingHomeUserService;

@Controller
public class HomeController {

    @Autowired
    private NursingHomeUserService userService;
 
    @GetMapping("/")
    public String index(){
        return "index";
    }
    @GetMapping("/contact")
    public String contact() {
        return "contact"; // loads contact.html
    }

    @GetMapping("/loginn")
    public String login() {
        return "loginn"; // loads login.html
    }
     
    @PostMapping("/loginn")
    public String processLogin(@RequestParam String username,@RequestParam String password){
        NursingHomeUserRole role = userService.authenticate(username,password);

        if (role == null) {
            return "login-error"; // invalid credentials
        }
        switch (role) {
            case ADMIN:
                return "redirect:/admin-dashboard";
            case STAFF:
                return "redirect:/staff-dashboard";
            case ACCOUNTANT:
                return "redirect:/accountant-dashboard";
            default:
                return "login-error";
        }
        
    }

    @GetMapping("/sign-in")
    public String signIn(){
        return "sign-in";//loads sign-in.html
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(){
        return "admin-dashboard";
    }
}
