package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import nursing_home.example.demo.model.AssignedTask;
import nursing_home.example.demo.services.AssignedTaskService;

@Controller
public class TaskManagementController {

    private final AssignedTaskService assignedTaskService;

    public TaskManagementController(AssignedTaskService assignedTaskService) {
        this.assignedTaskService = assignedTaskService;
    }

    @GetMapping("/assign-task")
    @PreAuthorize("hasRole('ADMIN')")
    public String assignTaskPage(Model model) {
        model.addAttribute("task", new AssignedTask());
        model.addAttribute("residents", assignedTaskService.getAllResidentsForAssignment());
        model.addAttribute("staffUsernames", assignedTaskService.getStaffUsernames());
        model.addAttribute("assignedTasks", assignedTaskService.getAllTasks());
        return "assign-task";
    }

    @PostMapping("/assign-task")
    @PreAuthorize("hasRole('ADMIN')")
    public String assignTask(@ModelAttribute AssignedTask task, Authentication authentication, Model model) {
        assignedTaskService.assignTask(task, authentication.getName());
        model.addAttribute("task", new AssignedTask());
        model.addAttribute("residents", assignedTaskService.getAllResidentsForAssignment());
        model.addAttribute("staffUsernames", assignedTaskService.getStaffUsernames());
        model.addAttribute("assignedTasks", assignedTaskService.getAllTasks());
        model.addAttribute("successMessage", "Task assigned successfully.");
        return "assign-task";
    }
}
