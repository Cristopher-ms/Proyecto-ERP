package com.erp.orders.config;

import com.erp.orders.domain.entity.Customer;
import com.erp.orders.domain.entity.Order;
import com.erp.orders.domain.entity.OrderItem;
import com.erp.orders.domain.enums.DocumentType;
import com.erp.orders.domain.enums.OrderStatus;
import com.erp.orders.domain.enums.PaymentMethod;
import com.erp.orders.domain.repository.CustomerRepository;
import com.erp.orders.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Override
    public void run(String... args) {
        if (customerRepository.count() == 0) {
            log.info("Sembrando clientes de demostración para portafolio empresarial...");

            Customer c1 = Customer.builder()
                    .firstName("Tech Solutions")
                    .lastName("Perú S.A.C.")
                    .email("contacto@techsolutions.pe")
                    .phone("+51 987 654 321")
                    .documentType(DocumentType.RUC)
                    .documentNumber("20601234561")
                    .address("Av. Javier Prado Este 4560, San Borja, Lima")
                    .active(true)
                    .build();

            Customer c2 = Customer.builder()
                    .firstName("Corporación Minera")
                    .lastName("del Sur S.A.")
                    .email("compras@minerasur.com")
                    .phone("+51 954 123 789")
                    .documentType(DocumentType.RUC)
                    .documentNumber("20509876542")
                    .address("Calle Mercaderes 120, Arequipa")
                    .active(true)
                    .build();

            Customer c3 = Customer.builder()
                    .firstName("Juan Carlos")
                    .lastName("Pérez Gómez")
                    .email("juan.perez@gmail.com")
                    .phone("+51 912 345 678")
                    .documentType(DocumentType.DNI)
                    .documentNumber("45892314")
                    .address("Av. Benavides 1230, Miraflores, Lima")
                    .active(true)
                    .build();

            Customer c4 = Customer.builder()
                    .firstName("Distribuidora Logística")
                    .lastName("Global E.I.R.L.")
                    .email("operaciones@logistaglobal.com")
                    .phone("+51 944 556 677")
                    .documentType(DocumentType.RUC)
                    .documentNumber("20456789012")
                    .address("Av. Elmer Faucett 200, Callao")
                    .active(true)
                    .build();

            List<Customer> savedCustomers = customerRepository.saveAll(List.of(c1, c2, c3, c4));
            log.info("4 Clientes de prueba sembrados exitosamente.");

            // Crear una orden semilla de demostración
            if (orderRepository.count() == 0) {
                Customer demoCustomer = savedCustomers.get(0);

                BigDecimal unitPrice1 = new BigDecimal("45.00");
                BigDecimal unitPrice2 = new BigDecimal("85.50");
                BigDecimal subtotal1 = unitPrice1.multiply(BigDecimal.valueOf(2));
                BigDecimal subtotal2 = unitPrice2.multiply(BigDecimal.valueOf(1));
                BigDecimal subtotal = subtotal1.add(subtotal2);
                BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.18"));
                BigDecimal totalAmount = subtotal.add(taxAmount);

                Order demoOrder = Order.builder()
                        .orderNumber("ORD-202611-0001")
                        .customer(demoCustomer)
                        .status(OrderStatus.CONFIRMED)
                        .subtotal(subtotal)
                        .taxAmount(taxAmount)
                        .totalAmount(totalAmount)
                        .paymentMethod(PaymentMethod.TRANSFER)
                        .notes("Pedido inicial de apertura de cuenta corporativa.")
                        .sellerEmail("vendedor@erp.com")
                        .build();

                OrderItem item1 = OrderItem.builder()
                        .productId(1L)
                        .productSku("PROD-001")
                        .productName("Cable UTP Categoría 6 Bobina 305m")
                        .unitPrice(unitPrice1)
                        .quantity(2)
                        .subtotal(subtotal1)
                        .build();

                OrderItem item2 = OrderItem.builder()
                        .productId(2L)
                        .productSku("PROD-002")
                        .productName("Switch Gigabit Ethernet 16 Puertos")
                        .unitPrice(unitPrice2)
                        .quantity(1)
                        .subtotal(subtotal2)
                        .build();

                demoOrder.addItem(item1);
                demoOrder.addItem(item2);

                orderRepository.save(demoOrder);
                log.info("Orden inicial de demostración ORD-202611-0001 sembrada con éxito.");
            }
        }
    }
}
