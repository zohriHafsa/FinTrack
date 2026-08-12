package com.fintrack.budget_notification_service.kafka.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
    private UUID transactionId;
    private UUID companyId;
    private UUID categoryId;
    private BigDecimal amount;
    private String type;
    private LocalDate date;
}