package com.erp.inventory.config;

import com.erp.inventory.domain.entity.Category;
import com.erp.inventory.domain.entity.Product;
import com.erp.inventory.domain.entity.StockMovement;
import com.erp.inventory.domain.enums.MovementType;
import com.erp.inventory.domain.repository.CategoryRepository;
import com.erp.inventory.domain.repository.ProductRepository;
import com.erp.inventory.domain.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Datos de inventario ya existentes. Omitiendo carga inicial.");
            return;
        }

        log.info("Sembrando datos iniciales de prueba para el ERP...");

        Category computo = Category.builder()
                .name("Computación")
                .description("Equipos de cómputo, laptops y componentes")
                .active(true)
                .build();

        Category oficina = Category.builder()
                .name("Mobiliario y Oficina")
                .description("Suministros y muebles ergonómicos de oficina")
                .active(true)
                .build();

        Category perifericos = Category.builder()
                .name("Periféricos")
                .description("Teclados, mouse, monitores y auriculares")
                .active(true)
                .build();

        Category redes = Category.builder()
                .name("Redes y Conectividad")
                .description("Routers, switches, cableado y adaptadores")
                .active(true)
                .build();

        categoryRepository.saveAll(List.of(computo, oficina, perifericos, redes));

        List<Product> sampleProducts = List.of(
                Product.builder()
                        .sku("LAP-DELL-G15")
                        .name("Laptop Dell Gaming G15 - Core i7 16GB")
                        .description("Laptop de alto rendimiento para desarrollo y diseño")
                        .price(new BigDecimal("1250.00"))
                        .costPrice(new BigDecimal("980.00"))
                        .stockQuantity(12)
                        .minStockAlert(5)
                        .category(computo)
                        .active(true)
                        .build(),

                Product.builder()
                        .sku("LAP-THINKPAD-E14")
                        .name("Lenovo ThinkPad E14 Gen 5")
                        .description("Ultrabook corporativa con chasis reforzado")
                        .price(new BigDecimal("980.00"))
                        .costPrice(new BigDecimal("750.00"))
                        .stockQuantity(3) // Alerta de stock bajo! (3 <= 5)
                        .minStockAlert(5)
                        .category(computo)
                        .active(true)
                        .build(),

                Product.builder()
                        .sku("MON-LG-27UL500")
                        .name("Monitor LG 27'' 4K UHD IPS")
                        .description("Monitor profesional con soporte HDR10 y calibración")
                        .price(new BigDecimal("340.00"))
                        .costPrice(new BigDecimal("260.00"))
                        .stockQuantity(18)
                        .minStockAlert(4)
                        .category(perifericos)
                        .active(true)
                        .build(),

                Product.builder()
                        .sku("KBD-LOGI-MXKEYS")
                        .name("Teclado Inalámbrico Logitech MX Keys Mini")
                        .description("Teclado premium retroiluminado para productividad")
                        .price(new BigDecimal("110.00"))
                        .costPrice(new BigDecimal("75.00"))
                        .stockQuantity(2) // Alerta de stock bajo! (2 <= 5)
                        .minStockAlert(5)
                        .category(perifericos)
                        .active(true)
                        .build(),

                Product.builder()
                        .sku("ROUT-CISCO-RV340")
                        .name("Router Cisco RV340 Dual Gigabit WAN VPN")
                        .description("Router empresarial de seguridad y balanceo de carga")
                        .price(new BigDecimal("420.00"))
                        .costPrice(new BigDecimal("310.00"))
                        .stockQuantity(8)
                        .minStockAlert(3)
                        .category(redes)
                        .active(true)
                        .build(),

                Product.builder()
                        .sku("SILLA-ERG-PRO")
                        .name("Silla Ergonómica Ejecutiva Reclinable")
                        .description("Soporte lumbar ajustable con malla transpirable")
                        .price(new BigDecimal("210.00"))
                        .costPrice(new BigDecimal("140.00"))
                        .stockQuantity(15)
                        .minStockAlert(4)
                        .category(oficina)
                        .active(true)
                        .build()
        );

        for (Product product : sampleProducts) {
            Product saved = productRepository.save(product);
            StockMovement movement = StockMovement.builder()
                    .product(saved)
                    .type(MovementType.IN)
                    .quantity(saved.getStockQuantity())
                    .previousStock(0)
                    .newStock(saved.getStockQuantity())
                    .reason("Carga inicial de inventario")
                    .referenceId("DEMO-INIT")
                    .build();
            stockMovementRepository.save(movement);
        }

        log.info("Datos iniciales de categorías y productos cargados exitosamente.");
    }
}
