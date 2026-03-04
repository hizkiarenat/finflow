package com.finflow.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * karna ini consumer jadi tidak perlu config queue,exchange,bind,rabbitTemplate
 * cukup buatkan messageConverter saja untuk consume eventnya
 */
@Configuration
public class RabbitMQConfig {

    // Converter -> agar pesan dikirim dalam format JSON
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
