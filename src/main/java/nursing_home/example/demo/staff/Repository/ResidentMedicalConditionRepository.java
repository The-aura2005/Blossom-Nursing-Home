package nursing_home.example.demo.staff.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.staff.model.ResidentMedicalCondition;

@Repository
public class ResidentMedicalConditionRepository {
    private final JdbcTemplate jdbcTemplate;

    public ResidentMedicalConditionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ResidentMedicalCondition> rowMapper = (rs, rowNum) -> {
        ResidentMedicalCondition c = new ResidentMedicalCondition();
        c.setId(rs.getLong("id"));
        Resident r = new Resident();
        long residentId = rs.getLong("resident_id");
        if (!rs.wasNull())
            r.setId(residentId);
        c.setResident(r);
        String ct = rs.getString("condition_type");
        if (ct != null)
            c.setConditionType(ResidentMedicalCondition.ConditionType.valueOf(ct));
        c.setDescription(rs.getString("description"));
        return c;
    };

    public List<ResidentMedicalCondition> findByResidentIdOrderByConditionTypeAscDescriptionAsc(Long residentId) {
        String sql = "SELECT * FROM resident_medical_conditions WHERE resident_id = ? ORDER BY condition_type ASC, description ASC";
        return jdbcTemplate.query(sql, rowMapper, residentId);
    }

    public Optional<ResidentMedicalCondition> findById(Long id) {
        String sql = "SELECT * FROM resident_medical_conditions WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public ResidentMedicalCondition save(ResidentMedicalCondition c) {
        if (c.getId() == null) {
            String sql = "INSERT INTO resident_medical_conditions(resident_id, condition_type, description) VALUES(?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setLong(1, c.getResident() != null ? c.getResident().getId() : null);
                ps.setString(2, c.getConditionType() != null ? c.getConditionType().name() : null);
                ps.setString(3, c.getDescription());
                return ps;
            }, kh);
            Number key = kh.getKey();
            if (key != null)
                c.setId(key.longValue());
            return c;
        }
        String sql = "UPDATE resident_medical_conditions SET resident_id=?, condition_type=?, description=? WHERE id = ?";
        jdbcTemplate.update(sql, c.getResident() != null ? c.getResident().getId() : null,
                c.getConditionType() != null ? c.getConditionType().name() : null,
                c.getDescription(), c.getId());
        return c;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM resident_medical_conditions WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM resident_medical_conditions";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
