package com.fintrack.transaction_service.repository;

import com.fintrack.transaction_service.model.Transaction;
import com.fintrack.transaction_service.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.UUID;

public interface TransactionRepository  extends JpaRepository<Transaction, UUID> {

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.companyId = :companyId
        AND (:type IS NULL OR t.type = :type)
        AND (:categoryId IS NULL OR t.categoryId = :categoryId)
        AND (:startDate IS NULL OR t.date >= :startDate)
        AND (:endDate IS NULL OR t.date <= :endDate)
        ORDER BY t.date DESC
    """)

    Page<Transaction> findWithFilters(
            @Param("companyId") UUID companyId,
            @Param("type")TransactionType type,
            @Param("categoryId") UUID categoryId,
            @Param("startDate")LocalDate startDate,
            @Param("endDate")LocalDate endDate,
            Pageable pageable
            );
}
