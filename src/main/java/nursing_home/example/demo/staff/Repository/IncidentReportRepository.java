package nursing_home.example.demo.staff.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.admin.Model.NursingHomeUser;
import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.staff.model.IncidentReport;

@Repository
public class IncidentReportRepository {
    private final JdbcTemplate jdbcTemplate;

    public IncidentReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<IncidentReport> rowMapper = (rs, rowNum) -> {
        IncidentReport ir = new IncidentReport();
        ir.setId(rs.getLong("id"));
        long residentId = rs.getLong("resident_id"); if (!rs.wasNull()) { Resident r = new Resident(); r.setId(residentId); ir.setResident(r); }
        long staffId = rs.getLong("staff_id"); if (!rs.wasNull()) { NursingHomeUser u = new NursingHomeUser(); u.setId(staffId); ir.setStaff(u); }
        ir.setTitle(rs.getString("title"));
        ir.setDescription(rs.getString("description"));
        ir.setSeverity(rs.getString("severity"));
        Timestamp t = rs.getTimestamp("created_at"); if (t != null) ir.setCreatedAt(t.toLocalDateTime());
        return ir;
    };

    public List<IncidentReport> findByResidentIdOrderByCreatedAtDesc(Long residentId) {
        String sql = "SELECT * FROM incident_reports WHERE resident_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, residentId);
    }

    public List<IncidentReport> findByStaffIdOrderByCreatedAtDesc(Long staffId) {
        String sql = "SELECT * FROM incident_reports WHERE staff_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, staffId);
    }

    public Optional<IncidentReport> findById(Long id) {
        String sql = "SELECT * FROM incident_reports WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public IncidentReport save(IncidentReport ir) {
        if (ir.getId() == null) {
            String sql = "INSERT INTO incident_reports(resident_id, staff_id, title, description, severity, created_at) VALUES(?,?,?,?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setObject(1, ir.getResident() != null ? ir.getResident().getId() : null);
                ps.setObject(2, ir.getStaff() != null ? ir.getStaff().getId() : null);
                ps.setString(3, ir.getTitle());
                ps.setString(4, ir.getDescription());
                ps.setString(5, ir.getSeverity());
                ps.setTimestamp(6, ir.getCreatedAt() != null ? Timestamp.valueOf(ir.getCreatedAt()) : null);
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) ir.setId(k.longValue());
            return ir;
        }
        String sql = "UPDATE incident_reports SET resident_id=?, staff_id=?, title=?, description=?, severity=?, created_at=? WHERE id = ?";
        jdbcTemplate.update(sql, ir.getResident() != null ? ir.getResident().getId() : null, ir.getStaff() != null ? ir.getStaff().getId() : null, ir.getTitle(), ir.getDescription(), ir.getSeverity(), ir.getCreatedAt() != null ? Timestamp.valueOf(ir.getCreatedAt()) : null, ir.getId());
        return ir;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM incident_reports WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
     public List<IncidentReport> findAll() {
        String sql = "SELECT * FROM incident_reports";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
