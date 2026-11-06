package com.erp.purchases.service;

import com.erp.purchases.client.InventoryServiceClient;
import com.erp.purchases.domain.entity.PurchaseItem;
import com.erp.purchases.domain.entity.PurchaseOrder;
import com.erp.purchases.domain.entity.Supplier;
import com.erp.purchases.domain.enums.PurchaseStatus;
import com.erp.purchases.domain.repository.PurchaseOrderRepository;
import com.erp.purchases.domain.repository.SupplierRepository;
import com.erp.purchases.dto.*;
import com.erp.purchases.event.PurchaseEventPublisher;
import com.erp.purchases.event.StockReplenishEvent;
import com.erp.purchases.event.StockReplenishItemEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private static final BigDecimal IGV = new BigDecimal("0.18");

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final SupplierService supplierService;
    private final InventoryServiceClient inventoryServiceClient;
    private final PurchaseEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchases() {
        return purchaseOrderRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional
    public PurchaseOrderResponse createPurchase(CreatePurchaseRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado: " + request.supplierId()));

        String purchaseNumber = generatePurchaseNumber();

        PurchaseOrder po = PurchaseOrder.builder()
                .purchaseNumber(purchaseNumber)
                .supplier(supplier)
                .status(PurchaseStatus.DRAFT)
                .expectedDeliveryDate(request.expectedDeliveryDate())
                .notes(request.notes())
                .buyerEmail(request.buyerEmail())
                .build();

        List<PurchaseItem> items = request.items().stream().map(itemReq -> {
            BigDecimal subtotal = itemReq.unitCost()
                    .multiply(BigDecimal.valueOf(itemReq.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            return PurchaseItem.builder()
                    .purchaseOrder(po)
                    .productId(itemReq.productId())
                    .productSku(itemReq.productSku())
                    .productName(itemReq.productName())
                    .unitCost(itemReq.unitCost())
                    .quantity(itemReq.quantity())
                    .subtotal(subtotal)
                    .build();
        }).collect(Collectors.toList());

        po.setItems(items);

        BigDecimal subtotal = items.stream()
                .map(PurchaseItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxAmount = subtotal.multiply(IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        po.setSubtotal(subtotal);
        po.setTaxAmount(taxAmount);
        po.setTotalAmount(totalAmount);

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        log.info("OC creada: {} por S/ {}", purchaseNumber, totalAmount);
        return toResponse(saved);
    }

    @Transactional
    public PurchaseOrderResponse approvePurchase(Long id) {
        PurchaseOrder po = findOrThrow(id);
        if (po.getStatus() != PurchaseStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden aprobar OCs en estado DRAFT");
        }
        po.setStatus(PurchaseStatus.APPROVED);
        log.info("OC aprobada: {}", po.getPurchaseNumber());
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderResponse receivePurchase(Long id) {
        PurchaseOrder po = findOrThrow(id);
        if (po.getStatus() != PurchaseStatus.APPROVED) {
            throw new IllegalStateException("Solo se pueden recibir OCs en estado APPROVED");
        }
        po.setStatus(PurchaseStatus.RECEIVED);
        po.setReceivedDate(LocalDateTime.now());

        // Reposición de stock vía inventory-service (resiliente)
        po.getItems().forEach(item -> {
            try {
                inventoryServiceClient.addStock(item.getProductId(), item.getQuantity(), po.getPurchaseNumber());
                log.info("Stock repuesto: productId={} +{}", item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.warn("No se pudo reponer stock para productId={}: {}", item.getProductId(), e.getMessage());
            }
        });

        // Publicar evento de reposición
        List<StockReplenishItemEvent> eventItems = po.getItems().stream()
                .map(i -> new StockReplenishItemEvent(i.getProductId(), i.getProductSku(), i.getQuantity()))
                .collect(Collectors.toList());
        eventPublisher.publishStockReplenish(
                new StockReplenishEvent(po.getPurchaseNumber(), po.getSupplier().getId(), eventItems));

        log.info("OC recibida: {}", po.getPurchaseNumber());
        return toResponse(purchaseOrderRepository.save(po));
    }

    @Transactional(readOnly = true)
    public PurchasesSummaryResponse getSummary() {
        long total = purchaseOrderRepository.count();
        BigDecimal totalAmount = purchaseOrderRepository.sumTotalPurchases();
        long pending = purchaseOrderRepository.countPending();
        BigDecimal monthly = purchaseOrderRepository.sumPurchasesSince(
                LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0));

        return new PurchasesSummaryResponse(
                total,
                totalAmount != null ? totalAmount : BigDecimal.ZERO,
                pending,
                monthly != null ? monthly : BigDecimal.ZERO
        );
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private PurchaseOrder findOrThrow(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OC no encontrada con id: " + id));
    }

    private String generatePurchaseNumber() {
        String yyyymm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = purchaseOrderRepository.count() + 1;
        return String.format("PO-%s-%04d", yyyymm, count);
    }

    private PurchaseOrderResponse toResponse(PurchaseOrder po) {
        List<PurchaseItemResponse> items = po.getItems() == null ? List.of() :
                po.getItems().stream().map(i -> new PurchaseItemResponse(
                        i.getId(), i.getProductId(), i.getProductSku(),
                        i.getProductName(), i.getUnitCost(), i.getQuantity(), i.getSubtotal()
                )).collect(Collectors.toList());

        return new PurchaseOrderResponse(
                po.getId(),
                po.getPurchaseNumber(),
                supplierService.toResponse(po.getSupplier()),
                po.getStatus(),
                po.getSubtotal(),
                po.getTaxAmount(),
                po.getTotalAmount(),
                items,
                po.getExpectedDeliveryDate(),
                po.getReceivedDate(),
                po.getNotes(),
                po.getBuyerEmail(),
                po.getCreatedAt()
        );
    }
}
