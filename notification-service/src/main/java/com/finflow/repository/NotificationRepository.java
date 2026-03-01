package com.finflow.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.finflow.dto.NotificationTypeCount;
import com.finflow.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query(value = """
            SELECT * FROM notifications
            WHERE user_id = :userId
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<Notification> findNotificationByUserId(@Param("userId") UUID userId);

    @Query(value = """
            SELECT COUNT(*) FROM notifications
            WHERE user_id = :userId
            AND is_read = :isRead
            """, nativeQuery = true)
    Long countNotificationByUserIdAndIsRead(@Param("userId") UUID userId, @Param("isRead") Boolean isRead);

    // ambil notifikasi milik user yang belum dibaca
    @Query(value = """
            SELECT * FROM notifications
            WHERE user_id = :userId
              AND is_read = FALSE
            ORDER BY created_at DESC
            """, nativeQuery = true)
    List<Notification> findUnreadByUserId(@Param("userId") UUID userId);

    // tandai semua notifikasi milik user sudah dibaca
    @Modifying
    @Query(value = """
            UPDATE notifications
            SET is_read = TRUE
            WHERE user_id = :userId
              AND is_read = FALSE
            """, nativeQuery = true)
    int markAllAsRead(@Param("userId") UUID userId);

    // ringkasan jumlah notifikasi per tipe milik user
    @Query(value = """
            SELECT
                type,
                COUNT(*) AS total
            FROM notifications
            WHERE user_id = :userId
            GROUP BY type
            ORDER BY total DESC
            """, nativeQuery = true)
    List<NotificationTypeCount> countByTypeForUser(@Param("userId") UUID userId);
}
