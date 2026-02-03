package nursing_home.example.demo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.model.NursingHomeUser;

@Repository
public interface NursingHomeUserRepository extends JpaRepository<NursingHomeUser, Long> {

    Optional<NursingHomeUser> findByUsername(String username);

    
}
