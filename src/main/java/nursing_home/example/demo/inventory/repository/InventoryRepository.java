package nursing_home.example.demo.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.inventory.model.InventoryItem;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    @Query("SELECT i FROM InventoryItem i WHERE i.quantity <= i.minThreshold AND i.quantity > 0")
    List<InventoryItem> findLowStockItems();

    List<InventoryItem> findByQuantity(int quantity);
}
