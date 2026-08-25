package com.fintrack.invoice_service.kafka.producer;

import com.fintrack.invoice_service.model.InvoiceStatus;
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
public class InvoiceStatusChangedEvent {
    private UUID invoiceId;
    private UUID companyId;
    private UUID clientId;
    private InvoiceStatus status;
    private BigDecimal amount;
    private LocalDate dueDate;
}