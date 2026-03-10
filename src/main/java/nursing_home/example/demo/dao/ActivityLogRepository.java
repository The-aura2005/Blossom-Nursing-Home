package nursing_home.example.demo.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.model.ActivityLog;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    @Query("SELECT a FROM ActivityLog a JOIN FETCH a.resident ORDER BY a.activityDate DESC, a.activityTime DESC, a.id DESC")
    List<ActivityLog> findActivityHistoryWithResident();

    @Query("SELECT a FROM ActivityLog a JOIN FETCH a.resident WHERE a.resident.id = :residentId ORDER BY a.activityDate DESC, a.activityTime DESC, a.id DESC")
    List<ActivityLog> findByResidentIdOrderByActivityDateDescActivityTimeDescIdDesc(Long residentId);

    @Query("SELECT a FROM ActivityLog a JOIN FETCH a.resident WHERE a.resident.id = :residentId AND a.activityDate BETWEEN :fromDate AND :toDate ORDER BY a.activityDate DESC, a.activityTime DESC, a.id DESC")
    List<ActivityLog> findByResidentIdAndActivityDateBetweenOrderByActivityDateDescActivityTimeDescIdDesc(
            Long residentId,
            LocalDate fromDate, LocalDate toDate);

    @Query("SELECT a FROM ActivityLog a JOIN FETCH a.resident WHERE a.loggedByUsername = :username ORDER BY a.activityDate DESC, a.activityTime DESC, a.id DESC")
    List<ActivityLog> findByLoggedByUsernameOrderByActivityDateDescActivityTimeDescIdDesc(String username);
}
