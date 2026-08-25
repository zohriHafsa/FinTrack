package com.fintrack.budget_notification_service.dto.response;

import com.fintrack.budget_notification_service.model.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private UUID companyId;
    private UUID userId;
    private NotificationType type;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}