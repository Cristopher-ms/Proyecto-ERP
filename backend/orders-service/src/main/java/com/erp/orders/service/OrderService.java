package com.erp.orders.service;

import com.erp.orders.domain.entity.Customer;
import com.erp.orders.domain.entity.Order;
import com.erp.orders.domain.entity.OrderItem;
import com.erp.orders.domain.enums.OrderStatus;
import com.erp.orders.domain.repository.CustomerRepository;
import com.erp.orders.domain.repository.OrderRepository;
import com.erp.orders.dto.*;
import com.erp.orders.event.OrderCreatedEvent;
import com.erp.orders.event.OrderEventPublisher;
import com.erp.orders.event.OrderItemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final OrderEventPublisher eventPublisher;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.18"); // IGV / IVA 18%

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + request.getCustomerId()));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("La orden debe incluir al menos un producto.");
        }

        // 1. Calcular subtotales
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        List<OrderItemEvent> eventItems = new ArrayList<>();

        for (CreateOrderItemRequest itemReq : request.getItems()) {
            BigDecimal itemSubtotal = itemReq.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(itemSubtotal);

            OrderItem item = OrderItem.builder()
                    .productId(itemReq.getProductId())
                    .productSku(itemReq.getProductSku())
                    .productName(itemReq.getProductName())
                    .unitPrice(itemReq.getUnitPrice())
                    .quantity(itemReq.getQuantity())
                    .subtotal(itemSubtotal)
                    .build();

            items.add(item);

            eventItems.add(OrderItemEvent.builder()
                    .productId(itemReq.getProductId())
                    .productSku(itemReq.getProductSku())
                    .quantity(itemReq.getQuantity())
                    .build());
        }

        BigDecimal taxAmount = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        // 2. Generar correlativo empresarial único (ej. ORD-202611-0001)
        String orderNumber = generateOrderNumber();

        // 3. Crear entidad Order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customer(customer)
                .status(OrderStatus.CONFIRMED)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .sellerEmail(request.getSellerEmail())
                .build();

        for (OrderItem item : items) {
            order.addItem(item);
        }

        Order saved = orderRepository.save(order);
        log.info("Orden creada exitosamente con número: {} por un total de ${}", saved.getOrderNumber(), saved.getTotalAmount());

        // 4. Publicar evento asíncrono hacia el broker de mensajería (RabbitMQ)
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(saved.getId())
                .orderNumber(saved.getOrderNumber())
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .items(eventItems)
                .totalAmount(saved.getTotalAmount())
                .createdAt(saved.getCreatedAt())
                .build();

        eventPublisher.publishOrderCreated(event);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(OrderStatus status, String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.searchOrders(status, search, pageable);
        return orders.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada con ID: " + id));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public SalesSummaryResponse getSalesSummary() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        long totalOrders = orderRepository.countTotalOrders();
        BigDecimal totalSales = orderRepository.sumAllTimeSales();
        long todayOrders = orderRepository.countOrdersSince(startOfToday);
        BigDecimal todaySales = orderRepository.sumTotalSalesSince(startOfToday);

        BigDecimal avgTicket = totalOrders > 0
                ? totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return SalesSummaryResponse.builder()
                .totalOrders(totalOrders)
                .totalSalesAmount(totalSales)
                .todayOrders(todayOrders)
                .todaySalesAmount(todaySales)
                .averageTicket(avgTicket)
                .build();
    }

    private String generateOrderNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = orderRepository.countTotalOrders() + 1;
        return String.format("ORD-%s-%04d", datePrefix, count);
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .productId(i.getProductId())
                        .productSku(i.getProductSku())
                        .productName(i.getProductName())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customer(customerService.toResponse(order.getCustomer()))
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .sellerEmail(order.getSellerEmail())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
