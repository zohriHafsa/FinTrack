package com.fintrack.budget_notification_service.dto.response;

import com.fintrack.budget_notification_service.model.BudgetPeriod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class BudgetResponse {
    private UUID id;
    private UUID companyId;
    private UUID categoryId;
    private BigDecimal limitAmount;
    private BudgetPeriod period;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal currentSpend;
    private BigDecimal percentageUsed;
    private LocalDateTime createdAt;
}