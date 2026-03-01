package com.finflow.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.finflow.model.Notification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
