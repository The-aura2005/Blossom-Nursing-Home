package nursing_home.example.demo.inventory.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.inventory.model.InventoryActivityLog;

@Repository("inventoryActivityLogRepository")
public class ActivityLogRepository {
    private final JdbcTemplate jdbcTemplate;

    public ActivityLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<InventoryActivityLog> rowMapper = (rs, rowNum) -> {
        InventoryActivityLog l = new InventoryActivityLog();
        l.setId(rs.getLong("id"));
        l.setAction(rs.getString("action"));
        l.setItemName(rs.getString("item_name"));
        l.setQuantityChanged(rs.getObject("quantity_changed") != null ? rs.getInt("quantity_changed") : null);
        l.setStaffUsername(rs.getString("staff_username"));
        long residentId = rs.getLong("resident_id"); if (!rs.wasNull()) l.setResidentId(residentId);
        l.setResidentName(rs.getString("resident_name"));
        l.setNotes(rs.getString("notes"));
        Timestamp t = rs.getTimestamp("timestamp"); if (t != null) l.setTimestamp(t.toLocalDateTime());
        return l;
    };

    public List<InventoryActivityLog> findAllByOrderByTimestampDesc() {
        String sql = "SELECT * FROM inventory_activity_logs ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<InventoryActivityLog> findById(Long id) {
        String sql = "SELECT * FROM inventory_activity_logs WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public InventoryActivityLog save(InventoryActivityLog l) {
        if (l.getId() == null) {
            String sql = "INSERT INTO inventory_activity_logs(action, item_name, quantity_changed, staff_username, resident_id, resident_name, notes, timestamp) VALUES(?,?,?,?,?,?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setString(1, l.getAction());
                ps.setString(2, l.getItemName());
                if (l.getQuantityChanged() != null) ps.setInt(3, l.getQuantityChanged()); else ps.setNull(3, java.sql.Types.INTEGER);
                ps.setString(4, l.getStaffUsername());
                ps.setObject(5, l.getResidentId());
                ps.setString(6, l.getResidentName());
                ps.setString(7, l.getNotes());
                ps.setTimestamp(8, l.getTimestamp() != null ? Timestamp.valueOf(l.getTimestamp()) : null);
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) l.setId(k.longValue());
            return l;
        }
        String sql = "UPDATE inventory_activity_logs SET action=?, item_name=?, quantity_changed=?, staff_username=?, resident_id=?, resident_name=?, notes=?, timestamp=? WHERE id = ?";
        jdbcTemplate.update(sql, l.getAction(), l.getItemName(), l.getQuantityChanged(), l.getStaffUsername(), l.getResidentId(), l.getResidentName(), l.getNotes(), l.getTimestamp() != null ? Timestamp.valueOf(l.getTimestamp()) : null, l.getId());
        return l;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM inventory_activity_logs WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
