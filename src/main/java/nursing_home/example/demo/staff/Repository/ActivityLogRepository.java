package nursing_home.example.demo.staff.Repository;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.staff.model.ActivityLog;

@Repository
public class ActivityLogRepository {
    private final JdbcTemplate jdbcTemplate;

    public ActivityLogRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ActivityLog> rowMapper = (rs, rowNum) -> {
        ActivityLog a = new ActivityLog();
        a.setId(rs.getLong("id"));
        long residentId = rs.getLong("resident_id");
        if (!rs.wasNull()) {
            Resident r = new Resident();
            r.setId(residentId);
            a.setResident(r);
        }
        a.setActivityType(rs.getString("activity_type"));
        a.setNotes(rs.getString("notes"));
        Date d = rs.getDate("activity_date");
        if (d != null)
            a.setActivityDate(d.toLocalDate());
        Time tm = rs.getTime("activity_time");
        if (tm != null)
            a.setActivityTime(tm.toLocalTime());
        a.setLoggedByUsername(rs.getString("logged_by_username"));
        Timestamp t = rs.getTimestamp("created_at");
        if (t != null)
            a.setCreatedAt(t.toLocalDateTime());
        return a;
    };

    public List<ActivityLog> findActivityHistoryWithResident() {
        String sql = "SELECT * FROM activity_logs ORDER BY activity_date DESC, activity_time DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<ActivityLog> findByResidentIdOrderByActivityDateDescActivityTimeDescIdDesc(Long residentId) {
        String sql = "SELECT * FROM activity_logs WHERE resident_id = ? ORDER BY activity_date DESC, activity_time DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, residentId);
    }

    public List<ActivityLog> findByResidentIdAndActivityDateBetweenOrderByActivityDateDescActivityTimeDescIdDesc(
            Long residentId, java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        String sql = "SELECT * FROM activity_logs WHERE resident_id = ? AND activity_date BETWEEN ? AND ? ORDER BY activity_date DESC, activity_time DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, residentId, Date.valueOf(fromDate), Date.valueOf(toDate));
    }

    public List<ActivityLog> findByLoggedByUsernameOrderByActivityDateDescActivityTimeDescIdDesc(String username) {
        String sql = "SELECT * FROM activity_logs WHERE logged_by_username = ? ORDER BY activity_date DESC, activity_time DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, username);
    }

    public Optional<ActivityLog> findById(Long id) {
        String sql = "SELECT * FROM activity_logs WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public ActivityLog save(ActivityLog a) {
        if (a.getId() == null) {
            String sql = "INSERT INTO activity_logs(resident_id, activity_type, notes, activity_date, activity_time, logged_by_username, created_at) VALUES(?,?,?,?,?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setObject(1, a.getResident() != null ? a.getResident().getId() : null);
                ps.setString(2, a.getActivityType());
                ps.setString(3, a.getNotes());
                ps.setDate(4, a.getActivityDate() != null ? Date.valueOf(a.getActivityDate()) : null);
                ps.setTime(5, a.getActivityTime() != null ? Time.valueOf(a.getActivityTime()) : null);
                ps.setString(6, a.getLoggedByUsername());
                ps.setTimestamp(7, a.getCreatedAt() != null ? Timestamp.valueOf(a.getCreatedAt()) : null);
                return ps;
            }, kh);
            Number k = kh.getKey();
            if (k != null)
                a.setId(k.longValue());
            return a;
        }
        String sql = "UPDATE activity_logs SET resident_id=?, activity_type=?, notes=?, activity_date=?, activity_time=?, logged_by_username=?, created_at=? WHERE id = ?";
        jdbcTemplate.update(sql, a.getResident() != null ? a.getResident().getId() : null, a.getActivityType(),
                a.getNotes(), a.getActivityDate() != null ? Date.valueOf(a.getActivityDate()) : null,
                a.getActivityTime() != null ? Time.valueOf(a.getActivityTime()) : null, a.getLoggedByUsername(),
                a.getCreatedAt() != null ? Timestamp.valueOf(a.getCreatedAt()) : null, a.getId());
        return a;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM activity_logs WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM activity_logs";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
