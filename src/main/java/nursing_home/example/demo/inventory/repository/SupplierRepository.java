package nursing_home.example.demo.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import nursing_home.example.demo.inventory.model.Supplier;

@Repository
public class SupplierRepository {
    private final JdbcTemplate jdbcTemplate;

    public SupplierRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Supplier> rowMapper = (rs, rowNum) -> {
        Supplier s = new Supplier();
        s.setId(rs.getLong("id"));
        s.setName(rs.getString("name"));
        s.setContactInfo(rs.getString("contact_info"));
        return s;
    };

    public List<Supplier> findAll() {
        String sql = "SELECT * FROM inventory_suppliers";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Supplier> findById(Long id) {
        String sql = "SELECT * FROM inventory_suppliers WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id).stream().findFirst();
    }

    public Supplier save(Supplier s) {
        if (s.getId() == null) {
            String sql = "INSERT INTO inventory_suppliers(name, contact_info) VALUES(?,?)";
            KeyHolder kh = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                var ps = conn.prepareStatement(sql, new String[] { "id" });
                ps.setString(1, s.getName());
                ps.setString(2, s.getContactInfo());
                return ps;
            }, kh);
            Number key = kh.getKey(); if (key != null) s.setId(key.longValue());
            return s;
        }
        String sql = "UPDATE inventory_suppliers SET name=?, contact_info=? WHERE id = ?";
        jdbcTemplate.update(sql, s.getName(), s.getContactInfo(), s.getId());
        return s;
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM inventory_suppliers WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}

