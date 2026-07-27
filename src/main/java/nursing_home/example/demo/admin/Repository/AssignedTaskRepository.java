package nursing_home.example.demo.admin.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.admin.Model.AssignedTask;
import nursing_home.example.demo.admin.Model.NursingHomeUserRole;

@Repository
public class AssignedTaskRepository {
    private final JdbcTemplate jdbcTemplate;

    public AssignedTaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<AssignedTask> rowMapper = (rs, rowNum) -> {
        AssignedTask t = new AssignedTask();
        t.setId(rs.getLong("id"));
        t.setTitle(rs.getString("title"));
        long residentId = rs.getLong("resident_id");
        if (!rs.wasNull()) {
            t.setResidentId(residentId);
        }
        t.setResidentName(rs.getString("resident_name"));
        t.setRoomNumber(rs.getString("room_number"));
        t.setScheduledTime(rs.getString("scheduled_time"));
        t.setPriority(rs.getString("priority"));
        t.setStatus(rs.getString("status"));
        t.setAssignedToUsername(rs.getString("assigned_to_username"));
        t.setAssignedBy(rs.getString("assigned_by"));
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            t.setCreatedAt(createdTs.toLocalDateTime());
        }
        Timestamp completedTs = rs.getTimestamp("completed_at");
        if (completedTs != null) {
            t.setCompletedAt(completedTs.toLocalDateTime());
        }
        return t;
    };

    public List<AssignedTask> findAll() {
        String sql = "SELECT * FROM assigned_tasks ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<AssignedTask> findById(Long id) {
        String sql = "SELECT * FROM assigned_tasks WHERE id = ?";
        List<AssignedTask> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.stream().findFirst();
    }

    public List<AssignedTask> findByAssignedToUsernameOrderByCreatedAtDesc(String username) {
        String sql = "SELECT * FROM assigned_tasks WHERE assigned_to_username = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, username);
    }

    public List<AssignedTask> findByNursingHomeUserRole(NursingHomeUserRole role) {
        String sql = "SELECT at.* FROM assigned_tasks at JOIN nursing_home_user u ON at.assigned_to_username = u.username WHERE u.nursing_home_user_role = ? ORDER BY at.created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, role.name());
    }

    public long countByAssignedToUsernameAndStatus(String username, String status) {
        String sql = "SELECT COUNT(*) FROM assigned_tasks WHERE assigned_to_username = ? AND status = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, username, status);
    }

    public AssignedTask save(AssignedTask task) {
        if (task.getId() == null) {
            String sql = "INSERT INTO assigned_tasks(title, resident_id, resident_name, room_number, scheduled_time, priority, status, assigned_to_username, assigned_by, created_at, completed_at) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[] { "id" });
                ps.setString(1, task.getTitle());
                if (task.getResidentId() != null) ps.setLong(2, task.getResidentId()); else ps.setNull(2, java.sql.Types.BIGINT);
                ps.setString(3, task.getResidentName());
                ps.setString(4, task.getRoomNumber());
                ps.setString(5, task.getScheduledTime());
                ps.setString(6, task.getPriority());
                ps.setString(7, task.getStatus());
                ps.setString(8, task.getAssignedToUsername());
                ps.setString(9, task.getAssignedBy());
                ps.setTimestamp(10, task.getCreatedAt() != null ? Timestamp.valueOf(task.getCreatedAt()) : null);
                ps.setTimestamp(11, task.getCompletedAt() != null ? Timestamp.valueOf(task.getCompletedAt()) : null);
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key != null) {
                task.setId(key.longValue());
            }
            return task;
        } else {
            String sql = "UPDATE assigned_tasks SET title=?, resident_id=?, resident_name=?, room_number=?, scheduled_time=?, priority=?, status=?, assigned_to_username=?, assigned_by=?, created_at=?, completed_at=? WHERE id = ?";
            jdbcTemplate.update(sql,
                    task.getTitle(),
                    task.getResidentId(),
                    task.getResidentName(),
                    task.getRoomNumber(),
                    task.getScheduledTime(),
                    task.getPriority(),
                    task.getStatus(),
                    task.getAssignedToUsername(),
                    task.getAssignedBy(),
                    task.getCreatedAt() != null ? Timestamp.valueOf(task.getCreatedAt()) : null,
                    task.getCompletedAt() != null ? Timestamp.valueOf(task.getCompletedAt()) : null,
                    task.getId());
            return task;
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM assigned_tasks WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}

