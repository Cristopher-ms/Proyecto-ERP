import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, of, timeout } from 'rxjs';
import { Product, ProductRequest } from '../models/product.model';
import { PaginatedResponse } from '../models/paginated-response.model';
import { InventorySummary } from '../models/inventory-summary.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/products`;

  getProducts(
    search?: string,
    categoryId?: number,
    lowStockOnly: boolean = false,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'name',
    direction: string = 'ASC'
  ): Observable<PaginatedResponse<Product>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('direction', direction)
      .set('lowStockOnly', lowStockOnly.toString());

    if (search && search.trim() !== '') {
      params = params.set('search', search.trim());
    }

    if (categoryId) {
      params = params.set('categoryId', categoryId.toString());
    }

    return this.http.get<PaginatedResponse<Product>>(this.apiUrl, { params }).pipe(
      timeout(4500),
      catchError((err) => {
        console.warn('Backend de inventario en arranque en frío o no alcanzable. Usando catálogo de respaldo resiliente:', err);
        return of(this.getMockProductsResponse());
      })
    );
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`).pipe(
      timeout(3500),
      catchError(() => of(this.getMockProducts().find(p => p.id === id) || this.getMockProducts()[0]))
    );
  }

  getProductBySku(sku: string): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/sku/${sku}`).pipe(
      timeout(3500),
      catchError(() => of(this.getMockProducts().find(p => p.sku === sku) || this.getMockProducts()[0]))
    );
  }

  getSummary(): Observable<InventorySummary> {
    return this.http.get<InventorySummary>(`${this.apiUrl}/summary`).pipe(
      timeout(3500),
      catchError(() => of({
        totalProducts: 8,
        lowStockAlerts: 1,
        totalUnitsInStock: 245,
        totalValuation: 28450.00
      }))
    );
  }

  createProduct(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(this.apiUrl, request);
  }

  updateProduct(id: number, request: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${this.apiUrl}/${id}`, request);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  private getMockProductsResponse(): PaginatedResponse<Product> {
    const list = this.getMockProducts();
    return {
      content: list,
      pageNumber: 0,
      pageSize: 10,
      totalElements: list.length,
      totalPages: 1,
      last: true
    };
  }

  private getMockProducts(): Product[] {
    return [
      {
        id: 1,
        sku: 'COMP-001',
        name: 'Laptop Dell Latitude 5440 i7 16GB',
        description: 'Laptop corporativa de alto rendimiento',
        price: 1250.00,
        costPrice: 950.00,
        stockQuantity: 15,
        minStockAlert: 5,
        category: { id: 1, name: 'Computación', description: 'Equipos de cómputo', active: true },
        active: true,
        isLowStock: false
      },
      {
        id: 2,
        sku: 'PERIF-001',
        name: 'Monitor Dell 27 4K UHD UltraSharp',
        description: 'Monitor profesional con panel IPS',
        price: 450.00,
        costPrice: 320.00,
        stockQuantity: 28,
        minStockAlert: 8,
        category: { id: 3, name: 'Periféricos', description: 'Monitores y accesorios', active: true },
        active: true,
        isLowStock: false
      },
      {
        id: 3,
        sku: 'RED-001',
        name: 'Switch Cisco Gigabit 24 Puertos PoE',
        description: 'Switch administrable de capa 2/3',
        price: 890.00,
        costPrice: 650.00,
        stockQuantity: 8,
        minStockAlert: 3,
        category: { id: 4, name: 'Redes y Conectividad', description: 'Equipamiento de redes', active: true },
        active: true,
        isLowStock: false
      },
      {
        id: 4,
        sku: 'RED-002',
        name: 'Access Point Ubiquiti UniFi 6 Pro',
        description: 'Punto de acceso WiFi 6 empresarial',
        price: 195.00,
        costPrice: 140.00,
        stockQuantity: 42,
        minStockAlert: 10,
        category: { id: 4, name: 'Redes y Conectividad', description: 'Equipamiento de redes', active: true },
        active: true,
        isLowStock: false
      },
      {
        id: 5,
        sku: 'OFIC-001',
        name: 'Silla Ergonómica Herman Miller Aeron',
        description: 'Silla de oficina ergonómica ajustable',
        price: 1100.00,
        costPrice: 800.00,
        stockQuantity: 12,
        minStockAlert: 4,
        category: { id: 2, name: 'Mobiliario y Oficina', description: 'Muebles de oficina', active: true },
        active: true,
        isLowStock: false
      },
      {
        id: 6,
        sku: 'PERIF-002',
        name: 'Teclado Mecánico Logitech MX Mechanical',
        description: 'Teclado inalámbrico retroiluminado',
        price: 165.00,
        costPrice: 115.00,
        stockQuantity: 3,
        minStockAlert: 5,
        category: { id: 3, name: 'Periféricos', description: 'Monitores y accesorios', active: true },
        active: true,
        isLowStock: true
      }
    ];
  }
}
