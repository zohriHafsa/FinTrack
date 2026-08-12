package com.fintrack.invoice_service.dto.response;

import com.fintrack.invoice_service.model.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InvoiceResponse {
    private UUID id;
    private UUID companyId;
    private UUID clientId;
    private BigDecimal amount;
    private InvoiceStatus status;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String notes;
    private LocalDateTime createdAt;
}