package nursing_home.example.demo.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.model.MedicationAdministration;

@Repository
public interface MedicationAdministrationRepository extends JpaRepository<MedicationAdministration, Long> {
    @Query("SELECT m FROM MedicationAdministration m JOIN FETCH m.resident ORDER BY m.administeredAt DESC, m.id DESC")
    List<MedicationAdministration> findAllWithResidentOrderByAdministeredAtDescIdDesc();

    List<MedicationAdministration> findByResidentIdOrderByAdministeredAtDescIdDesc(Long residentId);

    List<MedicationAdministration> findByAdministeredByUsernameOrderByAdministeredAtDescIdDesc(
            String administeredByUsername);

    List<MedicationAdministration> findByResidentIdAndAdministeredAtBetweenOrderByAdministeredAtDescIdDesc(
            Long residentId,
            LocalDateTime fromDateTime, LocalDateTime toDateTime);
}
