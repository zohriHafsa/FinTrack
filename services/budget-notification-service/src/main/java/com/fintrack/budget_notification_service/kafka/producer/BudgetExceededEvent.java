package com.fintrack.budget_notification_service.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetExceededEvent {
    private UUID budgetId;
    private UUID companyId;
    private UUID categoryId;
    private BigDecimal budgetLimit;
    private BigDecimal currentSpend;
    private LocalDateTime exceededAt;
}