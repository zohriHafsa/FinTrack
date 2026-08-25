package com.fintrack.budget_notification_service.repository;

import com.fintrack.budget_notification_service.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByCompanyId(UUID companyId);
    Optional<Budget> findByCompanyIdAndCategoryId(UUID companyId, UUID categoryId);
}