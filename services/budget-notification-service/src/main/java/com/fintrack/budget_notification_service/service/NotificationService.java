package com.fintrack.budget_notification_service.service;

import com.fintrack.budget_notification_service.dto.response.NotificationResponse;
import com.fintrack.budget_notification_service.exception.ResourceNotFoundException;
import com.fintrack.budget_notification_service.mapper.NotificationMapper;
import com.fintrack.budget_notification_service.model.Notification;
import com.fintrack.budget_notification_service.model.NotificationType;
import com.fintrack.budget_notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

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
}