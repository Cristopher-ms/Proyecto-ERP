package com.erp.inventory.service;

import com.erp.inventory.domain.entity.Category;
import com.erp.inventory.domain.entity.Product;
import com.erp.inventory.domain.entity.StockMovement;
import com.erp.inventory.domain.enums.MovementType;
import com.erp.inventory.domain.repository.CategoryRepository;
import com.erp.inventory.domain.repository.ProductRepository;
import com.erp.inventory.domain.repository.StockMovementRepository;
import com.erp.inventory.dto.*;
import com.erp.inventory.exception.DuplicateResourceException;
import com.erp.inventory.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public PaginatedResponse<ProductResponse> getProducts(
            String search,
            Long categoryId,
            boolean lowStockOnly,
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction != null ? direction : "ASC"),
                            sortBy != null ? sortBy : "name");
        Pageable pageable = PageRequest.of(page, size, sort);

        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        Page<Product> productPage = productRepository.findProductsWithFilters(trimmedSearch, categoryId, lowStockOnly, pageable);

        Page<ProductResponse> dtoPage = productPage.map(this::mapToResponse);
        return PaginatedResponse.from(dtoPage);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return mapToResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con SKU: " + sku));
        return mapToResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        String cleanSku = request.getSku().trim().toUpperCase();
        if (productRepository.existsBySku(cleanSku)) {
            throw new DuplicateResourceException("Ya existe un producto con el código SKU: " + cleanSku);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoryId()));

        int initialStock = request.getStockQuantity() != null ? request.getStockQuantity() : 0;

        Product product = Product.builder()
                .sku(cleanSku)
                .name(request.getName().trim())
                .description(request.getDescription())
                .price(request.getPrice())
                .costPrice(request.getCostPrice() != null ? request.getCostPrice() : BigDecimal.ZERO)
                .stockQuantity(initialStock)
                .minStockAlert(request.getMinStockAlert() != null ? request.getMinStockAlert() : 5)
                .category(category)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Product savedProduct = productRepository.save(product);

        // Registro de movimiento inicial de inventario si el stock es mayor a 0
        if (initialStock > 0) {
            StockMovement movement = StockMovement.builder()
                    .product(savedProduct)
                    .type(MovementType.IN)
                    .quantity(initialStock)
                    .previousStock(0)
                    .newStock(initialStock)
                    .reason("Inventario Inicial")
                    .referenceId("INIT-" + savedProduct.getSku())
                    .build();
            stockMovementRepository.save(movement);
        }

        return mapToResponse(savedProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        String cleanSku = request.getSku().trim().toUpperCase();
        if (productRepository.existsBySkuAndIdNot(cleanSku, id)) {
            throw new DuplicateResourceException("Ya existe otro producto con el código SKU: " + cleanSku);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoryId()));

        product.setSku(cleanSku);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        if (request.getCostPrice() != null) {
            product.setCostPrice(request.getCostPrice());
        }
        if (request.getMinStockAlert() != null) {
            product.setMinStockAlert(request.getMinStockAlert());
        }
        product.setCategory(category);
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public InventorySummaryResponse getInventorySummary() {
        long totalProducts = productRepository.countActiveProducts();
        long lowStock = productRepository.countLowStockProducts();
        long totalUnits = productRepository.sumTotalStockQuantity();
        BigDecimal totalValuation = productRepository.calculateTotalStockValuation();

        return InventorySummaryResponse.builder()
                .totalProducts(totalProducts)
                .lowStockAlerts(lowStock)
                .totalUnitsInStock(totalUnits)
                .totalValuation(totalValuation)
                .build();
    }

    public ProductResponse mapToResponse(Product product) {
        if (product == null) return null;
        boolean isLowStock = product.getStockQuantity() <= product.getMinStockAlert();

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .stockQuantity(product.getStockQuantity())
                .minStockAlert(product.getMinStockAlert())
                .category(categoryService.mapToResponse(product.getCategory()))
                .active(product.getActive())
                .isLowStock(isLowStock)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
