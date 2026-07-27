package nursing_home.example.demo.staff.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.staff.model.Staff;

@Repository
public class StaffRepository {
    private final JdbcTemplate jdbcTemplate;

    public StaffRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Staff> rowMapper = (rs, rowNum) -> {
        Staff s = new Staff();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        s.setPhoneNumber(rs.getInt("phone_number"));
        s.setStatus(rs.getString("status"));
        return s;
    };

    public Optional<Staff> findById(Long id) {
        String sql = "SELECT * FROM Staff WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public List<Staff> findAll() {
        String sql = "SELECT * FROM Staff";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Staff save(Staff s) {
        if (s.getId() == null) {
            String sql = "INSERT INTO Staff(name,email,phone_number,status) VALUES(?,?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setString(1, s.getName());
                ps.setString(2, s.getEmail());
                ps.setInt(3, s.getPhoneNumber());
                ps.setString(4, s.getStatus());
                return ps;
            }, kh);
            Number key = kh.getKey();
            if (key != null)
                s.setId(key.longValue());
            return s;
        }
        String sql = "UPDATE Staff SET name=?, email=?, phone_number=?, status=? WHERE id = ?";
        jdbcTemplate.update(sql, s.getName(), s.getEmail(), s.getPhoneNumber(), s.getStatus(), s.getId());
        return s;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM Staff WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM Staff";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
