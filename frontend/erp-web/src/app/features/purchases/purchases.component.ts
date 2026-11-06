import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PurchaseService } from '../../core/services/purchase.service';
import { PurchaseOrder, Supplier, PurchaseStatus } from '../../core/models/purchase.model';

@Component({
  selector: 'app-purchases',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './purchases.component.html',
  styleUrls: ['./purchases.component.css']
})
export class PurchasesComponent implements OnInit {
  purchaseService = inject(PurchaseService);
  fb = inject(FormBuilder);

  // ── Estado de UI ─────────────────────────────────────────────────────────
  activeTab    = signal<'orders' | 'suppliers' | 'new'>('orders');
  actionMsg    = signal<{ text: string; type: 'success' | 'error' } | null>(null);
  supplierSearch = signal('');

  // ── Formulario nueva OC ──────────────────────────────────────────────────
  newOrderForm!: FormGroup;

  // ── Computed ─────────────────────────────────────────────────────────────
  filteredSuppliers = computed(() => {
    const q = this.supplierSearch().toLowerCase();
    return this.purchaseService.suppliers().filter(s =>
      !q || s.companyName.toLowerCase().includes(q) || s.taxId.includes(q)
    );
  });

  summary = this.purchaseService.summary;

  ngOnInit(): void {
    this.purchaseService.loadPurchases();
    this.purchaseService.loadSuppliers();
    this.purchaseService.loadSummary();
    this.initForm();
  }

  private initForm(): void {
    this.newOrderForm = this.fb.group({
      supplierId:          [null, Validators.required],
      buyerEmail:          ['compras@erp-empresa.pe', [Validators.required, Validators.email]],
      notes:               [''],
      expectedDeliveryDate:[''],
      // Línea de item único simplificada
      productSku:    ['', Validators.required],
      productName:   ['', Validators.required],
      productId:     [1],
      unitCost:      [0, [Validators.required, Validators.min(0.01)]],
      quantity:      [1,  [Validators.required, Validators.min(1)]]
    });
  }

  setTab(tab: 'orders' | 'suppliers' | 'new'): void {
    this.activeTab.set(tab);
  }

  // ── Acciones sobre OC ────────────────────────────────────────────────────
  approve(po: PurchaseOrder): void {
    if (!confirm(`¿Aprobar la OC ${po.purchaseNumber}?`)) return;
    this.purchaseService.approvePurchase(po.id).subscribe({
      next: () => this.showMsg(`OC ${po.purchaseNumber} aprobada ✔`, 'success'),
      error: () => this.showMsg('Error al aprobar la OC', 'error')
    });
  }

  receive(po: PurchaseOrder): void {
    if (!confirm(`¿Marcar como recibida la OC ${po.purchaseNumber}? Esto repondrá stock automáticamente.`)) return;
    this.purchaseService.receivePurchase(po.id).subscribe({
      next: () => this.showMsg(`OC ${po.purchaseNumber} recibida. Stock repuesto ✔`, 'success'),
      error: () => this.showMsg('Error al recibir la OC', 'error')
    });
  }

  submitNewOrder(): void {
    if (this.newOrderForm.invalid) { this.newOrderForm.markAllAsTouched(); return; }
    const v = this.newOrderForm.value;
    const req = {
      supplierId: +v.supplierId,
      buyerEmail: v.buyerEmail,
      notes: v.notes,
      expectedDeliveryDate: v.expectedDeliveryDate || undefined,
      items: [{
        productId:   +v.productId,
        productSku:  v.productSku,
        productName: v.productName,
        unitCost:    +v.unitCost,
        quantity:    +v.quantity
      }]
    };
    this.purchaseService.createPurchase(req).subscribe({
      next: () => {
        this.showMsg('Orden de Compra creada en estado DRAFT ✔', 'success');
        this.newOrderForm.reset({ buyerEmail: 'compras@erp-empresa.pe', productId: 1, quantity: 1 });
        this.activeTab.set('orders');
      },
      error: () => this.showMsg('Error al crear la OC', 'error')
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  statusLabel(s: PurchaseStatus): string {
    const map: Record<PurchaseStatus, string> = {
      DRAFT: 'Borrador', APPROVED: 'Aprobada', RECEIVED: 'Recibida', CANCELLED: 'Cancelada'
    };
    return map[s] ?? s;
  }

  statusClass(s: PurchaseStatus): string {
    const map: Record<PurchaseStatus, string> = {
      DRAFT: 'badge-draft', APPROVED: 'badge-approved',
      RECEIVED: 'badge-received', CANCELLED: 'badge-cancelled'
    };
    return map[s] ?? '';
  }

  formatCurrency(n: number): string {
    return new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' }).format(n);
  }

  private showMsg(text: string, type: 'success' | 'error'): void {
    this.actionMsg.set({ text, type });
    setTimeout(() => this.actionMsg.set(null), 4000);
  }
}
