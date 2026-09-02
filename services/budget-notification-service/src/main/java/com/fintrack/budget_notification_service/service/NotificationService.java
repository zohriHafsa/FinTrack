package com.fintrack.budget_notification_service.service;

import com.fintrack.budget_notification_service.client.AuthServiceClient;
import com.fintrack.budget_notification_service.dto.response.NotificationResponse;
import com.fintrack.budget_notification_service.exception.ResourceNotFoundException;
import com.fintrack.budget_notification_service.mapper.NotificationMapper;
import com.fintrack.budget_notification_service.model.AlertPreferences;
import com.fintrack.budget_notification_service.model.Notification;
import com.fintrack.budget_notification_service.model.NotificationType;
import com.fintrack.budget_notification_service.repository.AlertPreferencesRepository;
import com.fintrack.budget_notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuthServiceClient authServiceClient;
    private final AlertPreferencesRepository alertPreferencesRepository;

    public List<NotificationResponse> getUserNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId
                ));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
        log.info("Marked {} notifications as read for user: {}",
                unread.size(), userId);
    }

    @Transactional
    public void createNotification(UUID companyId, UUID userId,
                                   NotificationType type, String message) {
        Notification notification = Notification.builder()
                .companyId(companyId)
                .userId(userId)
                .type(type)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    /**
     * Resolves every real user belonging to a company (via auth-service) and
     * creates one notification per user, skipping anyone who has opted out
     * via the given AlertPreferences flag. Company-wide events (budgets,
     * invoices) have no single "owner" to address, so this replaces the
     * previous bug of writing the companyId itself into the userId column.
     */
    @Transactional
    public void notifyCompany(UUID companyId, NotificationType type, String message,
                               Function<AlertPreferences, Boolean> preferenceEnabled) {
        List<UUID> userIds = authServiceClient.getCompanyUserIds(companyId);
        if (userIds.isEmpty()) {
            log.warn("No users found for company {} - cannot deliver {} notification", companyId, type);
            return;
        }

        for (UUID userId : userIds) {
            boolean enabled = alertPreferencesRepository.findByUserId(userId)
                    .map(preferenceEnabled)
                    .orElse(true);
            if (!enabled) continue;
            createNotification(companyId, userId, type, message);
        }
    }
}