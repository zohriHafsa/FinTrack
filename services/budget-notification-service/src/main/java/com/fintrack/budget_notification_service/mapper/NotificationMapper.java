package com.fintrack.budget_notification_service.mapper;

import com.fintrack.budget_notification_service.dto.response.NotificationResponse;
import com.fintrack.budget_notification_service.model.Notification;

public class NotificationMapper {

    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .companyId(notification.getCompanyId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}