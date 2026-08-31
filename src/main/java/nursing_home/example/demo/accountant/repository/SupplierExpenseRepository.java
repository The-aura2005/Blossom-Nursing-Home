package nursing_home.example.demo.accountant.repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.accountant.model.SupplierExpense;

@Repository
public class SupplierExpenseRepository {
    private final JdbcTemplate jdbcTemplate;

    public SupplierExpenseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SupplierExpense> rowMapper = (rs, rowNum) -> {
        SupplierExpense e = new SupplierExpense();
        e.setId(rs.getLong("id"));
        e.setSupplierName(rs.getString("supplier_name"));
        e.setItemName(rs.getString("item_name"));
        e.setCost(rs.getBigDecimal("cost"));
        java.sql.Date d = rs.getDate("expense_date"); if (d != null) e.setExpenseDate(d.toLocalDate());
        return e;
    };

    public List<SupplierExpense> findAllByOrderByExpenseDateDescIdDesc() {
        String sql = "SELECT * FROM supplier_expenses ORDER BY expense_date DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<SupplierExpense> findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        String sql = "SELECT * FROM supplier_expenses WHERE expense_date BETWEEN ? AND ? ORDER BY expense_date DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, Date.valueOf(fromDate), Date.valueOf(toDate));
    }

    public Optional<SupplierExpense> findById(Long id) {
        String sql = "SELECT * FROM supplier_expenses WHERE id = ?";
        //you have to use stream convert the rs to list then use findfirts to get the first element
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public SupplierExpense save(SupplierExpense e) {
        if (e.getId() == null) {
            String sql = "INSERT INTO supplier_expenses(supplier_name, item_name, cost, expense_date) VALUES(?,?,?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setString(1, e.getSupplierName());
                ps.setString(2, e.getItemName());
                ps.setBigDecimal(3, e.getCost());
                ps.setDate(4, e.getExpenseDate() != null ? Date.valueOf(e.getExpenseDate()) : null);
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) e.setId(k.longValue());
            return e;
        }
        String sql = "UPDATE supplier_expenses SET supplier_name=?, item_name=?, cost=?, expense_date=? WHERE id = ?";
        jdbcTemplate.update(sql, e.getSupplierName(), e.getItemName(), e.getCost(), e.getExpenseDate() != null ? Date.valueOf(e.getExpenseDate()) : null, e.getId());
        return e;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM supplier_expenses WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
