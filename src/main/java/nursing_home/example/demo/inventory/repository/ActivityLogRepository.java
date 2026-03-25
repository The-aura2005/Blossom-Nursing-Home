package nursing_home.example.demo.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.inventory.model.InventoryActivityLog;

@Repository("inventoryActivityLogRepository")
public interface ActivityLogRepository extends JpaRepository<InventoryActivityLog, Long> {
    List<InventoryActivityLog> findAllByOrderByTimestampDesc();
}
