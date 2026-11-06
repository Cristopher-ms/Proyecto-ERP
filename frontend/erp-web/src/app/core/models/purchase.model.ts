// ── Enums ────────────────────────────────────────────────────────────────────
export type SupplierStatus = 'ACTIVE' | 'INACTIVE' | 'BLACKLISTED';
export type PurchaseStatus = 'DRAFT' | 'APPROVED' | 'RECEIVED' | 'CANCELLED';
export type PaymentTerms  = 'NET_15' | 'NET_30' | 'NET_60' | 'IMMEDIATE' | 'CONSIGNMENT';

// ── Modelos ──────────────────────────────────────────────────────────────────
export interface Supplier {
  id:            number;
  companyName:   string;
  taxId:         string;
  contactName?:  string;
  email?:        string;
  phone?:        string;
  address?:      string;
  country?:      string;
  paymentTerms:  PaymentTerms;
  status:        SupplierStatus;
  notes?:        string;
}

export interface SupplierRequest {
  companyName:  string;
  taxId:        string;
  contactName?: string;
  email?:       string;
  phone?:       string;
  address?:     string;
  country?:     string;
  paymentTerms: PaymentTerms;
  notes?:       string;
}

export interface PurchaseItem {
  id?:          number;
  productId:    number;
  productSku:   string;
  productName:  string;
  unitCost:     number;
  quantity:     number;
  subtotal:     number;
}

export interface PurchaseOrder {
  id:                     number;
  purchaseNumber:         string;
  supplier:               Supplier;
  status:                 PurchaseStatus;
  subtotal:               number;
  taxAmount:              number;
  totalAmount:            number;
  items:                  PurchaseItem[];
  expectedDeliveryDate?:  string;
  receivedDate?:          string;
  notes?:                 string;
  buyerEmail?:            string;
  createdAt:              string;
}

export interface CreatePurchaseRequest {
  supplierId:              number;
  items:                   PurchaseItemRequest[];
  expectedDeliveryDate?:   string;
  notes?:                  string;
  buyerEmail?:             string;
}

export interface PurchaseItemRequest {
  productId:   number;
  productSku:  string;
  productName: string;
  unitCost:    number;
  quantity:    number;
}

export interface PurchasesSummary {
  totalPurchases: number;
  totalAmount:    number;
  pendingOrders:  number;
  monthlyAmount:  number;
}
