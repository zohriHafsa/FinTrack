package com.fintrack.transaction_service.dto.response;

import com.fintrack.transaction_service.model.PaymentMethod;
import com.fintrack.transaction_service.model.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID id;
    private UUID companyId;
    private UUID categoryId;
    private BigDecimal amount;
    private TransactionType type;
    private String description;
    private PaymentMethod paymentMethod;
    private LocalDate date;
    private UUID createdBy;
    private LocalDateTime createdAt;
}