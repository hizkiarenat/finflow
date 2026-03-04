package com.finflow.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE    = "finflow.exchange";
    public static final String QUEUE       = "finflow.notification.queue";
    public static final String ROUTING_KEY = "finflow.notification";

    // exchange -> seperti kantor pos yang menerima pesan
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // queue -> tempat pesan menunggu untuk diproses
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true); // true = durable, pesan tidak hilang saat restart
    }

    // binding -> menghubungkan queue ke exchange dengan routing_key
    @Bean
    public Binding binding(Queue queue, TopicExchange topicExchange) {
        return BindingBuilder
                .bind(queue)
                .to(topicExchange)
                .with(ROUTING_KEY);
    }

    // Converter -> agar pesan dikirim dalam format JSON
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
