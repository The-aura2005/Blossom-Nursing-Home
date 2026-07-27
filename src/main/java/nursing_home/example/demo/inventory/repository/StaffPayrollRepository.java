package nursing_home.example.demo.inventory.repository;

import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.accountant.model.PayrollStatus;
import nursing_home.example.demo.accountant.model.StaffPayroll;
import nursing_home.example.demo.staff.model.Staff;

@Repository
public class StaffPayrollRepository {
    private final JdbcTemplate jdbcTemplate;

    public StaffPayrollRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<StaffPayroll> rowMapper = (rs, rowNum) -> {
        StaffPayroll p = new StaffPayroll();
        p.setId(rs.getLong("id"));
        long staffId = rs.getLong("staff_id"); if (!rs.wasNull()) { Staff s = new Staff(); s.setId(staffId); p.setStaff(s); }
        p.setPayrollMonth(rs.getString("payroll_month"));
        p.setSalaryAmount(rs.getBigDecimal("salary_amount"));
        Date pd = rs.getDate("payroll_date"); if (pd != null) p.setPayrollDate(pd.toLocalDate());
        String st = rs.getString("status"); if (st != null) p.setStatus(PayrollStatus.valueOf(st));
        Timestamp t = rs.getTimestamp("paid_at"); if (t != null) p.setPaidAt(t.toLocalDateTime());
        return p;
    };

    public List<StaffPayroll> findAllByOrderByPayrollDateDescIdDesc() {
        String sql = "SELECT * FROM staff_payroll ORDER BY payroll_date DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<StaffPayroll> findByPayrollDateBetweenOrderByPayrollDateDescIdDesc(LocalDate fromDate, LocalDate toDate) {
        String sql = "SELECT * FROM staff_payroll WHERE payroll_date BETWEEN ? AND ? ORDER BY payroll_date DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, Date.valueOf(fromDate), Date.valueOf(toDate));
    }

    public boolean existsByStaffIdAndPayrollMonth(Long staffId, String payrollMonth) {
        String sql = "SELECT COUNT(*) FROM staff_payroll WHERE staff_id = ? AND payroll_month = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, staffId, payrollMonth);
        return count != null && count > 0;
    }

    public long countByStatus(PayrollStatus status) {
        String sql = "SELECT COUNT(*) FROM staff_payroll WHERE status = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, status.name());
    }

    public Optional<StaffPayroll> findById(Long id) {
        String sql = "SELECT * FROM staff_payroll WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public StaffPayroll save(StaffPayroll p) {
        if (p.getId() == null) {
            String sql = "INSERT INTO staff_payroll(staff_id, payroll_month, salary_amount, payroll_date, status, paid_at) VALUES(?,?,?,?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setObject(1, p.getStaff() != null ? p.getStaff().getId() : null);
                ps.setString(2, p.getPayrollMonth());
                ps.setBigDecimal(3, p.getSalaryAmount());
                ps.setDate(4, p.getPayrollDate() != null ? Date.valueOf(p.getPayrollDate()) : null);
                ps.setString(5, p.getStatus() != null ? p.getStatus().name() : null);
                ps.setTimestamp(6, p.getPaidAt() != null ? Timestamp.valueOf(p.getPaidAt()) : null);
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) p.setId(k.longValue());
            return p;
        }
        String sql = "UPDATE staff_payroll SET staff_id=?, payroll_month=?, salary_amount=?, payroll_date=?, status=?, paid_at=? WHERE id = ?";
        jdbcTemplate.update(sql, p.getStaff() != null ? p.getStaff().getId() : null, p.getPayrollMonth(), p.getSalaryAmount(), p.getPayrollDate() != null ? Date.valueOf(p.getPayrollDate()) : null, p.getStatus() != null ? p.getStatus().name() : null, p.getPaidAt() != null ? Timestamp.valueOf(p.getPaidAt()) : null, p.getId());
        return p;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM staff_payroll WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
