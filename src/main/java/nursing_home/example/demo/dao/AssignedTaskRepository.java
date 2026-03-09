package nursing_home.example.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.model.AssignedTask;

@Repository
public interface AssignedTaskRepository extends JpaRepository<AssignedTask, Long> {
    List<AssignedTask> findByAssignedToUsernameOrderByCreatedAtDesc(String assignedToUsername);

    long countByAssignedToUsernameAndStatus(String assignedToUsername, String status);
}
