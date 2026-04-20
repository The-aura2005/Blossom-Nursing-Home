package nursing_home.example.demo.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nursing_home.example.demo.model.PayrollStatus;
import nursing_home.example.demo.model.StaffPayroll;

public interface StaffPayrollRepository extends JpaRepository<StaffPayroll, Long> {
    List<StaffPayroll> findAllByOrderByPayrollDateDescIdDesc();

    List<StaffPayroll> findByPayrollDateBetweenOrderByPayrollDateDescIdDesc(LocalDate fromDate, LocalDate toDate);

    boolean existsByStaffIdAndPayrollMonth(Long staffId, String payrollMonth);

    long countByStatus(PayrollStatus status);
}
