package com.erp.purchases.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PURCHASE_EXCHANGE    = "erp.purchase.exchange";
    public static final String STOCK_REPLENISH_KEY  = "stock.replenish";
    public static final String STOCK_REPLENISH_QUEUE = "erp.inventory.stock-replenish.queue";

    @Bean
    TopicExchange purchaseExchange() {
        return new TopicExchange(PURCHASE_EXCHANGE, true, false);
    }

    @Bean
    Queue stockReplenishQueue() {
        return QueueBuilder.durable(STOCK_REPLENISH_QUEUE).build();
    }

    @Bean
    Binding stockReplenishBinding(Queue stockReplenishQueue, TopicExchange purchaseExchange) {
        return BindingBuilder.bind(stockReplenishQueue)
                .to(purchaseExchange)
                .with(STOCK_REPLENISH_KEY);
    }

    @Bean
    Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                   Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
