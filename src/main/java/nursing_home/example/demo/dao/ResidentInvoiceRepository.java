package nursing_home.example.demo.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import nursing_home.example.demo.model.InvoiceStatus;
import nursing_home.example.demo.model.ResidentInvoice;

public interface ResidentInvoiceRepository extends JpaRepository<ResidentInvoice, Long> {
    List<ResidentInvoice> findAllByOrderByInvoiceDateDescIdDesc();

    List<ResidentInvoice> findByInvoiceDateBetweenOrderByInvoiceDateDescIdDesc(LocalDate fromDate, LocalDate toDate);

    long countByStatus(InvoiceStatus status);
}
