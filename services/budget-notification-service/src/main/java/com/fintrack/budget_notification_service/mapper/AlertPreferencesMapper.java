package com.fintrack.budget_notification_service.mapper;

import com.fintrack.budget_notification_service.dto.response.AlertPreferencesResponse;
import com.fintrack.budget_notification_service.model.AlertPreferences;

public class AlertPreferencesMapper {

    public static AlertPreferencesResponse toResponse(AlertPreferences prefs) {
        return AlertPreferencesResponse.builder()
                .id(prefs.getId())
                .userId(prefs.getUserId())
                .invoiceOverdue(prefs.getInvoiceOverdue())
                .budgetAlerts(prefs.getBudgetAlerts())
                .weeklySummary(prefs.getWeeklySummary())
                .paymentReceived(prefs.getPaymentReceived())
                .budgetThreshold(prefs.getBudgetThreshold())
                .emailEnabled(prefs.getEmailEnabled())
                .inAppEnabled(prefs.getInAppEnabled())
                .build();
    }
}