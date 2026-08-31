package nursing_home.example.demo.staff.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.admin.Model.Resident;
import nursing_home.example.demo.staff.model.Vitals;

@Repository
public class VitalsRepository {
    private final JdbcTemplate jdbcTemplate;

    public VitalsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Vitals> rowMapper = (rs, rowNum) -> {
        Vitals v = new Vitals();
        v.setId(rs.getLong("id"));
        v.setTemperature(rs.getDouble("temperature"));
        v.setBloodPressure(rs.getString("blood_pressure"));
        v.setWeight(rs.getInt("weight"));
        Date d = rs.getDate("date_recorded");
        if (d != null)
            v.setDateRecorded(d.toLocalDate());
        v.setNotes(rs.getString("notes"));
        v.setRecordedByUsername(rs.getString("recorded_by_username"));
        long residentId = rs.getLong("resident_id");
        if (!rs.wasNull()) {
            Resident r = new Resident();
            r.setId(residentId);
            v.setResident(r);
        }
        return v;
    };

    public List<Vitals> findAllByOrderByDateRecordedDescIdDesc() {
        String sql = "SELECT * FROM Vitals ORDER BY date_recorded DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<Vitals> findByResidentIdAndDateRecordedBetweenOrderByDateRecordedDescIdDesc(Long residentId,
            java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        String sql = "SELECT * FROM Vitals WHERE resident_id = ? AND date_recorded BETWEEN ? AND ? ORDER BY date_recorded DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, residentId, Date.valueOf(fromDate), Date.valueOf(toDate));
    }

    public List<Vitals> findByResidentIdOrderByDateRecordedDescIdDesc(Long residentId) {
        String sql = "SELECT * FROM Vitals WHERE resident_id = ? ORDER BY date_recorded DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, residentId);
    }

    public List<Vitals> findByResidentIdInOrderByDateRecordedDescIdDesc(List<Long> residentIds) {
        if (residentIds == null || residentIds.isEmpty())
            return List.of();
        StringJoiner sj = new StringJoiner(",", "(", ")");
        for (int i = 0; i < residentIds.size(); i++)
            sj.add("?");
        String sql = "SELECT * FROM Vitals WHERE resident_id IN " + sj.toString()
                + " ORDER BY date_recorded DESC, id DESC";
        Object[] params = residentIds.toArray();
        return jdbcTemplate.query(sql, rowMapper, params);
    }

    public List<Vitals> findByRecordedByUsernameOrderByDateRecordedDescIdDesc(String recordedByUsername) {
        String sql = "SELECT * FROM Vitals WHERE recorded_by_username = ? ORDER BY date_recorded DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, recordedByUsername);
    }

    public Optional<Vitals> findById(Long id) {
        String sql = "SELECT * FROM Vitals WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public Vitals save(Vitals v) {
        if (v.getId() == null) {
            String sql = "INSERT INTO Vitals(temperature, blood_pressure, weight, date_recorded, notes, recorded_by_username, resident_id) VALUES(?,?,?,?,?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setDouble(1, v.getTemperature());
                ps.setString(2, v.getBloodPressure());
                ps.setInt(3, v.getWeight());
                ps.setDate(4, v.getDateRecorded() != null ? Date.valueOf(v.getDateRecorded()) : null);
                ps.setString(5, v.getNotes());
                ps.setString(6, v.getRecordedByUsername());
                if (v.getResident() != null)
                    ps.setLong(7, v.getResident().getId());
                else
                    ps.setNull(7, java.sql.Types.BIGINT);
                return ps;
            }, kh);
            Number key = kh.getKey();
            if (key != null)
                v.setId(key.longValue());
            return v;
        }
        String sql = "UPDATE Vitals SET temperature=?, blood_pressure=?, weight=?, date_recorded=?, notes=?, recorded_by_username=?, resident_id=? WHERE id = ?";
        jdbcTemplate.update(sql, v.getTemperature(), v.getBloodPressure(), v.getWeight(),
                v.getDateRecorded() != null ? Date.valueOf(v.getDateRecorded()) : null,
                v.getNotes(), v.getRecordedByUsername(), v.getResident() != null ? v.getResident().getId() : null,
                v.getId());
        return v;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM Vitals WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM Vitals";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
