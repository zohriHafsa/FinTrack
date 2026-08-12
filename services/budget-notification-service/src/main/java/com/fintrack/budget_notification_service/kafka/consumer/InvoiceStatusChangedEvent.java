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
public class InvoiceStatusChangedEvent {
    private UUID invoiceId;
    private UUID companyId;
    private UUID clientId;
    private String status;
    private BigDecimal amount;
    private LocalDate dueDate;
}