package com.finflow.service.Impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finflow.dto.NotificationRequest;
import com.finflow.dto.NotificationResponse;
import com.finflow.model.Notification;
import com.finflow.repository.NotificationRepository;
import com.finflow.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;

    // simpan notif
    @Override
    @Transactional
    public NotificationResponse save(NotificationRequest request) {
        // validasi tipe notifikasi
        Notification.NotificationType type;
        try {
            type = Notification.NotificationType.valueOf(request.getType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid notification type, must be: TRANSFER, DEPOSIT, WITHDRAWAL, INFO");
        }

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(type)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification saved with id: {}", saved.getId());

        return NotificationResponse.fromEntity(saved);
    }

    // hitung notif yg blm dibaca
    @Override
    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countNotificationByUserIdAndIsRead(userId, false);
    }

    // ambil semua notif milik user
    @Override
    public List<NotificationResponse> getByUserId(UUID userId) {
        return notificationRepository.findNotificationByUserId(userId)
                .stream()
                .map(NotificationResponse::fromEntity)       
                .collect(Collectors.toList());
    }

    // ringkasan notif per tipe
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSummaryByType(UUID userId) {
        return notificationRepository.countByTypeForUser(userId)
                .stream()                                    // Java Stream dari List<Object[]>
                .map(row -> {
                    Map<String, Object> summary = new LinkedHashMap<>();
                    summary.put("type", row.getType());
                    summary.put("total", row.getTotal());
                    return summary;
                })
                .collect(Collectors.toList());
    }

    // ambil notif yg blm di read
    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnread(UUID userId) {
        return notificationRepository.findUnreadByUserId(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // tandai semua notif sudah dibaca
    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        int updated = notificationRepository.markAllAsRead(userId);
        log.info("Marked {} notifications as read for userId: {}", updated, userId);
    }

}
