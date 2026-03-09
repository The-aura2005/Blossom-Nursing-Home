package nursing_home.example.demo.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.model.Vitals;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals, Long> {
    List<Vitals> findAllByOrderByDateRecordedDescIdDesc();

    List<Vitals> findByResidentIdAndDateRecordedBetweenOrderByDateRecordedDescIdDesc(Long residentId,
            LocalDate fromDate,
            LocalDate toDate);

    List<Vitals> findByResidentIdOrderByDateRecordedDescIdDesc(Long residentId);
}
