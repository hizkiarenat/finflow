package com.finflow.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

/**
 * DTO untuk mengirim notifikasi ke Notification Service via Feign Client
 */
@Data
@Builder
public class NotificationRequest {
    private UUID userId;
    private String title;
    private String message;
    private String type; // TRANSFER, DEPOSIT, WITHDRAWAL
}