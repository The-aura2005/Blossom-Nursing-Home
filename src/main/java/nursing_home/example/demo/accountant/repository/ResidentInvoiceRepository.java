package nursing_home.example.demo.accountant.repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.accountant.controller.InvoiceStatus;
import nursing_home.example.demo.accountant.model.ResidentInvoice;
import nursing_home.example.demo.admin.Model.Resident;

@Repository
public class ResidentInvoiceRepository {
    private final JdbcTemplate jdbcTemplate;

    public ResidentInvoiceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
     //rowmapper is used to map the rs of a query to resident invoice object
    private final RowMapper<ResidentInvoice> rowMapper = (rs, rowNum) -> {
        ResidentInvoice r = new ResidentInvoice();
        r.setId(rs.getLong("id"));
        long residentId = rs.getLong("resident_id"); if (!rs.wasNull()) { Resident res = new Resident(); res.setId(residentId); r.setResident(res); }
        r.setDescription(rs.getString("description"));
        r.setAmount(rs.getBigDecimal("amount"));
        java.sql.Date inv = rs.getDate("invoice_date"); if (inv != null) r.setInvoiceDate(inv.toLocalDate());
        java.sql.Date due = rs.getDate("due_date"); if (due != null) r.setDueDate(due.toLocalDate());
        String st = rs.getString("status"); if (st != null) r.setStatus(InvoiceStatus.valueOf(st));
        Timestamp paid = rs.getTimestamp("paid_at"); if (paid != null) r.setPaidAt(paid.toLocalDateTime());
        r.setPaymentMethod(rs.getString("payment_method"));
        return r;
    };

    public List<ResidentInvoice> findAllByOrderByInvoiceDateDescIdDesc() {
        String sql = "SELECT * FROM resident_invoices ORDER BY invoice_date DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }
    //selcts resident invoices between two dates and orders them by invoice date and id in descending order

    public List<ResidentInvoice> findByInvoiceDateBetweenOrderByInvoiceDateDescIdDesc(java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        String sql = "SELECT * FROM resident_invoices WHERE invoice_date BETWEEN ? AND ? ORDER BY invoice_date DESC, id DESC";
        return jdbcTemplate.query(sql, rowMapper, Date.valueOf(fromDate), Date.valueOf(toDate));
    }

    public long countByStatus(InvoiceStatus status) {
        String sql = "SELECT COUNT(*) FROM resident_invoices WHERE status = ?";
        //long.class is used to convert enum value to string represntation 
        //status.name() is used to get the string representation
        return jdbcTemplate.queryForObject(sql, Long.class, status.name());
    }
    //optional handles a case where the resident invoice with its id doesn't exist

    public Optional<ResidentInvoice> findById(Long id) {
        String sql = "SELECT * FROM resident_invoices WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }
    //method to save resident invoice to a database
    public ResidentInvoice save(ResidentInvoice r) {
        //check if invoice id is null
        if (r.getId() == null) {
            //if null insert a new invoice record
            String sql = "INSERT INTO resident_invoices(resident_id, description, amount, invoice_date, due_date, status, paid_at, payment_method) VALUES(?,?,?,?,?,?,?,?)";
            //use keyholder to retrieve the generated id of newly inserted invoice record
            KeyHolder kh = new GeneratedKeyHolder();
            
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                //ps is used to set the values of the parameters
                //:null inserts null if resident id isnt set
                ps.setObject(1, r.getResident() != null ? r.getResident().getId() : null);
                ps.setString(2, r.getDescription());
                ps.setBigDecimal(3, r.getAmount());
                //valueOf converts LocalDate to java.sql.date
                ps.setDate(4, r.getInvoiceDate() != null ? Date.valueOf(r.getInvoiceDate()) : null);
                ps.setDate(5, r.getDueDate() != null ? Date.valueOf(r.getDueDate()) : null);
                ps.setString(6, r.getStatus() != null ? r.getStatus().name() : null);
                ps.setTimestamp(7, r.getPaidAt() != null ? Timestamp.valueOf(r.getPaidAt()) : null);
                ps.setString(8, r.getPaymentMethod());
                return ps;
            }, kh);
            Number k = kh.getKey(); if (k != null) r.setId(k.longValue());
            return r;
        }
        String sql = "UPDATE resident_invoices SET resident_id=?, description=?, amount=?, invoice_date=?, due_date=?, status=?, paid_at=?, payment_method=? WHERE id = ?";
        jdbcTemplate.update(sql, r.getResident() != null ? r.getResident().getId() : null, r.getDescription(), r.getAmount(), r.getInvoiceDate() != null ? Date.valueOf(r.getInvoiceDate()) : null, r.getDueDate() != null ? Date.valueOf(r.getDueDate()) : null, r.getStatus() != null ? r.getStatus().name() : null, r.getPaidAt() != null ? Timestamp.valueOf(r.getPaidAt()) : null, r.getPaymentMethod(), r.getId());
        return r;
    }
    //method to delete a resident invoice by its id from the database

    public void deleteById(Long id) {
        String sql = "DELETE FROM resident_invoices WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
