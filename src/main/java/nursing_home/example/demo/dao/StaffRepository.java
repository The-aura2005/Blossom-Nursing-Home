package nursing_home.example.demo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nursing_home.example.demo.model.Staff;

public interface StaffRepository extends JpaRepository<Staff,Long>{
    Optional<Staff> findById(Long id);
    
}
