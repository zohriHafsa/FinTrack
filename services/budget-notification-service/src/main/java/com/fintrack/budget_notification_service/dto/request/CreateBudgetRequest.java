package com.fintrack.budget_notification_service.dto.request;

import com.fintrack.budget_notification_service.model.BudgetPeriod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateBudgetRequest {

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @NotNull(message = "Limit amount is required")
    @Positive(message = "Limit amount must be positive")
    private BigDecimal limitAmount;

    @NotNull(message = "Period is required")
    private BudgetPeriod period;

    @NotNull(message = "Period start is required")
    private LocalDate periodStart;

    @NotNull(message = "Period end is required")
    private LocalDate periodEnd;
}