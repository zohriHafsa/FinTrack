package com.fintrack.budget_notification_service.repository;

import com.fintrack.budget_notification_service.model.AlertPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AlertPreferencesRepository extends JpaRepository<AlertPreferences, UUID> {
    Optional<AlertPreferences> findByUserId(UUID userId);
}