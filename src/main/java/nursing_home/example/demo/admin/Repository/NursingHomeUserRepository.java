package nursing_home.example.demo.admin.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.admin.Model.NursingHomeUser;
import nursing_home.example.demo.admin.Model.NursingHomeUserRole;

@Repository
public interface NursingHomeUserRepository extends JpaRepository<NursingHomeUser, Long> {

    NursingHomeUser findByUsername(String username);

    List<NursingHomeUser> findByNursingHomeUserRole(NursingHomeUserRole nursingHomeUserRole);

}
