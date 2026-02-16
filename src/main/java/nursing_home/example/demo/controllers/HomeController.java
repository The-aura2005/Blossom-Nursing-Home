package nursing_home.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index(){
        return "index";
    }
    @GetMapping("/contact")
    public String contact() {
        return "contact"; 
    }

    @GetMapping("/sign-in")
    public String signIn(){
        return "sign-in";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(){
        return "admin-dashboard";
    }
    @GetMapping("/staff-dashboard")
    public String staffDashboard(){
        return "staff-dashboard";
    }
    @GetMapping("/accountant-dashboard")
    public String accountantDashboard(){
        return "accountant-dashboard";
    }
   
}