package com.fintrack.budget_notification_service.service;

import com.fintrack.budget_notification_service.dto.request.UpdateAlertPreferencesRequest;
import com.fintrack.budget_notification_service.dto.response.AlertPreferencesResponse;
import com.fintrack.budget_notification_service.mapper.AlertPreferencesMapper;
import com.fintrack.budget_notification_service.model.AlertPreferences;
import com.fintrack.budget_notification_service.repository.AlertPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertPreferencesService {

    private final AlertPreferencesRepository alertPreferencesRepository;

    public AlertPreferencesResponse getPreferences(UUID userId) {
        AlertPreferences prefs = alertPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
        return AlertPreferencesMapper.toResponse(prefs);
    }

    @Transactional
    public AlertPreferencesResponse updatePreferences(
            UUID userId, UpdateAlertPreferencesRequest request) {

        AlertPreferences prefs = alertPreferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        if (request.getInvoiceOverdue() != null)
            prefs.setInvoiceOverdue(request.getInvoiceOverdue());
        if (request.getBudgetAlerts() != null)
            prefs.setBudgetAlerts(request.getBudgetAlerts());
        if (request.getWeeklySummary() != null)
            prefs.setWeeklySummary(request.getWeeklySummary());
        if (request.getPaymentReceived() != null)
            prefs.setPaymentReceived(request.getPaymentReceived());
        if (request.getBudgetThreshold() != null)
            prefs.setBudgetThreshold(request.getBudgetThreshold());
        if (request.getEmailEnabled() != null)
            prefs.setEmailEnabled(request.getEmailEnabled());
        if (request.getInAppEnabled() != null)
            prefs.setInAppEnabled(request.getInAppEnabled());

        AlertPreferences saved = alertPreferencesRepository.save(prefs);
        return AlertPreferencesMapper.toResponse(saved);
    }

    private AlertPreferences createDefaultPreferences(UUID userId) {
        AlertPreferences prefs = AlertPreferences.builder()
                .userId(userId)
                .build();
        return alertPreferencesRepository.save(prefs);
    }
}