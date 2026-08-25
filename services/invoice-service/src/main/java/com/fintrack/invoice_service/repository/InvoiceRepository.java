package com.fintrack.invoice_service.repository;

import com.fintrack.invoice_service.model.Invoice;
import com.fintrack.invoice_service.model.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.companyId = :companyId
        AND (:status IS NULL OR i.status = :status)
        AND (:clientId IS NULL OR i.clientId = :clientId)
        AND (:startDate IS NULL OR i.issueDate >= :startDate)
        AND (:endDate IS NULL OR i.issueDate <= :endDate)
        ORDER BY i.issueDate DESC
    """)
    Page<Invoice> findWithFilters(
            @Param("companyId") UUID companyId,
            @Param("status") InvoiceStatus status,
            @Param("clientId") UUID clientId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    List<Invoice> findByStatusAndDueDateBefore(
            InvoiceStatus status, LocalDate date
    );

    List<Invoice> findByCompanyIdAndStatus(UUID companyId, InvoiceStatus status);

}
