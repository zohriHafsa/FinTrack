package com.fintrack.budget_notification_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class AlertPreferencesResponse {
    private UUID id;
    private UUID userId;
    private Boolean invoiceOverdue;
    private Boolean budgetAlerts;
    private Boolean weeklySummary;
    private Boolean paymentReceived;
    private BigDecimal budgetThreshold;
    private Boolean emailEnabled;
    private Boolean inAppEnabled;
}