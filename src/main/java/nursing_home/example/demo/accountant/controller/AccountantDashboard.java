package nursing_home.example.demo.accountant.controller;

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

import nursing_home.example.demo.accountant.service.AccountantService;
import nursing_home.example.demo.accountant.service.SupplierExpenseService;
import nursing_home.example.demo.admin.Repository.ResidentRepository;

@Controller
public class AccountantDashboard {

    private final AccountantService accountantService;
    private final ResidentRepository residentRepository;
    private final SupplierExpenseService supplierExpenseService;
    
    //constructor injection for the services and repository
    public AccountantDashboard(
            AccountantService accountantService,
            ResidentRepository residentRepository,
            SupplierExpenseService supplierExpenseService) {
        this.accountantService = accountantService;
        this.residentRepository = residentRepository;
        this.supplierExpenseService = supplierExpenseService;
    }

    @GetMapping("/accountant-dashboard")//returns accountant dashboard page
    @PreAuthorize("hasRole('ACCOUNTANT')")//only authorises an accountant to access their dashboard
    public String accountantDashboard(Model model) {
        model.addAttribute("summary", accountantService.getDashboardSummary());
        model.addAttribute("invoiceCount", accountantService.getAllInvoices().size());
        model.addAttribute("payrollCount", accountantService.getAllPayroll().size());
        model.addAttribute("supplierExpenseCount", supplierExpenseService.getAllExpenses().size());
        return "accountant-dashboard";
    }
    //resident-payments page 

    @GetMapping("/resident-payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String residentPayments(Model model) {
        model.addAttribute("residents", residentRepository.findAllByOrderByNameAsc());
        model.addAttribute("invoices", accountantService.getAllInvoices());
        return "resident-payments";
    }
    
    //create invoice and mark invoice as paid via M-pesa 
    @PostMapping("/resident-payments/invoices")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String createInvoice(
            @RequestParam Long residentId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) LocalDate dueDate,
            @RequestParam(required = false) String description,
            RedirectAttributes redirectAttributes) {
                //error handling for creating invoice
        try {
            accountantService.createResidentInvoice(residentId, amount, dueDate, description);
            redirectAttributes.addFlashAttribute("residentPaymentMessage", "Invoice created successfully.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("residentPaymentError", ex.getMessage());
        }
        return "redirect:/resident-payments";
    }
     
    //mark invoice as paid via m_pesa
    @PostMapping("/resident-payments/invoices/{invoiceId}/mark-paid")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String markInvoicePaid(@PathVariable Long invoiceId, RedirectAttributes redirectAttributes) {
        //error handling for marking invoice as paid 
        try {
            accountantService.markInvoicePaid(invoiceId);
            redirectAttributes.addFlashAttribute("residentPaymentMessage", "Invoice marked PAID via M-Pesa simulation.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("residentPaymentError", ex.getMessage());
        }
        return "redirect:/resident-payments";
    }
    //model is used to pass data to the staff-payments page and uses addatribute to add data to the model

    @GetMapping("/staff-payments")
    @PreAuthorize("hasRole('ACCOUNTANT')")
    public String staffPayments(Model model) {
        model.addAttribute("staffSalaryViews", accountantService.getStaffSalaryViews());
        model.addAttribute("payrollRows", accountantService.getAllPayroll());
        model.addAttribute("defaultPayrollMonth", accountantService.currentPayrollMonth());
        return "staff-payments";//returns staff-payments page with the model data
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
    @PreAuthorize("hasRole('ACCOUNTANT')")//only authorise the accountant to mark payroll as paid.
    public String markPayrollPaid(@PathVariable Long payrollId, RedirectAttributes redirectAttributes) {
        try {
            accountantService.markPayrollPaid(payrollId);//mark payroll as paid via mpesa
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
        //@RequestParam enables the accountant to extract the fromDate and toDate parameters from the request.
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Model model) {
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("reportRows", accountantService.getFinancialReport(fromDate, toDate));
        return "report-payments";
    }

}
