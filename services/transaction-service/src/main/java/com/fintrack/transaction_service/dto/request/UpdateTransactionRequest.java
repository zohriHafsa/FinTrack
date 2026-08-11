package com.fintrack.transaction_service.dto.request;

import com.fintrack.transaction_service.model.PaymentMethod;
import com.fintrack.transaction_service.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateTransactionRequest {

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Type is required")
    private TransactionType type;

    private String description;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Date is required")
    private LocalDate date;
}