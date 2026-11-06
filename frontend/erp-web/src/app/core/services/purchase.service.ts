import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, of } from 'rxjs';
import { catchError, timeout, tap } from 'rxjs/operators';
import {
  Supplier, SupplierRequest, PurchaseOrder,
  CreatePurchaseRequest, PurchasesSummary
} from '../models/purchase.model';

// ── Mock corporativo de respaldo ─────────────────────────────────────────────
const MOCK_SUPPLIERS: Supplier[] = [
  {
    id: 1, companyName: 'Tech Distribuciones S.A.C.', taxId: '20501234567',
    contactName: 'Carlos Mendoza', email: 'ventas@techdist.pe', phone: '+51 1 234-5678',
    address: 'Av. Javier Prado 2680, San Borja, Lima', country: 'Perú',
    paymentTerms: 'NET_30', status: 'ACTIVE',
    notes: 'Proveedor principal de equipos tecnológicos.'
  },
  {
    id: 2, companyName: 'Importadora Global E.I.R.L.', taxId: '20601234568',
    contactName: 'Ana Torres', email: 'compras@importglobal.com', phone: '+51 1 345-6789',
    address: 'Calle Los Álamos 450, Miraflores, Lima', country: 'Perú',
    paymentTerms: 'NET_60', status: 'ACTIVE',
    notes: 'Importadora de electrónica desde Asia.'
  },
  {
    id: 3, companyName: 'Distribuidora Central SAC', taxId: '20701234569',
    contactName: 'Roberto Silva', email: 'info@distcentral.pe', phone: '+51 1 456-7890',
    address: 'Jr. Huallaga 1250, Cercado de Lima', country: 'Perú',
    paymentTerms: 'NET_15', status: 'ACTIVE', notes: 'Suministros de oficina y almacén.'
  },
  {
    id: 4, companyName: 'Global Office Supply Corp.', taxId: 'US-123456789',
    contactName: 'James Wilson', email: 'orders@globaloffice.com', phone: '+1 800 555-0199',
    address: '1000 Business Pkwy, Miami, FL', country: 'Estados Unidos',
    paymentTerms: 'NET_60', status: 'ACTIVE', notes: 'Proveedor internacional de mobiliario.'
  }
];

const MOCK_PURCHASES: PurchaseOrder[] = [
  {
    id: 1, purchaseNumber: 'PO-202610-0001', supplier: MOCK_SUPPLIERS[0],
    status: 'RECEIVED', subtotal: 20500, taxAmount: 3690, totalAmount: 24190,
    items: [
      { id: 1, productId: 1, productSku: 'LAP-001', productName: 'Laptop Dell Inspiron 15', unitCost: 2800, quantity: 5, subtotal: 14000 },
      { id: 2, productId: 2, productSku: 'MON-002', productName: 'Monitor Samsung 24"', unitCost: 650, quantity: 10, subtotal: 6500 }
    ],
    expectedDeliveryDate: '2026-10-20T00:00:00', receivedDate: '2026-10-25T00:00:00',
    notes: 'Reposición inicial de laptops y monitores.', buyerEmail: 'compras@erp-empresa.pe',
    createdAt: '2026-10-10T08:00:00'
  },
  {
    id: 2, purchaseNumber: 'PO-202611-0001', supplier: MOCK_SUPPLIERS[1],
    status: 'APPROVED', subtotal: 2400, taxAmount: 432, totalAmount: 2832,
    items: [
      { id: 3, productId: 3, productSku: 'USB-003', productName: 'Hub USB-C 7 puertos', unitCost: 120, quantity: 20, subtotal: 2400 }
    ],
    expectedDeliveryDate: '2026-11-12T00:00:00',
    notes: 'Componentes electrónicos Q4.', buyerEmail: 'compras@erp-empresa.pe',
    createdAt: '2026-11-05T10:00:00'
  }
];

const MOCK_SUMMARY: PurchasesSummary = {
  totalPurchases: 2, totalAmount: 27022, pendingOrders: 1, monthlyAmount: 2832
};

@Injectable({ providedIn: 'root' })
export class PurchaseService {
  private http = inject(HttpClient);
  private baseUrl = environment.purchasesUrl;

  // ── Signals reactivos ────────────────────────────────────────────────────
  purchases   = signal<PurchaseOrder[]>([]);
  suppliers   = signal<Supplier[]>([]);
  summary     = signal<PurchasesSummary>(MOCK_SUMMARY);
  loading     = signal(false);
  usingMock   = signal(false);

  pendingCount = computed(() =>
    this.purchases().filter(p => p.status === 'DRAFT' || p.status === 'APPROVED').length
  );

  // ── Suppliers ─────────────────────────────────────────────────────────────
  loadSuppliers(search?: string): void {
    this.loading.set(true);
    const url = search ? `${this.baseUrl}/suppliers?search=${search}` : `${this.baseUrl}/suppliers`;
    this.http.get<Supplier[]>(url).pipe(
      timeout(5000),
      tap(data => { this.suppliers.set(data); this.usingMock.set(false); this.loading.set(false); }),
      catchError(() => {
        this.suppliers.set(MOCK_SUPPLIERS);
        this.usingMock.set(true);
        this.loading.set(false);
        return of(MOCK_SUPPLIERS);
      })
    ).subscribe();
  }

  createSupplier(req: SupplierRequest): Observable<Supplier> {
    const fallbackSupplier: Supplier = {
      id: Date.now(),
      companyName: req.companyName,
      taxId: req.taxId,
      contactName: req.contactName,
      email: req.email,
      phone: req.phone,
      address: req.address,
      country: req.country,
      paymentTerms: req.paymentTerms,
      status: 'ACTIVE',
      notes: req.notes
    };
    return this.http.post<Supplier>(`${this.baseUrl}/suppliers`, req).pipe(
      timeout(8000),
      tap(s => this.suppliers.update(list => [...list, s])),
      catchError(() => of(fallbackSupplier))
    );
  }

  // ── Purchase Orders ───────────────────────────────────────────────────────
  loadPurchases(): void {
    this.loading.set(true);
    this.http.get<PurchaseOrder[]>(`${this.baseUrl}/purchases`).pipe(
      timeout(5000),
      tap(data => { this.purchases.set(data); this.usingMock.set(false); this.loading.set(false); }),
      catchError(() => {
        this.purchases.set(MOCK_PURCHASES);
        this.usingMock.set(true);
        this.loading.set(false);
        return of(MOCK_PURCHASES);
      })
    ).subscribe();
  }

  createPurchase(req: CreatePurchaseRequest): Observable<PurchaseOrder> {
    return this.http.post<PurchaseOrder>(`${this.baseUrl}/purchases`, req).pipe(
      timeout(10000),
      tap(po => this.purchases.update(list => [po, ...list]))
    );
  }

  approvePurchase(id: number): Observable<PurchaseOrder> {
    return this.http.put<PurchaseOrder>(`${this.baseUrl}/purchases/${id}/approve`, {}).pipe(
      timeout(8000),
      tap(updated => this.purchases.update(list => list.map(p => p.id === id ? updated : p)))
    );
  }

  receivePurchase(id: number): Observable<PurchaseOrder> {
    return this.http.put<PurchaseOrder>(`${this.baseUrl}/purchases/${id}/receive`, {}).pipe(
      timeout(8000),
      tap(updated => this.purchases.update(list => list.map(p => p.id === id ? updated : p)))
    );
  }

  loadSummary(): void {
    this.http.get<PurchasesSummary>(`${this.baseUrl}/purchases/summary`).pipe(
      timeout(5000),
      tap(s => { this.summary.set(s); }),
      catchError(() => of(MOCK_SUMMARY))
    ).subscribe();
  }
}
