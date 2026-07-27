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
        resident.setAdmissionDate(rs.getDate("admission_date").toLocalDate());
        resident.setEmergencyContact(rs.getLong("emergency_contact"));
        resident.setStatus(rs.getString("status"));
        return resident;
    };

    public Optional<Resident> findById(Long id) {
        String sql = "SELECT * FROM resident WHERE id = ?";
        List<Resident> result = jdbcTemplate.query(sql, residentRowMapper, id);
        return result.stream().findFirst();
    }

    public int save(Resident resident) {
        String sql = "INSERT INTO resident(name,age, gender,room_number, admission_date, emergency_contact,status) VALUES(?,?,?,?,?,?,?)";
        Date sqlAdmissionDate = resident.getAdmissionDate() != null ? Date.valueOf(resident.getAdmissionDate()) : null;
        return jdbcTemplate.update(sql,
                resident.getName(),
                resident.getAge(),
                resident.getGender(),
                resident.getRoomNumber(),
                sqlAdmissionDate,
                resident.getEmergencyContact(),
                resident.getStatus());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM resident WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public List<Resident> findAll() {
        String sql = "SELECT * FROM resident";
        return jdbcTemplate.query(sql, residentRowMapper);
    }

    public List<Resident> findAllByOrderByNameAsc() {
        String sql = "SELECT * FROM resident ORDER BY name ASC";
        return jdbcTemplate.query(sql, residentRowMapper);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM resident";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
