package nursing_home.example.demo.model.services;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.dao.ActivityLogRepository;
import nursing_home.example.demo.dao.ResidentRepository;
import nursing_home.example.demo.model.ActivityLog;
import nursing_home.example.demo.model.Resident;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ResidentRepository residentRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository, ResidentRepository residentRepository) {
        this.activityLogRepository = activityLogRepository;
        this.residentRepository = residentRepository;
    }

    public void logActivity(Long residentId, String activityType, String notes, LocalDate activityDate,
            LocalTime activityTime, String loggedByUsername) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalStateException("Resident not found"));

        ActivityLog log = new ActivityLog();
        log.setResident(resident);
        log.setActivityType(activityType);
        log.setNotes(notes);
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
