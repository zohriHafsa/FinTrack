package com.fintrack.budget_notification_service.service;

import com.fintrack.budget_notification_service.dto.request.CreateBudgetRequest;
import com.fintrack.budget_notification_service.dto.request.UpdateBudgetRequest;
import com.fintrack.budget_notification_service.dto.response.BudgetResponse;
import com.fintrack.budget_notification_service.exception.ResourceNotFoundException;
import com.fintrack.budget_notification_service.kafka.producer.BudgetEventProducer;
import com.fintrack.budget_notification_service.kafka.producer.BudgetExceededEvent;
import com.fintrack.budget_notification_service.mapper.BudgetMapper;
import com.fintrack.budget_notification_service.model.Budget;
import com.fintrack.budget_notification_service.model.Notification;
import com.fintrack.budget_notification_service.model.NotificationType;
import com.fintrack.budget_notification_service.repository.BudgetRepository;
import com.fintrack.budget_notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final NotificationRepository notificationRepository;
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
    public void updateSpendAndCheckBudget(UUID companyId, UUID categoryId,
                                          BigDecimal amount, UUID userId) {

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
        budgetRepository.save(budget);

        // Check if budget exceeded
        if (newSpend.compareTo(budget.getLimitAmount()) > 0) {
            log.warn("Budget exceeded for company: {} category: {}",
                    companyId, categoryId);

            // Create notification
            Notification notification = Notification.builder()
                    .companyId(companyId)
                    .userId(userId)
                    .type(NotificationType.BUDGET_EXCEEDED)
                    .message(String.format(
                            "Budget exceeded! Spent %.2f MAD of %.2f MAD limit",
                            newSpend, budget.getLimitAmount()))
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);

            // Publish Kafka event
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
        }
    }
}