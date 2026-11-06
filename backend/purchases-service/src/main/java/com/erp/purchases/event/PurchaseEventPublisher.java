package com.erp.purchases.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${erp.rabbitmq.enabled:false}")
    private boolean enabled;

    @Value("${erp.rabbitmq.exchange:erp.purchase.exchange}")
    private String exchange;

    @Value("${erp.rabbitmq.routing-key:stock.replenish}")
    private String routingKey;

    public void publishStockReplenish(StockReplenishEvent event) {
        if (!enabled) {
            log.info("[Event-Driven Simulation] StockReplenishEvent generado para orden {} — {} líneas de productos reabastecidos.",
                    event.getPurchaseNumber(), event.getItems().size());
            return;
        }
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("StockReplenishEvent publicado en RabbitMQ para orden {}", event.getPurchaseNumber());
        } catch (Exception ex) {
            log.warn("No se pudo publicar en RabbitMQ ({}). El stock fue actualizado directamente vía REST.", ex.getMessage());
        }
    }
}
