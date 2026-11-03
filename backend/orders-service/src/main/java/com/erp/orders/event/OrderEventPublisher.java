package com.erp.orders.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${erp.rabbitmq.enabled:false}")
    private boolean rabbitMqEnabled;

    @Value("${erp.rabbitmq.exchange:erp.order.exchange}")
    private String exchange;

    @Value("${erp.rabbitmq.routing-key:order.created}")
    private String routingKey;

    public void publishOrderCreated(OrderCreatedEvent event) {
        if (!rabbitMqEnabled) {
            log.info("[Event-Driven Simulation] Evento de Orden Creada generado: {} con {} productos. Total: ${}",
                    event.getOrderNumber(), event.getItems().size(), event.getTotalAmount());
            return;
        }

        try {
            log.info("Publicando evento a RabbitMQ [exchange={}, routingKey={}]: {}", exchange, routingKey, event.getOrderNumber());
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Evento publicado exitosamente para la orden {}", event.getOrderNumber());
        } catch (Exception ex) {
            log.warn("No se pudo publicar en RabbitMQ ({}), continuando en modo resiliente: {}",
                    ex.getMessage(), event.getOrderNumber());
        }
    }
}
