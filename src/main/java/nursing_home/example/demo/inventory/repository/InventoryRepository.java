package nursing_home.example.demo.inventory.repository;


import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.inventory.model.InventoryItem;
import nursing_home.example.demo.inventory.model.Supplier;

@Repository
public class InventoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public InventoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<InventoryItem> rowMapper = (rs, rowNum) -> {
        InventoryItem i = new InventoryItem();
        i.setId(rs.getLong("id"));
        i.setName(rs.getString("name"));
        i.setCategory(rs.getString("category"));
        i.setQuantity(rs.getInt("quantity"));
        i.setUnit(rs.getString("unit"));
        i.setMinThreshold(rs.getInt("min_threshold"));
        i.setPurchaseCost(rs.getBigDecimal("purchase_cost"));
        long supplierId = rs.getLong("supplier_id");
        if (!rs.wasNull()) { Supplier s = new Supplier(); s.setId(supplierId); i.setSupplier(s); }
        java.sql.Timestamp ts = rs.getTimestamp("created_at"); if (ts != null) i.setCreatedAt(ts.toLocalDateTime());
        return i;
    };

    public List<InventoryItem> findAll() {
        String sql = "SELECT * FROM inventory_items";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<InventoryItem> findById(Long id) {
        String sql = "SELECT * FROM inventory_items WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public List<InventoryItem> findLowStockItems() {
        String sql = "SELECT * FROM inventory_items WHERE quantity <= min_threshold AND quantity > 0";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<InventoryItem> findByQuantity(int quantity) {
        String sql = "SELECT * FROM inventory_items WHERE quantity = ?";
        return jdbcTemplate.query(sql, rowMapper, quantity);
    }

    public InventoryItem save(InventoryItem i) {
        if (i.getId() == null) {
            String sql = "INSERT INTO inventory_items(name, category, quantity, unit, min_threshold, purchase_cost, supplier_id, created_at) VALUES(?,?,?,?,?,?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setString(1, i.getName());
                ps.setString(2, i.getCategory());
                ps.setInt(3, i.getQuantity());
                ps.setString(4, i.getUnit());
                ps.setInt(5, i.getMinThreshold());
                ps.setBigDecimal(6, i.getPurchaseCost());
                ps.setObject(7, i.getSupplier() != null ? i.getSupplier().getId() : null);
                ps.setTimestamp(8, i.getCreatedAt() != null ? Timestamp.valueOf(i.getCreatedAt()) : null);
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) i.setId(k.longValue());
            return i;
        }
        String sql = "UPDATE inventory_items SET name=?, category=?, quantity=?, unit=?, min_threshold=?, purchase_cost=?, supplier_id=?, created_at=? WHERE id = ?";
        jdbcTemplate.update(sql, i.getName(), i.getCategory(), i.getQuantity(), i.getUnit(), i.getMinThreshold(), i.getPurchaseCost(), i.getSupplier() != null ? i.getSupplier().getId() : null, i.getCreatedAt() != null ? Timestamp.valueOf(i.getCreatedAt()) : null, i.getId());
        return i;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM inventory_items WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
