package nursing_home.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.model.IncidentReport;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {
    List<IncidentReport> findByResidentIdOrderByCreatedAtDesc(Long residentId);

    List<IncidentReport> findByStaffIdOrderByCreatedAtDesc(Long staffId);
}
