package com.fintrack.budget_notification_service.mapper;

import com.fintrack.budget_notification_service.dto.request.CreateBudgetRequest;
import com.fintrack.budget_notification_service.dto.response.BudgetResponse;
import com.fintrack.budget_notification_service.model.Budget;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BudgetMapper {

    public static Budget toEntity(CreateBudgetRequest request) {
        return Budget.builder()
                .companyId(request.getCompanyId())
                .categoryId(request.getCategoryId())
                .limitAmount(request.getLimitAmount())
                .period(request.getPeriod())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .currentSpend(BigDecimal.ZERO)
                .build();
    }

    public static BudgetResponse toResponse(Budget budget) {
        BigDecimal percentageUsed = BigDecimal.ZERO;
        if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = budget.getCurrentSpend()
                    .divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return BudgetResponse.builder()
                .id(budget.getId())
                .companyId(budget.getCompanyId())
                .categoryId(budget.getCategoryId())
                .limitAmount(budget.getLimitAmount())
                .period(budget.getPeriod())
                .periodStart(budget.getPeriodStart())
                .periodEnd(budget.getPeriodEnd())
                .currentSpend(budget.getCurrentSpend())
                .percentageUsed(percentageUsed)
                .createdAt(budget.getCreatedAt())
                .build();
    }
}