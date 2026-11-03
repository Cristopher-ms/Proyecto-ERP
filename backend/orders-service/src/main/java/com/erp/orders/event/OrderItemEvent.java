package com.erp.orders.event;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEvent implements Serializable {
    private Long productId;
    private String productSku;
    private Integer quantity;
}
