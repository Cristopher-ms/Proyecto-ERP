package com.erp.purchases.config;

import com.erp.purchases.domain.entity.PurchaseItem;
import com.erp.purchases.domain.entity.PurchaseOrder;
import com.erp.purchases.domain.entity.Supplier;
import com.erp.purchases.domain.enums.PaymentTerms;
import com.erp.purchases.domain.enums.PurchaseStatus;
import com.erp.purchases.domain.enums.SupplierStatus;
import com.erp.purchases.domain.repository.PurchaseOrderRepository;
import com.erp.purchases.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final BigDecimal IGV = new BigDecimal("0.18");

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    public void run(String... args) {
        if (supplierRepository.count() > 0) {
            log.info("DataInitializer: datos ya existentes, omitiendo seed.");
            return;
        }

        log.info("DataInitializer: cargando proveedores y OC semilla...");

        // ── Proveedores ────────────────────────────────────────────────────────
        Supplier tech = supplierRepository.save(Supplier.builder()
                .companyName("Tech Distribuciones S.A.C.")
                .taxId("20501234567")
                .contactName("Carlos Mendoza")
                .email("ventas@techdist.pe")
                .phone("+51 1 234-5678")
                .address("Av. Javier Prado 2680, San Borja, Lima")
                .country("Perú")
                .paymentTerms(PaymentTerms.NET_30)
                .status(SupplierStatus.ACTIVE)
                .notes("Proveedor principal de equipos tecnológicos y periféricos.")
                .build());

        Supplier importadora = supplierRepository.save(Supplier.builder()
                .companyName("Importadora Global E.I.R.L.")
                .taxId("20601234568")
                .contactName("Ana Torres")
                .email("compras@importglobal.com")
                .phone("+51 1 345-6789")
                .address("Calle Los Álamos 450, Miraflores, Lima")
                .country("Perú")
                .paymentTerms(PaymentTerms.NET_60)
                .status(SupplierStatus.ACTIVE)
                .notes("Importadora de electrónica y componentes desde Asia.")
                .build());

        supplierRepository.save(Supplier.builder()
                .companyName("Distribuidora Central SAC")
                .taxId("20701234569")
                .contactName("Roberto Silva")
                .email("info@distcentral.pe")
                .phone("+51 1 456-7890")
                .address("Jr. Huallaga 1250, Cercado de Lima")
                .country("Perú")
                .paymentTerms(PaymentTerms.NET_15)
                .status(SupplierStatus.ACTIVE)
                .notes("Suministros de oficina y almacén.")
                .build());

        supplierRepository.save(Supplier.builder()
                .companyName("Global Office Supply Corp.")
                .taxId("US-123456789")
                .contactName("James Wilson")
                .email("orders@globaloffice.com")
                .phone("+1 800 555-0199")
                .address("1000 Business Pkwy, Miami, FL 33101")
                .country("Estados Unidos")
                .paymentTerms(PaymentTerms.NET_60)
                .status(SupplierStatus.ACTIVE)
                .notes("Proveedor internacional de mobiliario y equipo de oficina.")
                .build());

        // ── OC semilla (ya RECEIVED) ────────────────────────────────────────
        PurchaseOrder po = PurchaseOrder.builder()
                .purchaseNumber("PO-202610-0001")
                .supplier(tech)
                .status(PurchaseStatus.RECEIVED)
                .expectedDeliveryDate(LocalDateTime.now().minusDays(10))
                .receivedDate(LocalDateTime.now().minusDays(5))
                .notes("Reposición inicial de laptops y monitores para el área de TI.")
                .buyerEmail("compras@erp-empresa.pe")
                .build();

        PurchaseItem item1 = PurchaseItem.builder()
                .purchaseOrder(po)
                .productId(1L)
                .productSku("LAP-001")
                .productName("Laptop Dell Inspiron 15")
                .unitCost(new BigDecimal("2800.00"))
                .quantity(5)
                .subtotal(new BigDecimal("14000.00"))
                .build();

        PurchaseItem item2 = PurchaseItem.builder()
                .purchaseOrder(po)
                .productId(2L)
                .productSku("MON-002")
                .productName("Monitor Samsung 24\"")
                .unitCost(new BigDecimal("650.00"))
                .quantity(10)
                .subtotal(new BigDecimal("6500.00"))
                .build();

        po.setItems(List.of(item1, item2));

        BigDecimal subtotal = new BigDecimal("20500.00");
        BigDecimal tax = subtotal.multiply(IGV).setScale(2, RoundingMode.HALF_UP);
        po.setSubtotal(subtotal);
        po.setTaxAmount(tax);
        po.setTotalAmount(subtotal.add(tax).setScale(2, RoundingMode.HALF_UP));

        purchaseOrderRepository.save(po);

        // ── Segunda OC en estado APPROVED ──────────────────────────────────
        PurchaseOrder po2 = PurchaseOrder.builder()
                .purchaseNumber("PO-202611-0001")
                .supplier(importadora)
                .status(PurchaseStatus.APPROVED)
                .expectedDeliveryDate(LocalDateTime.now().plusDays(7))
                .notes("Componentes electrónicos para el próximo trimestre.")
                .buyerEmail("compras@erp-empresa.pe")
                .build();

        PurchaseItem item3 = PurchaseItem.builder()
                .purchaseOrder(po2)
                .productId(3L)
                .productSku("USB-003")
                .productName("Hub USB-C 7 puertos")
                .unitCost(new BigDecimal("120.00"))
                .quantity(20)
                .subtotal(new BigDecimal("2400.00"))
                .build();

        po2.setItems(List.of(item3));
        BigDecimal sub2 = new BigDecimal("2400.00");
        BigDecimal tax2 = sub2.multiply(IGV).setScale(2, RoundingMode.HALF_UP);
        po2.setSubtotal(sub2);
        po2.setTaxAmount(tax2);
        po2.setTotalAmount(sub2.add(tax2).setScale(2, RoundingMode.HALF_UP));
        purchaseOrderRepository.save(po2);

        log.info("DataInitializer: 4 proveedores y 2 OCs creadas correctamente.");
    }
}
