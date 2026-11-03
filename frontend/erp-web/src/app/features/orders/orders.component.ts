import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../core/services/order.service';
import { ProductService } from '../../core/services/product.service';
import { AuthService } from '../../core/services/auth.service';
import { Order, Customer, OrderStatus, PaymentMethod, CreateOrderItemRequest, CustomerRequest } from '../../core/models/order.model';
import { Product } from '../../core/models/product.model';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {
  readonly orderService = inject(OrderService);
  private readonly productService = inject(ProductService);
  private readonly authService = inject(AuthService);

  activeTab: 'list' | 'new' = 'list';

  // Filtros
  statusFilter: string = '';
  searchFilter: string = '';

  // Datos para nueva venta (POS)
  availableProducts = signal<Product[]>([]);
  cart = signal<CreateOrderItemRequest[]>([]);
  selectedCustomerId: number | null = null;
  selectedPaymentMethod: PaymentMethod = 'TRANSFER';
  orderNotes: string = '';

  // Modal de detalle de orden
  selectedOrder = signal<Order | null>(null);

  // Modal de nuevo cliente
  showCustomerModal: boolean = false;
  newCustomer: CustomerRequest = {
    firstName: '',
    lastName: '',
    documentType: 'RUC',
    documentNumber: '',
    email: '',
    phone: '',
    address: ''
  };

  // Notificación de éxito
  successMessage: string = '';
  errorMessage: string = '';

  ngOnInit(): void {
    this.loadOrders();
    this.loadCustomers();
    this.loadProducts();
    this.orderService.getSalesSummary().subscribe();
  }

  loadOrders(): void {
    const status = this.statusFilter ? (this.statusFilter as OrderStatus) : undefined;
    this.orderService.getOrders(status, this.searchFilter).subscribe();
  }

  loadCustomers(): void {
    this.orderService.getCustomers().subscribe((customers) => {
      if (customers.length > 0 && !this.selectedCustomerId) {
        this.selectedCustomerId = customers[0].id;
      }
    });
  }

  loadProducts(): void {
    this.productService.getProducts().subscribe((res: any) => {
      const items = res.content ? res.content : res;
      this.availableProducts.set(items);
    });
  }

  // Lógica de Carrito POS
  addToCart(product: Product): void {
    if (product.stockQuantity <= 0) {
      this.errorMessage = `El producto "${product.name}" no tiene existencias disponibles.`;
      setTimeout(() => this.errorMessage = '', 4000);
      return;
    }

    const currentCart = this.cart();
    const existingIndex = currentCart.findIndex((item) => item.productId === product.id);

    if (existingIndex > -1) {
      const currentQty = currentCart[existingIndex].quantity;
      if (currentQty + 1 > product.stockQuantity) {
        this.errorMessage = `No puedes agregar más unidades que el stock disponible (${product.stockQuantity}).`;
        setTimeout(() => this.errorMessage = '', 4000);
        return;
      }
      currentCart[existingIndex].quantity += 1;
      this.cart.set([...currentCart]);
    } else {
      const newItem: CreateOrderItemRequest = {
        productId: product.id,
        productSku: product.sku,
        productName: product.name,
        unitPrice: product.price,
        quantity: 1
      };
      this.cart.set([...currentCart, newItem]);
    }
  }

  updateQuantity(index: number, delta: number): void {
    const current = this.cart();
    const item = current[index];
    const product = this.availableProducts().find(p => p.id === item.productId);

    const newQty = item.quantity + delta;
    if (newQty <= 0) {
      this.removeFromCart(index);
      return;
    }

    if (product && newQty > product.stockQuantity) {
      this.errorMessage = `Límite de stock alcanzado (${product.stockQuantity} unidades).`;
      setTimeout(() => this.errorMessage = '', 3500);
      return;
    }

    item.quantity = newQty;
    this.cart.set([...current]);
  }

  removeFromCart(index: number): void {
    const current = this.cart();
    current.splice(index, 1);
    this.cart.set([...current]);
  }

  clearCart(): void {
    this.cart.set([]);
  }

  getCartSubtotal(): number {
    return this.cart().reduce((acc, it) => acc + (it.unitPrice * it.quantity), 0);
  }

  getCartTax(): number {
    return +(this.getCartSubtotal() * 0.18).toFixed(2);
  }

  getCartTotal(): number {
    return +(this.getCartSubtotal() + this.getCartTax()).toFixed(2);
  }

  // Procesar venta
  submitOrder(): void {
    if (!this.selectedCustomerId) {
      this.errorMessage = 'Por favor selecciona un cliente para la orden.';
      return;
    }

    if (this.cart().length === 0) {
      this.errorMessage = 'Debes agregar al menos un producto al carrito de venta.';
      return;
    }

    const currentUser = this.authService.currentUser();
    const request = {
      customerId: this.selectedCustomerId,
      paymentMethod: this.selectedPaymentMethod,
      notes: this.orderNotes,
      sellerEmail: currentUser?.email || 'vendedor@erp.com',
      items: this.cart()
    };

    this.orderService.createOrder(request).subscribe({
      next: (created) => {
        this.successMessage = `¡Venta procesada con éxito! Orden ${created.orderNumber} confirmada. Evento asíncrono emitido a RabbitMQ.`;
        this.clearCart();
        this.orderNotes = '';
        this.activeTab = 'list';
        this.loadOrders();
        this.orderService.getSalesSummary().subscribe();
        setTimeout(() => this.successMessage = '', 7000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Error al procesar la orden.';
        setTimeout(() => this.errorMessage = '', 5000);
      }
    });
  }

  // Crear cliente rápido desde el modal
  saveCustomer(): void {
    if (!this.newCustomer.firstName || !this.newCustomer.lastName || !this.newCustomer.documentNumber) {
      this.errorMessage = 'Nombre, apellido y documento son obligatorios.';
      return;
    }

    this.orderService.createCustomer(this.newCustomer).subscribe({
      next: (created) => {
        this.selectedCustomerId = created.id;
        this.showCustomerModal = false;
        this.newCustomer = {
          firstName: '',
          lastName: '',
          documentType: 'RUC',
          documentNumber: '',
          email: '',
          phone: '',
          address: ''
        };
        this.successMessage = `Cliente "${created.fullName}" registrado exitosamente.`;
        setTimeout(() => this.successMessage = '', 4000);
      }
    });
  }

  viewOrderDetail(order: Order): void {
    this.selectedOrder.set(order);
  }

  closeDetailModal(): void {
    this.selectedOrder.set(null);
  }
}
