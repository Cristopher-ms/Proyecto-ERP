import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ProductService } from '../../core/services/product.service';
import { CategoryService } from '../../core/services/category.service';
import { StockService } from '../../core/services/stock.service';
import { Product, ProductRequest } from '../../core/models/product.model';
import { Category } from '../../core/models/category.model';
import { InventorySummary } from '../../core/models/inventory-summary.model';
import { StockAdjustmentRequest } from '../../core/models/stock-movement.model';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.css']
})
export class InventoryComponent implements OnInit {
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly stockService = inject(StockService);

  // Datos
  products: Product[] = [];
  categories: Category[] = [];
  summary: InventorySummary = {
    totalProducts: 0,
    lowStockAlerts: 0,
    totalUnitsInStock: 0,
    totalValuation: 0
  };

  // Filtros y paginación
  searchTerm: string = '';
  selectedCategoryId: number | undefined = undefined;
  lowStockOnly: boolean = false;
  currentPage: number = 0;
  pageSize: number = 8;
  totalPages: number = 1;
  totalElements: number = 0;

  // Estados de carga y error
  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  // Modales
  isProductModalOpen: boolean = false;
  isEditing: boolean = false;
  editingProductId: number | null = null;
  productForm: ProductRequest = this.getEmptyProductForm();

  isStockModalOpen: boolean = false;
  stockTargetProduct: Product | null = null;
  stockAdjustmentForm: StockAdjustmentRequest = {
    productId: 0,
    type: 'IN',
    quantity: 1,
    reason: '',
    referenceId: ''
  };

  ngOnInit(): void {
    this.loadCategories();
    this.loadSummary();
    this.loadProducts();
  }

  loadSummary(): void {
    this.productService.getSummary().subscribe({
      next: (data) => this.summary = data,
      error: (err) => console.error('Error cargando KPIs:', err)
    });
  }

  loadCategories(): void {
    this.categoryService.getActiveCategories().subscribe({
      next: (data) => this.categories = data,
      error: (err) => console.error('Error cargando categorías:', err)
    });
  }

  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.productService.getProducts(
      this.searchTerm,
      this.selectedCategoryId,
      this.lowStockOnly,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (response) => {
        this.products = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'No se pudo conectar con el microservicio de inventario. Verifica que esté en ejecución en el puerto 8082.';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  toggleLowStock(): void {
    this.lowStockOnly = !this.lowStockOnly;
    this.currentPage = 0;
    this.loadProducts();
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadProducts();
    }
  }

  // --- Modal de Producto ---
  openCreateModal(): void {
    this.isEditing = false;
    this.editingProductId = null;
    this.productForm = this.getEmptyProductForm();
    if (this.categories.length > 0) {
      this.productForm.categoryId = this.categories[0].id;
    }
    this.isProductModalOpen = true;
  }

  openEditModal(product: Product): void {
    this.isEditing = true;
    this.editingProductId = product.id;
    this.productForm = {
      sku: product.sku,
      name: product.name,
      description: product.description || '',
      price: product.price,
      costPrice: product.costPrice || 0,
      stockQuantity: product.stockQuantity,
      minStockAlert: product.minStockAlert,
      categoryId: product.category?.id || (this.categories[0]?.id || 1),
      active: product.active
    };
    this.isProductModalOpen = true;
  }

  closeProductModal(): void {
    this.isProductModalOpen = false;
    this.editingProductId = null;
  }

  saveProduct(): void {
    if (!this.productForm.sku || !this.productForm.name || !this.productForm.price) {
      alert('Por favor complete los campos obligatorios: SKU, Nombre y Precio.');
      return;
    }

    if (this.isEditing && this.editingProductId) {
      this.productService.updateProduct(this.editingProductId, this.productForm).subscribe({
        next: () => {
          this.showSuccess('Producto actualizado exitosamente.');
          this.closeProductModal();
          this.loadProducts();
          this.loadSummary();
        },
        error: (err) => alert(err.error?.message || 'Error al actualizar el producto.')
      });
    } else {
      this.productService.createProduct(this.productForm).subscribe({
        next: () => {
          this.showSuccess('Producto creado exitosamente con su registro de inventario inicial.');
          this.closeProductModal();
          this.loadProducts();
          this.loadSummary();
        },
        error: (err) => alert(err.error?.message || 'Error al crear el producto.')
      });
    }
  }

  deleteProduct(product: Product): void {
    if (confirm(`¿Estás seguro de desactivar el producto "${product.name}" (${product.sku})?`)) {
      this.productService.deleteProduct(product.id).subscribe({
        next: () => {
          this.showSuccess('Producto desactivado correctamente.');
          this.loadProducts();
          this.loadSummary();
        },
        error: (err) => alert('Error al eliminar el producto: ' + err.message)
      });
    }
  }

  // --- Modal de Ajuste de Stock ---
  openStockModal(product: Product): void {
    this.stockTargetProduct = product;
    this.stockAdjustmentForm = {
      productId: product.id,
      type: 'IN',
      quantity: 1,
      reason: 'Entrada de mercancía',
      referenceId: 'MANUAL-' + Date.now().toString().slice(-4)
    };
    this.isStockModalOpen = true;
  }

  closeStockModal(): void {
    this.isStockModalOpen = false;
    this.stockTargetProduct = null;
  }

  submitStockAdjustment(): void {
    if (!this.stockAdjustmentForm.quantity || this.stockAdjustmentForm.quantity < 1) {
      alert('La cantidad debe ser mayor a 0');
      return;
    }

    this.stockService.adjustStock(this.stockAdjustmentForm).subscribe({
      next: () => {
        this.showSuccess('Ajuste de inventario aplicado exitosamente.');
        this.closeStockModal();
        this.loadProducts();
        this.loadSummary();
      },
      error: (err) => alert(err.error?.message || 'Error al ajustar el inventario.')
    });
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => this.successMessage = '', 4000);
  }

  private getEmptyProductForm(): ProductRequest {
    return {
      sku: '',
      name: '',
      description: '',
      price: 0,
      costPrice: 0,
      stockQuantity: 0,
      minStockAlert: 5,
      categoryId: 1,
      active: true
    };
  }
}
