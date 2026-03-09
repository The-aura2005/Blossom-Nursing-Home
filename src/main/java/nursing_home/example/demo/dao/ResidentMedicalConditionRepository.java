package nursing_home.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.model.ResidentMedicalCondition;

@Repository
public interface ResidentMedicalConditionRepository extends JpaRepository<ResidentMedicalCondition, Long> {
    List<ResidentMedicalCondition> findByResidentIdOrderByConditionTypeAscDescriptionAsc(Long residentId);
}
