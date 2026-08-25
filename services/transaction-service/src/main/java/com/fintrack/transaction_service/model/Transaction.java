package com.fintrack.transaction_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id",nullable = false)
    private UUID companyId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(length = 500)
    private String description;

    @Column(name ="created_at",nullable = false )
    private LocalDateTime createdAt;

    @Column(name = "created_by",nullable = false )
    private UUID createdBy;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now();}

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;


}
