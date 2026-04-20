package nursing_home.example.demo.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.inventory.model.InventoryActivityLog;
import nursing_home.example.demo.inventory.repository.ActivityLogRepository;

@Service("inventoryActivityLogService")
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void logAction(String action, String itemName) {
        InventoryActivityLog log = new InventoryActivityLog();
        log.setAction(action);
        log.setItemName(itemName);
        log.setQuantityChanged(0);
        activityLogRepository.save(log);
    }

    public void logIssue(String itemName, int quantityIssued, String staffUsername, Long residentId,
            String residentName,
            String notes) {
        InventoryActivityLog log = new InventoryActivityLog();
        log.setAction("ISSUE");
        log.setItemName(itemName);
        log.setQuantityChanged(quantityIssued);
        log.setStaffUsername(staffUsername);
        log.setResidentId(residentId);
        log.setResidentName(residentName);
        log.setNotes(notes);
        activityLogRepository.save(log);
    }

    public List<InventoryActivityLog> getAllLogs() {
        return activityLogRepository.findAllByOrderByTimestampDesc();
    }
}
