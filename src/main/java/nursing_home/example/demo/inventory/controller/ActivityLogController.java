package nursing_home.example.demo.inventory.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nursing_home.example.demo.inventory.model.InventoryActivityLog;
import nursing_home.example.demo.inventory.service.ActivityLogService;

@RestController("inventoryActivityLogController")
@RequestMapping("/api/logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public List<InventoryActivityLog> getAllLogs() {
        return activityLogService.getAllLogs();
    }
}
