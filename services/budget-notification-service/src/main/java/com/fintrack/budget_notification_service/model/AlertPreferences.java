package com.fintrack.budget_notification_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "alert_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "invoice_overdue", nullable = false)
    @Builder.Default
    private Boolean invoiceOverdue = true;

    @Column(name = "budget_alerts", nullable = false)
    @Builder.Default
    private Boolean budgetAlerts = true;

    @Column(name = "weekly_summary", nullable = false)
    @Builder.Default
    private Boolean weeklySummary = true;

    @Column(name = "payment_received", nullable = false)
    @Builder.Default
    private Boolean paymentReceived = false;

    @Column(name = "budget_threshold", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal budgetThreshold = new BigDecimal("90.00");

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    @Builder.Default
    private Boolean inAppEnabled = true;
}