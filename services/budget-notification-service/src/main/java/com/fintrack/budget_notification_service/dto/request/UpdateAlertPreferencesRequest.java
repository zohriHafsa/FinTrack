package com.fintrack.budget_notification_service.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateAlertPreferencesRequest {
    private Boolean invoiceOverdue;
    private Boolean budgetAlerts;
    private Boolean weeklySummary;
    private Boolean paymentReceived;
    private BigDecimal budgetThreshold;
    private Boolean emailEnabled;
    private Boolean inAppEnabled;
}