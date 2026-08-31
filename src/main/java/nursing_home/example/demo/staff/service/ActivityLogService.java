package nursing_home.example.demo.staff.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.admin.Repository.ResidentRepository;
import nursing_home.example.demo.staff.Repository.ActivityLogRepository;
import nursing_home.example.demo.staff.model.ActivityLog;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ResidentRepository residentRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository, ResidentRepository residentRepository) {
        this.activityLogRepository = activityLogRepository;
        this.residentRepository = residentRepository;
    }

    public void logActivity(Long residentId, String activityType, String notes, Integer intakePercentage,
            LocalDate activityDate, LocalTime activityTime, String loggedByUsername) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalStateException("Resident not found"));

        ActivityLog log = new ActivityLog();
        log.setResident(resident);
        log.setActivityType(activityType);
        log.setNotes(notes);
        if (intakePercentage != null && (intakePercentage < 0 || intakePercentage > 100)) {
            throw new IllegalStateException("Meal intake must be between 0 and 100 percent.");
        }
        log.setIntakePercentage("Meal".equalsIgnoreCase(activityType) ? intakePercentage : null);
        log.setActivityDate(activityDate != null ? activityDate : LocalDate.now());
        log.setActivityTime(activityTime != null ? activityTime : LocalTime.now());
        log.setLoggedByUsername(loggedByUsername);
        activityLogRepository.save(log);
    }

    public List<ActivityLog> getActivityHistory() {
        return activityLogRepository.findActivityHistoryWithResident();
    }

    public List<ActivityLog> getActivityHistoryForUser(String username) {
        return activityLogRepository.findByLoggedByUsernameOrderByActivityDateDescActivityTimeDescIdDesc(username);
    }
}
