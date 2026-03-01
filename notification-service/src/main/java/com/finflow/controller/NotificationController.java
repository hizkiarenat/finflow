package com.finflow.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finflow.dto.ApiResponse;
import com.finflow.dto.NotificationRequest;
import com.finflow.dto.NotificationResponse;
import com.finflow.service.NotificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Simpan notifikasi baru
     * Dipanggil oleh Transaction Service via Feign Client
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> save(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.save(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification successfully saved", response));
    }

    // ambil semua notif milik user
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByUserId(@PathVariable UUID userId) {
        List<NotificationResponse> notifications = notificationService.getByUserId(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Found " + notifications.size() + " notifications", notifications));
    }

    // ambil notif yg blm dibaca
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnread(@PathVariable UUID userId) {
        List<NotificationResponse> notifications = notificationService.getUnread(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Unread notifications", notifications));
    }

    // hitung notif belum dibaca
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<ApiResponse<Long>> countUnread(@PathVariable UUID userId) {
        long count = notificationService.countUnread(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Unread count", count));
    }

    // tandai semua notif sudah dibaca
    @PatchMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(
                ApiResponse.success("All notifications marked as read", null));
    }

    // ringkasan notif per tipe
    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSummary(
            @PathVariable UUID userId) {
        List<Map<String, Object>> summary = notificationService.getSummaryByType(userId);
        return ResponseEntity.ok(
                ApiResponse.success("Notification summary", summary));
    }
}
