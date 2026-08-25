package com.fintrack.transaction_service.kafka.producer;

import com.fintrack.transaction_service.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
    private UUID transactionId;
    private UUID companyId;
    private UUID categoryId;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDate date;
}