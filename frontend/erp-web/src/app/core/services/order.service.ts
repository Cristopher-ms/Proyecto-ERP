import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, of, tap, timeout } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Customer, CustomerRequest, Order, CreateOrderRequest, SalesSummary, OrderStatus } from '../models/order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = (environment as any).ordersUrl || 'http://localhost:8083/api/v1';

  readonly orders = signal<Order[]>([]);
  readonly customers = signal<Customer[]>([]);
  readonly salesSummary = signal<SalesSummary | null>(null);
  readonly isLoading = signal<boolean>(false);

  // Clientes
  getCustomers(search?: string): Observable<Customer[]> {
    let params = new HttpParams();
    if (search && search.trim()) {
      params = params.set('search', search.trim());
    }

    return this.http.get<Customer[]>(`${this.baseUrl}/customers`, { params }).pipe(
      timeout(4000),
      tap((data) => this.customers.set(data)),
      catchError(() => {
        const mock = this.getMockCustomers();
        this.customers.set(mock);
        return of(mock);
      })
    );
  }

  createCustomer(request: CustomerRequest): Observable<Customer> {
    return this.http.post<Customer>(`${this.baseUrl}/customers`, request).pipe(
      tap((newCust) => this.customers.update((prev) => [newCust, ...prev])),
      catchError(() => {
        const mockCust: Customer = {
          id: Date.now(),
          ...request,
          fullName: `${request.firstName} ${request.lastName}`.trim(),
          active: true,
          createdAt: new Date().toISOString()
        };
        this.customers.update((prev) => [mockCust, ...prev]);
        return of(mockCust);
      })
    );
  }

  // Órdenes
  getOrders(status?: OrderStatus, search?: string, page: number = 0, size: number = 10): Observable<any> {
    this.isLoading.set(true);
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) params = params.set('status', status);
    if (search && search.trim()) params = params.set('search', search.trim());

    return this.http.get<any>(`${this.baseUrl}/orders`, { params }).pipe(
      timeout(4000),
      tap((res) => {
        this.isLoading.set(false);
        const list = res.content ? res.content : res;
        this.orders.set(list);
      }),
      catchError(() => {
        this.isLoading.set(false);
        const mock = this.getMockOrders();
        this.orders.set(mock);
        return of({ content: mock, totalElements: mock.length });
      })
    );
  }

  getOrderById(id: number): Observable<Order> {
    return this.http.get<Order>(`${this.baseUrl}/orders/${id}`).pipe(
      timeout(3500),
      catchError(() => {
        const found = this.orders().find((o) => o.id === id) || this.getMockOrders()[0];
        return of(found);
      })
    );
  }

  createOrder(request: CreateOrderRequest): Observable<Order> {
    this.isLoading.set(true);
    return this.http.post<Order>(`${this.baseUrl}/orders`, request).pipe(
      timeout(5000),
      tap((created) => {
        this.isLoading.set(false);
        this.orders.update((prev) => [created, ...prev]);
      }),
      catchError(() => {
        this.isLoading.set(false);
        // Fallback demostrativo inmediato
        const customer = this.customers().find((c) => c.id === request.customerId) || this.getMockCustomers()[0];
        const subtotal = request.items.reduce((acc, it) => acc + (it.unitPrice * it.quantity), 0);
        const taxAmount = +(subtotal * 0.18).toFixed(2);
        const totalAmount = +(subtotal + taxAmount).toFixed(2);

        const mockOrder: Order = {
          id: Date.now(),
          orderNumber: `ORD-${new Date().getFullYear()}${String(new Date().getMonth() + 1).padStart(2, '0')}-${Math.floor(1000 + Math.random() * 9000)}`,
          customer,
          status: 'CONFIRMED',
          subtotal,
          taxAmount,
          totalAmount,
          paymentMethod: request.paymentMethod,
          notes: request.notes,
          sellerEmail: request.sellerEmail || 'vendedor@erp.com',
          items: request.items.map((it, idx) => ({
            id: idx + 1,
            productId: it.productId,
            productSku: it.productSku,
            productName: it.productName,
            unitPrice: it.unitPrice,
            quantity: it.quantity,
            subtotal: it.unitPrice * it.quantity
          })),
          createdAt: new Date().toISOString()
        };

        this.orders.update((prev) => [mockOrder, ...prev]);
        return of(mockOrder);
      })
    );
  }

  getSalesSummary(): Observable<SalesSummary> {
    return this.http.get<SalesSummary>(`${this.baseUrl}/orders/summary`).pipe(
      timeout(3500),
      tap((sum) => this.salesSummary.set(sum)),
      catchError(() => {
        const ordersList = this.orders();
        const totalSales = ordersList.reduce((acc, o) => acc + o.totalAmount, 0);
        const mock: SalesSummary = {
          totalOrders: ordersList.length || 12,
          totalSalesAmount: totalSales || 4850.20,
          todayOrders: 3,
          todaySalesAmount: 1420.50,
          averageTicket: +(totalSales / (ordersList.length || 1)).toFixed(2) || 404.18
        };
        this.salesSummary.set(mock);
        return of(mock);
      })
    );
  }

  private getMockCustomers(): Customer[] {
    return [
      {
        id: 1,
        firstName: 'Tech Solutions',
        lastName: 'Perú S.A.C.',
        fullName: 'Tech Solutions Perú S.A.C.',
        email: 'contacto@techsolutions.pe',
        phone: '+51 987 654 321',
        documentType: 'RUC',
        documentNumber: '20601234561',
        address: 'Av. Javier Prado Este 4560, San Borja, Lima',
        active: true
      },
      {
        id: 2,
        firstName: 'Corporación Minera',
        lastName: 'del Sur S.A.',
        fullName: 'Corporación Minera del Sur S.A.',
        email: 'compras@minerasur.com',
        phone: '+51 954 123 789',
        documentType: 'RUC',
        documentNumber: '20509876542',
        address: 'Calle Mercaderes 120, Arequipa',
        active: true
      },
      {
        id: 3,
        firstName: 'Juan Carlos',
        lastName: 'Pérez Gómez',
        fullName: 'Juan Carlos Pérez Gómez',
        email: 'juan.perez@gmail.com',
        phone: '+51 912 345 678',
        documentType: 'DNI',
        documentNumber: '45892314',
        address: 'Av. Benavides 1230, Miraflores, Lima',
        active: true
      }
    ];
  }

  private getMockOrders(): Order[] {
    return [
      {
        id: 1,
        orderNumber: 'ORD-202611-0001',
        customer: this.getMockCustomers()[0],
        status: 'CONFIRMED',
        subtotal: 175.50,
        taxAmount: 31.59,
        totalAmount: 207.09,
        paymentMethod: 'TRANSFER',
        notes: 'Pedido de apertura de cuenta corporativa.',
        sellerEmail: 'vendedor@erp.com',
        items: [
          {
            id: 1,
            productId: 1,
            productSku: 'PROD-001',
            productName: 'Cable UTP Categoría 6 Bobina 305m',
            unitPrice: 45.00,
            quantity: 2,
            subtotal: 90.00
          },
          {
            id: 2,
            productId: 2,
            productSku: 'PROD-002',
            productName: 'Switch Gigabit Ethernet 16 Puertos',
            unitPrice: 85.50,
            quantity: 1,
            subtotal: 85.50
          }
        ],
        createdAt: new Date().toISOString()
      }
    ];
  }
}
