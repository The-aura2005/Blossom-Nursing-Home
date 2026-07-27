package nursing_home.example.demo.staff.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.staff.model.MedicationAdministration;

@Repository
public class MedicationAdministrationRepository {
    private final JdbcTemplate jdbcTemplate;

    public MedicationAdministrationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MedicationAdministration> rowMapper = (rs, rowNum) -> {
        MedicationAdministration m = new MedicationAdministration();
        m.setId(rs.getLong("id"));
        long residentId = rs.getLong("resident_id");
        if (!rs.wasNull()) {
            Resident r = new Resident();
            r.setId(residentId);
            m.setResident(r);
        }
        m.setMedicationName(rs.getString("medication_name"));
        m.setDosage(rs.getString("dosage"));
        m.setAdministeredByUsername(rs.getString("administered_by_username"));
        m.setNotes(rs.getString("notes"));
        Timestamp t = rs.getTimestamp("administered_at");
        if (t != null)
            m.setAdministeredAt(t.toLocalDateTime());
        return m;
    };

    public List<MedicationAdministration> findAllWithResidentOrderByAdministeredAtDescIdDesc() {
        String sql = "SELECT * FROM medication_administration ORDER BY administered_at DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<MedicationAdministration> findByResidentIdOrderByAdministeredAtDescIdDesc(Long residentId) {
        String sql = "SELECT * FROM medication_administration WHERE resident_id = ? ORDER BY administered_at DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, residentId);
    }

    public List<MedicationAdministration> findByAdministeredByUsernameOrderByAdministeredAtDescIdDesc(String username) {
        String sql = "SELECT * FROM medication_administration WHERE administered_by_username = ? ORDER BY administered_at DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, username);
    }

    public List<MedicationAdministration> findByResidentIdAndAdministeredAtBetweenOrderByAdministeredAtDescIdDesc(
            Long residentId,
            java.time.LocalDateTime fromDateTime, java.time.LocalDateTime toDateTime) {
        String sql = "SELECT * FROM medication_administration WHERE resident_id = ? AND administered_at BETWEEN ? AND ? ORDER BY administered_at DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, residentId, Timestamp.valueOf(fromDateTime),
                Timestamp.valueOf(toDateTime));
    }

    public Optional<MedicationAdministration> findById(Long id) {
        String sql = "SELECT * FROM medication_administration WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public MedicationAdministration save(MedicationAdministration m) {
        if (m.getId() == null) {
            String sql = "INSERT INTO medication_administration(resident_id, medication_name, dosage, administered_by_username, notes, administered_at) VALUES(?,?,?,?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setObject(1, m.getResident() != null ? m.getResident().getId() : null);
                ps.setString(2, m.getMedicationName());
                ps.setString(3, m.getDosage());
                ps.setString(4, m.getAdministeredByUsername());
                ps.setString(5, m.getNotes());
                ps.setTimestamp(6, m.getAdministeredAt() != null ? Timestamp.valueOf(m.getAdministeredAt()) : null);
                return ps;
            }, kh);
            Number k = kh.getKey();
            if (k != null)
                m.setId(k.longValue());
            return m;
        }
        String sql = "UPDATE medication_administration SET resident_id=?, medication_name=?, dosage=?, administered_by_username=?, notes=?, administered_at=? WHERE id = ?";
        jdbcTemplate.update(sql, m.getResident() != null ? m.getResident().getId() : null, m.getMedicationName(),
                m.getDosage(), m.getAdministeredByUsername(), m.getNotes(),
                m.getAdministeredAt() != null ? Timestamp.valueOf(m.getAdministeredAt()) : null, m.getId());
        return m;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM medication_administration WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM medication_administration";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
