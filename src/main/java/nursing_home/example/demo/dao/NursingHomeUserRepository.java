package nursing_home.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.model.NursingHomeUser;
import nursing_home.example.demo.model.NursingHomeUserRole;

@Repository
public interface NursingHomeUserRepository extends JpaRepository<NursingHomeUser, Long> {

    NursingHomeUser findByUsername(String username);

    List<NursingHomeUser> findByNursingHomeUserRole(NursingHomeUserRole nursingHomeUserRole);

}
