package nursing_home.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.model.Vitals;

@Repository
public interface VitalsRepository extends JpaRepository<Vitals,Long> {
    
}
