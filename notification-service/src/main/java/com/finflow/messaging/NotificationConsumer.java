package com.finflow.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.finflow.model.Notification;
import com.finflow.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumer — mendengarkan pesan dari RabbitMQ queue
 * Setiap ada event masuk, langsung simpan ke database
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @RabbitListener(queues = "finflow.notification.queue")
    public void consume(NotificationEvent event) {
        log.info("Received notification event for userId: {}", event.getUserId());
        try {
            Notification.NotificationType type = Notification.NotificationType.valueOf(event.getType());
            Notification notification = Notification.builder()
                    .userId(event.getUserId())
                    .title(event.getTitle())
                    .message(event.getMessage())
                    .type(type)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
            log.info("Notification saved for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to proccess notification event: {}", e.getMessage());
        }
    }
}
