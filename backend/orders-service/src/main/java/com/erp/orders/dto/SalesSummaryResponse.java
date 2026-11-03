package com.erp.orders.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesSummaryResponse {

    private Long totalOrders;
    private BigDecimal totalSalesAmount;
    private Long todayOrders;
    private BigDecimal todaySalesAmount;
    private BigDecimal averageTicket;
}
