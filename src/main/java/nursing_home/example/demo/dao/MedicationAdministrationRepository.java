package nursing_home.example.demo.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.model.MedicationAdministration;

@Repository
public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministration, Long> {
    List<MedicationAdministration> findByResidentIdOrderByAdministeredAtDescIdDesc(Long residentId);

    List<MedicationAdministration> findByResidentIdAndAdministeredAtBetweenOrderByAdministeredAtDescIdDesc(
            Long residentId,
            LocalDateTime fromDateTime, LocalDateTime toDateTime);
}
