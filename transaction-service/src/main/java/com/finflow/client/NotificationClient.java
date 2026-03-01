package com.finflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.finflow.dto.ApiResponse;
import com.finflow.dto.NotificationRequest;

/**
 * Feign Client untuk komunikasi ke Notification Service
 * Digunakan untuk mengirim notifikasi setelah transaksi berhasil
 */
@FeignClient(name = "notification-service", url = "${notification-service.url}")
public interface NotificationClient {

    @PostMapping("/api/v1/notifications")
    ApiResponse<Void> sendNotification(@RequestBody NotificationRequest request);
}