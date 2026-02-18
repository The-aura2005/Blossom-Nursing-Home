package nursing_home.example.demo.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffDashboardController {
     
    @GetMapping("/MyTask")
    public String myTasks(){
        return "MyTask";
    }
    @GetMapping("/MyAssignedResidents")
    public String myAssignedResidents(){
        return "MyAssignedResidents";
    }
    @GetMapping("/VitalLogging")
    public String vitalLoggings(){
        return "VitalLogging";
    }
    @GetMapping("/activitiesLogging")
    public String activitiesLogging(){
        return "activitiesLogging";
    }
    @GetMapping("/myReports")
    public String myreports(){
        return "myReports";
    }
    @GetMapping("/staff-dashboard-layout")
    public String staffDashboardLayout(){
        return "staff-dashboard-layout";
    }

}