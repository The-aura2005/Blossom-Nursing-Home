package nursing_home.example.demo.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import nursing_home.example.demo.dao.ResidentRepository;
import nursing_home.example.demo.model.services.AccountantService;
import nursing_home.example.demo.model.services.SupplierExpenseService;

@Controller
public class AccountantDashboard {

    private final AccountantService accountantService;
    private final ResidentRepository residentRepository;
    private final SupplierExpenseService supplierExpenseService;

    public AccountantDashboard(
            AccountantService accountantService,
            ResidentRepository residentRepository,
            SupplierExpenseService supplierExpenseService) {
        this.accountantService = accountantService;
        this.residentRepository = residentRepository;
        this.supplierExpenseService = supplierExpenseService;
    }

    @GetMapping("/accountant-dashboard")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String accountantDashboard(Model model) {
        model.addAttribute("summary", accountantService.getDashboardSummary());
        model.addAttribute("invoiceCount", accountantService.getAllInvoices().size());
        model.addAttribute("payrollCount", accountantService.getAllPayroll().size());
        model.addAttribute("supplierExpenseCount", supplierExpenseService.getAllExpenses().size());
        return "accountant-dashboard";
    }

    @GetMapping("/resident-payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String residentPayments(Model model) {
        model.addAttribute("residents", residentRepository.findAllByOrderByNameAsc());
        model.addAttribute("invoices", accountantService.getAllInvoices());
        return "resident-payments";
    }

    @PostMapping("/resident-payments/invoices")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String createInvoice(
            @RequestParam Long residentId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) LocalDate dueDate,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {
        try {
            accountantService.createResidentInvoice(residentId, amount, dueDate, description);
            redirectAttributes.addFlashAttribute("residentPaymentMessage", "Invoice created successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("residentPaymentError", ex.getMessage());
        }
        return "redirect:/resident-payments";
    }

    @PostMapping("/resident-payments/invoices/{invoiceId}/mark-paid")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String markInvoicePaid(@PathVariable Long invoiceId, RedirectAttributes redirectAttributes) {
        try {
            accountantService.markInvoicePaid(invoiceId);
            redirectAttributes.addFlashAttribute("residentPaymentMessage", "Invoice marked PAID via M-Pesa simulation.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("residentPaymentError", ex.getMessage());
        }
        return "redirect:/resident-payments";
    }

    @GetMapping("/staff-payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String staffPayments(Model model) {
        model.addAttribute("staffSalaryViews", accountantService.getStaffSalaryViews());
        model.addAttribute("payrollRows", accountantService.getAllPayroll());
        model.addAttribute("defaultPayrollMonth", accountantService.currentPayrollMonth());
        return "staff-payments";
    }

    @PostMapping("/staff-payments/generate-payroll")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String generatePayroll(@RequestParam String payrollMonth, RedirectAttributes redirectAttributes) {
        try {
            int created = accountantService.generatePayrollForMonth(payrollMonth);
            redirectAttributes.addFlashAttribute("staffPaymentMessage",
                    "Generated payroll for " + payrollMonth + ". New records: " + created + ".");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("staffPaymentError", ex.getMessage());
        }
        return "redirect:/staff-payments";
    }

    @PostMapping("/staff-payments/{payrollId}/mark-paid")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String markPayrollPaid(@PathVariable Long payrollId, RedirectAttributes redirectAttributes) {
        try {
            accountantService.markPayrollPaid(payrollId);
            redirectAttributes.addFlashAttribute("staffPaymentMessage", "Salary marked as PAID.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("staffPaymentError", ex.getMessage());
        }
        return "redirect:/staff-payments";
    }

    @GetMapping("/inventory-payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String inventoryPayments(Model model) {
        model.addAttribute("supplierExpenses", supplierExpenseService.getAllExpenses());
        return "inventory-payments";
    }

    @GetMapping("/report-payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String reportPayments(
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Model model) {
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("reportRows", accountantService.getFinancialReport(fromDate, toDate));
        return "report-payments";
    }

}
