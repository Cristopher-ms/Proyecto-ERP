package com.erp.inventory.service;

import com.erp.inventory.domain.entity.Category;
import com.erp.inventory.domain.entity.Product;
import com.erp.inventory.domain.repository.CategoryRepository;
import com.erp.inventory.domain.repository.ProductRepository;
import com.erp.inventory.domain.repository.StockMovementRepository;
import com.erp.inventory.dto.CategoryResponse;
import com.erp.inventory.dto.ProductRequest;
import com.erp.inventory.dto.ProductResponse;
import com.erp.inventory.exception.DuplicateResourceException;
import com.erp.inventory.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electrónica")
                .description("Dispositivos electrónicos")
                .active(true)
                .build();

        testProduct = Product.builder()
                .id(100L)
                .sku("TEST-SKU-001")
                .name("Producto de Prueba")
                .description("Descripción de prueba")
                .price(new BigDecimal("150.00"))
                .costPrice(new BigDecimal("100.00"))
                .stockQuantity(10)
                .minStockAlert(5)
                .category(testCategory)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Debe crear un producto correctamente cuando el SKU es único")
    void shouldCreateProductSuccessfully() {
        ProductRequest request = ProductRequest.builder()
                .sku("TEST-SKU-001")
                .name("Producto de Prueba")
                .description("Descripción de prueba")
                .price(new BigDecimal("150.00"))
                .costPrice(new BigDecimal("100.00"))
                .stockQuantity(10)
                .minStockAlert(5)
                .categoryId(1L)
                .build();

        when(productRepository.existsBySku("TEST-SKU-001")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(categoryService.mapToResponse(any())).thenReturn(
                CategoryResponse.builder().id(1L).name("Electrónica").build()
        );

        ProductResponse response = productService.createProduct(request);

        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo("TEST-SKU-001");
        assertThat(response.getName()).isEqualTo("Producto de Prueba");
        verify(productRepository).save(any(Product.class));
        verify(stockMovementRepository).save(any());
    }

    @Test
    @DisplayName("Debe lanzar DuplicateResourceException si el SKU ya existe")
    void shouldThrowExceptionWhenSkuAlreadyExists() {
        ProductRequest request = ProductRequest.builder()
                .sku("TEST-SKU-001")
                .name("Producto Duplicado")
                .price(new BigDecimal("150.00"))
                .categoryId(1L)
                .build();

        when(productRepository.existsBySku("TEST-SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("TEST-SKU-001");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el producto no existe por ID")
    void shouldThrowExceptionWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
