package nursing_home.example.demo.model.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nursing_home.example.demo.dao.ResidentInvoiceRepository;
import nursing_home.example.demo.dao.ResidentRepository;
import nursing_home.example.demo.dao.StaffPayrollRepository;
import nursing_home.example.demo.dao.StaffRepository;
import nursing_home.example.demo.model.InvoiceStatus;
import nursing_home.example.demo.model.PayrollStatus;
import nursing_home.example.demo.model.Resident;
import nursing_home.example.demo.model.ResidentInvoice;
import nursing_home.example.demo.model.Staff;
import nursing_home.example.demo.model.StaffPayroll;
import nursing_home.example.demo.model.SupplierExpense;

@Service
public class AccountantService {

    private final ResidentInvoiceRepository residentInvoiceRepository;
    private final ResidentRepository residentRepository;
    private final StaffPayrollRepository staffPayrollRepository;
    private final StaffRepository staffRepository;
    private final SupplierExpenseService supplierExpenseService;

    public AccountantService(
            ResidentInvoiceRepository residentInvoiceRepository,
            ResidentRepository residentRepository,
            StaffPayrollRepository staffPayrollRepository,
            StaffRepository staffRepository,
            SupplierExpenseService supplierExpenseService) {
        this.residentInvoiceRepository = residentInvoiceRepository;
        this.residentRepository = residentRepository;
        this.staffPayrollRepository = staffPayrollRepository;
        this.staffRepository = staffRepository;
        this.supplierExpenseService = supplierExpenseService;
    }

    @Transactional
    public ResidentInvoice createResidentInvoice(Long residentId, BigDecimal amount, LocalDate dueDate, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invoice amount must be greater than zero");
        }

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));

        ResidentInvoice invoice = new ResidentInvoice();
        invoice.setResident(resident);
        invoice.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        invoice.setDescription((description == null || description.isBlank()) ? "Resident monthly billing" : description);
        invoice.setDueDate(dueDate == null ? LocalDate.now().plusDays(30) : dueDate);
        invoice.setStatus(InvoiceStatus.UNPAID);
        return residentInvoiceRepository.save(invoice);
    }

    @Transactional
    public void markInvoicePaid(Long invoiceId) {
        ResidentInvoice invoice = residentInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoice.setPaymentMethod("M-Pesa (Simulated)");
        residentInvoiceRepository.save(invoice);
    }

    public List<ResidentInvoice> getAllInvoices() {
        return residentInvoiceRepository.findAllByOrderByInvoiceDateDescIdDesc();
    }

    @Transactional
    public int generatePayrollForMonth(String monthText) {
        if (monthText == null || monthText.isBlank()) {
            throw new IllegalArgumentException("Payroll month is required");
        }

        int createdCount = 0;
        List<Staff> staffList = staffRepository.findAll();
        for (Staff staff : staffList) {
            if (staffPayrollRepository.existsByStaffIdAndPayrollMonth(staff.getId(), monthText)) {
                continue;
            }

            StaffPayroll payroll = new StaffPayroll();
            payroll.setStaff(staff);
            payroll.setPayrollMonth(monthText);
            payroll.setSalaryAmount(estimateStaffSalary(staff));
            payroll.setStatus(PayrollStatus.UNPAID);
            staffPayrollRepository.save(payroll);
            createdCount++;
        }
        return createdCount;
    }

    @Transactional
    public void markPayrollPaid(Long payrollId) {
        StaffPayroll payroll = staffPayrollRepository.findById(payrollId)
                .orElseThrow(() -> new IllegalArgumentException("Payroll record not found"));
        payroll.setStatus(PayrollStatus.PAID);
        payroll.setPaidAt(LocalDateTime.now());
        staffPayrollRepository.save(payroll);
    }

    public List<StaffPayroll> getAllPayroll() {
        return staffPayrollRepository.findAllByOrderByPayrollDateDescIdDesc();
    }

    public List<StaffSalaryView> getStaffSalaryViews() {
        return staffRepository.findAll().stream()
                .map(staff -> new StaffSalaryView(staff, estimateStaffSalary(staff)))
                .toList();
    }

    public DashboardSummary getDashboardSummary() {
        BigDecimal unpaidInvoicesTotal = getAllInvoices().stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.UNPAID)
                .map(ResidentInvoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paidInvoicesTotal = getAllInvoices().stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.PAID)
                .map(ResidentInvoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long unpaidPayrollCount = staffPayrollRepository.countByStatus(PayrollStatus.UNPAID);
        BigDecimal supplierExpenses = supplierExpenseService.getAllExpenses().stream()
                .map(SupplierExpense::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardSummary(unpaidInvoicesTotal, paidInvoicesTotal, unpaidPayrollCount, supplierExpenses);
    }

    public List<FinancialReportRow> getFinancialReport(LocalDate fromDate, LocalDate toDate) {
        LocalDate startDate = fromDate;
        LocalDate endDate = toDate;
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        List<ResidentInvoice> invoices = (startDate == null || endDate == null)
                ? residentInvoiceRepository.findAllByOrderByInvoiceDateDescIdDesc()
                : residentInvoiceRepository.findByInvoiceDateBetweenOrderByInvoiceDateDescIdDesc(startDate, endDate);

        List<StaffPayroll> payroll = (startDate == null || endDate == null)
                ? staffPayrollRepository.findAllByOrderByPayrollDateDescIdDesc()
                : staffPayrollRepository.findByPayrollDateBetweenOrderByPayrollDateDescIdDesc(startDate, endDate);

        List<SupplierExpense> supplierExpenses = supplierExpenseService.getExpensesByDateRange(startDate, endDate);

        List<FinancialReportRow> rows = new ArrayList<>();

        for (ResidentInvoice invoice : invoices) {
            rows.add(new FinancialReportRow(
                    "Income",
                    "Resident invoice - " + invoice.getResident().getName(),
                    invoice.getAmount(),
                    invoice.getInvoiceDate()));
        }

        for (StaffPayroll payrollRow : payroll) {
            rows.add(new FinancialReportRow(
                    "Expense",
                    "Staff salary - " + payrollRow.getStaff().getName() + " (" + payrollRow.getPayrollMonth() + ")",
                    payrollRow.getSalaryAmount(),
                    payrollRow.getPayrollDate()));
        }

        for (SupplierExpense expense : supplierExpenses) {
            rows.add(new FinancialReportRow(
                    "Expense",
                    "Supplier stock - " + expense.getSupplierName() + " / " + expense.getItemName(),
                    expense.getCost(),
                    expense.getExpenseDate()));
        }

        rows.sort(Comparator.comparing(FinancialReportRow::date).reversed());
        return rows;
    }

    public String currentPayrollMonth() {
        Month month = LocalDate.now().getMonth();
        return month.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + LocalDate.now().getYear();
    }

    private BigDecimal estimateStaffSalary(Staff staff) {
        long seed = (staff.getId() == null ? 1 : staff.getId()) + Math.abs(staff.getPhoneNumber() % 10);
        BigDecimal base = BigDecimal.valueOf(28000);
        BigDecimal delta = BigDecimal.valueOf(seed * 125);
        return base.add(delta).setScale(2, RoundingMode.HALF_UP);
    }

    public record DashboardSummary(BigDecimal outstandingInvoices, BigDecimal residentIncomeReceived,
            long unpaidPayrollCount, BigDecimal supplierExpensesTotal) {
    }

    public record StaffSalaryView(Staff staff, BigDecimal salaryAmount) {
    }

    public record FinancialReportRow(String type, String description, BigDecimal amount, LocalDate date) {
    }
}
