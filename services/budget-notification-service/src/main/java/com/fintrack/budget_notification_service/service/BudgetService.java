package com.fintrack.budget_notification_service.service;

import com.fintrack.budget_notification_service.dto.request.CreateBudgetRequest;
import com.fintrack.budget_notification_service.dto.request.UpdateBudgetRequest;
import com.fintrack.budget_notification_service.dto.response.BudgetResponse;
import com.fintrack.budget_notification_service.exception.ResourceNotFoundException;
import com.fintrack.budget_notification_service.kafka.producer.BudgetEventProducer;
import com.fintrack.budget_notification_service.kafka.producer.BudgetExceededEvent;
import com.fintrack.budget_notification_service.mapper.BudgetMapper;
import com.fintrack.budget_notification_service.model.AlertPreferences;
import com.fintrack.budget_notification_service.model.Budget;
import com.fintrack.budget_notification_service.model.NotificationType;
import com.fintrack.budget_notification_service.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private static final BigDecimal WARNING_THRESHOLD_PERCENT = BigDecimal.valueOf(90);

    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;
    private final BudgetEventProducer budgetEventProducer;

    @Transactional
    public BudgetResponse createBudget(CreateBudgetRequest request) {
        log.info("Creating budget for company: {}", request.getCompanyId());
        Budget budget = BudgetMapper.toEntity(request);
        Budget saved = budgetRepository.save(budget);
        return BudgetMapper.toResponse(saved);
    }

    public List<BudgetResponse> getBudgetsByCompany(UUID companyId) {
        return budgetRepository.findByCompanyId(companyId)
                .stream()
                .map(BudgetMapper::toResponse)
                .toList();
    }

    public BudgetResponse getBudgetById(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Budget not found with id: " + id
                ));
        return BudgetMapper.toResponse(budget);
    }

    @Transactional
    public BudgetResponse updateBudget(UUID id, UpdateBudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Budget not found with id: " + id
                ));
        budget.setLimitAmount(request.getLimitAmount());
        budget.setWarningSent(false);
        budget.setExceededSent(false);
        Budget updated = budgetRepository.save(budget);
        return BudgetMapper.toResponse(updated);
    }

    @Transactional
    public void deleteBudget(UUID id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Budget not found with id: " + id
                ));
        budgetRepository.delete(budget);
    }

    @Transactional
    public void updateSpendAndCheckBudget(UUID companyId, UUID categoryId, BigDecimal amount) {

        Optional<Budget> budgetOpt = budgetRepository
                .findByCompanyIdAndCategoryId(companyId, categoryId);

        if (budgetOpt.isEmpty()) {
            log.debug("No budget found for company: {} category: {}",
                    companyId, categoryId);
            return;
        }

        Budget budget = budgetOpt.get();
        BigDecimal newSpend = budget.getCurrentSpend().add(amount);
        budget.setCurrentSpend(newSpend);

        BigDecimal percentageUsed = budget.getLimitAmount().signum() == 0
                ? BigDecimal.ZERO
                : newSpend.multiply(BigDecimal.valueOf(100))
                        .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP);

        if (percentageUsed.compareTo(BigDecimal.valueOf(100)) >= 0 && !budget.getExceededSent()) {
            log.warn("Budget exceeded for company: {} category: {}", companyId, categoryId);
            budget.setExceededSent(true);

            notificationService.notifyCompany(companyId, NotificationType.BUDGET_EXCEEDED,
                    String.format("Budget exceeded! Spent %.2f MAD of %.2f MAD limit (%.0f%% used).",
                            newSpend, budget.getLimitAmount(), percentageUsed),
                    AlertPreferences::getBudgetAlerts);

            budgetEventProducer.publishBudgetExceeded(
                    BudgetExceededEvent.builder()
                            .budgetId(budget.getId())
                            .companyId(companyId)
                            .categoryId(categoryId)
                            .budgetLimit(budget.getLimitAmount())
                            .currentSpend(newSpend)
                            .exceededAt(LocalDateTime.now())
                            .build()
            );

        } else if (percentageUsed.compareTo(WARNING_THRESHOLD_PERCENT) >= 0 && !budget.getWarningSent()) {
            log.info("Budget nearing limit for company: {} category: {} ({}% used)",
                    companyId, categoryId, percentageUsed);
            budget.setWarningSent(true);

            notificationService.notifyCompany(companyId, NotificationType.BUDGET_WARNING,
                    String.format("Heads up: you've used %.0f%% of your %.2f MAD budget (%.2f MAD spent so far).",
                            percentageUsed, budget.getLimitAmount(), newSpend),
                    AlertPreferences::getBudgetAlerts);
        }

        budgetRepository.save(budget);
    }
}