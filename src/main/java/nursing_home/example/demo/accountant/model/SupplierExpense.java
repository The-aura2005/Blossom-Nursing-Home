package nursing_home.example.demo.accountant.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

//@entity indicates this class is a JPA entity and it will be mapped to a db table
//there will be a table called supplier_expense in the db to store supplier expense records
@Entity
@Table(name = "supplier_expenses")
public class SupplierExpense {

    @Id
    //it will nbe automatically generated
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String supplierName;
    private String itemName;
    private BigDecimal cost;
    private LocalDate expenseDate;

    @PrePersist
    public void prePersist() {
        if (expenseDate == null) {
            expenseDate = LocalDate.now();
        }
    }
    //getters  and  settters

    public Long getId() {
        return id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public void setId(long longValue) {
        this.id = longValue;
    }
}
