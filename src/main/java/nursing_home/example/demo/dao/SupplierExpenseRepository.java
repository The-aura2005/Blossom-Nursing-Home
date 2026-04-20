package nursing_home.example.demo.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nursing_home.example.demo.model.SupplierExpense;

public interface SupplierExpenseRepository extends JpaRepository<SupplierExpense, Long> {
    List<SupplierExpense> findAllByOrderByExpenseDateDescIdDesc();

    List<SupplierExpense> findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(LocalDate fromDate, LocalDate toDate);
}
