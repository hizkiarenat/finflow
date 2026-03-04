package com.finflow.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.finflow.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publisher — untuk mengirim event ke RabbitMQ
 * Dipanggil oleh TransactionServiceImpl setelah transaksi berhasil
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(NotificationEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
            );
            log.info("Notification event published for userId: {}", event.getUserId());
        } catch (Exception e) {
            // gagal published
            log.warn("Failed to publish notification event: {}", e.getMessage());
        }
    }
}
