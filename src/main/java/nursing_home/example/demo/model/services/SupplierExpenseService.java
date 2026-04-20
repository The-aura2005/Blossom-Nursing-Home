package nursing_home.example.demo.model.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import nursing_home.example.demo.dao.SupplierExpenseRepository;
import nursing_home.example.demo.model.SupplierExpense;

@Service
public class SupplierExpenseService {

    private final SupplierExpenseRepository supplierExpenseRepository;

    public SupplierExpenseService(SupplierExpenseRepository supplierExpenseRepository) {
        this.supplierExpenseRepository = supplierExpenseRepository;
    }

    public SupplierExpense saveExpense(String supplierName, String itemName, BigDecimal cost, LocalDate expenseDate) {
        if (cost == null || cost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense cost must be greater than zero");
        }

        SupplierExpense expense = new SupplierExpense();
        expense.setSupplierName(supplierName == null || supplierName.isBlank() ? "Unknown Supplier" : supplierName);
        expense.setItemName(itemName);
        expense.setCost(cost);
        expense.setExpenseDate(expenseDate == null ? LocalDate.now() : expenseDate);
        return supplierExpenseRepository.save(expense);
    }

    public List<SupplierExpense> getAllExpenses() {
        return supplierExpenseRepository.findAllByOrderByExpenseDateDescIdDesc();
    }

    public List<SupplierExpense> getExpensesByDateRange(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null) {
            return getAllExpenses();
        }
        return supplierExpenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescIdDesc(fromDate, toDate);
    }
}
