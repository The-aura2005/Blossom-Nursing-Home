package nursing_home.example.demo.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import nursing_home.example.demo.model.AssignedTask;
import nursing_home.example.demo.model.services.AssignedTaskService;

import java.util.Comparator;

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
        model.addAttribute("assignedTasks", assignedTaskService.getAllTasks().stream()
                .filter(task -> "PENDING".equalsIgnoreCase(task.getStatus()))
                .sorted(Comparator.comparing(AssignedTask::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList());
        return "assign-task";
    }

    @PostMapping("/assign-task")
    @PreAuthorize("hasRole('ADMIN')")
    public String assignTask(@ModelAttribute AssignedTask task, Authentication authentication, Model model) {
        assignedTaskService.assignTask(task, authentication.getName());
        model.addAttribute("task", new AssignedTask());
        model.addAttribute("residents", assignedTaskService.getAllResidentsForAssignment());
        model.addAttribute("staffUsernames", assignedTaskService.getStaffUsernames());
        model.addAttribute("assignedTasks", assignedTaskService.getAllTasks().stream()
                .filter(taskRow -> "PENDING".equalsIgnoreCase(taskRow.getStatus()))
                .sorted(Comparator.comparing(AssignedTask::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList());
        model.addAttribute("successMessage", "Task assigned successfully.");
        return "assign-task";
    }
}
