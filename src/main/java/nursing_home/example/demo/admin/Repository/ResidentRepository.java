package nursing_home.example.demo.admin.Repository;

import java.util.Optional;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import java.sql.Date;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.admin.Model.Resident;

@Repository
public class ResidentRepository {
    private final JdbcTemplate jdbcTemplate;

    public ResidentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper maps SQL columns to your Resident object manually
    private final RowMapper<Resident> residentRowMapper = (rs, rowNum) -> {
        Resident resident = new Resident();
        resident.setId(rs.getLong("id"));
        resident.setName(rs.getString("name"));
        resident.setAge(rs.getInt("age"));
        resident.setGender(rs.getString("gender"));
        resident.setRoomNumber(rs.getInt("room_number"));
        Date admissionDate = rs.getDate("admission_date");
        resident.setAdmissionDate(admissionDate != null ? admissionDate.toLocalDate() : null);
        resident.setEmergencyContact(rs.getLong("emergency_contact"));
        return resident;
    };

    public Optional<Resident> findById(Long id) {
        String sql = "SELECT * FROM residents WHERE id = ?";
        List<Resident> result = jdbcTemplate.query(sql, residentRowMapper, id);
        return result.stream().findFirst();
    }

    public int save(Resident resident) {
        String sql = "INSERT INTO residents(name,age, gender,room_number, admission_date, emergency_contact) VALUES(?,?,?,?,?,?)";
        Date sqlAdmissionDate = resident.getAdmissionDate() != null ? Date.valueOf(resident.getAdmissionDate()) : null;
        return jdbcTemplate.update(sql,
                resident.getName(),
                resident.getAge(),
                resident.getGender(),
                resident.getRoomNumber(),
                sqlAdmissionDate,
                resident.getEmergencyContact());
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM activity_logs WHERE resident_id = ?", id);
        jdbcTemplate.update("DELETE FROM incident_reports WHERE resident_id = ?", id);
        jdbcTemplate.update("DELETE FROM medication_administration WHERE resident_id = ?", id);
        jdbcTemplate.update("DELETE FROM resident_medical_conditions WHERE resident_id = ?", id);
        jdbcTemplate.update("DELETE FROM Vitals WHERE resident_id = ?", id);
        jdbcTemplate.update("DELETE FROM resident_invoices WHERE resident_id = ?", id);
        jdbcTemplate.update("DELETE FROM assigned_tasks WHERE resident_id = ?", id);
        String sql = "DELETE FROM residents WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public List<Resident> findAll() {
        String sql = "SELECT * FROM residents";
        return jdbcTemplate.query(sql, residentRowMapper);
    }

    public List<Resident> findAllByOrderByNameAsc() {
        String sql = "SELECT * FROM residents ORDER BY name ASC";
        return jdbcTemplate.query(sql, residentRowMapper);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM residents";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
