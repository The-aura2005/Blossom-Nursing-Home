package nursing_home.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    //@GetMapping("/residents")
    //public String residentPage() {
        //return "residents";
    //}
    @GetMapping("/billingreports")
    public String billingReportsPage() {
        return "billingreports";
    }

    @GetMapping("/medical")
    public String medicalPage() {
        return "medical";
    }

    @GetMapping("/settings")
    public String settingsPage() {
        return "settings";
    }

    @GetMapping("/logout")
    public String logoutPage() {
        return "logout";
    }

}
