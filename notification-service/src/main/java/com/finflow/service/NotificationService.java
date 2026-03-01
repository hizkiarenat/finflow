package com.finflow.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.finflow.dto.NotificationRequest;
import com.finflow.dto.NotificationResponse;

public interface NotificationService {

    NotificationResponse save(NotificationRequest request);

    List<NotificationResponse> getByUserId(UUID userId);

    List<NotificationResponse> getUnread(UUID userId);

    void markAllAsRead(UUID userId);

    long countUnread(UUID userId);

    List<Map<String, Object>> getSummaryByType(UUID userId);
}
