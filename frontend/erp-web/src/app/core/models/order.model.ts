export type DocumentType = 'DNI' | 'RUC' | 'PASSPORT' | 'OTHER';
export type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'CANCELLED';
export type PaymentMethod = 'CASH' | 'CREDIT_CARD' | 'TRANSFER' | 'YAPE_PLIN';

export interface Customer {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email?: string;
  phone?: string;
  documentType: DocumentType;
  documentNumber: string;
  address?: string;
  active: boolean;
  createdAt?: string;
}

export interface CustomerRequest {
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  documentType: DocumentType;
  documentNumber: string;
  address?: string;
}

export interface OrderItem {
  id?: number;
  productId: number;
  productSku: string;
  productName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface CreateOrderItemRequest {
  productId: number;
  productSku: string;
  productName: string;
  unitPrice: number;
  quantity: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  customer: Customer;
  status: OrderStatus;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  paymentMethod: PaymentMethod;
  notes?: string;
  sellerEmail?: string;
  items: OrderItem[];
  createdAt: string;
  updatedAt?: string;
}

export interface CreateOrderRequest {
  customerId: number;
  paymentMethod: PaymentMethod;
  notes?: string;
  sellerEmail?: string;
  items: CreateOrderItemRequest[];
}

export interface SalesSummary {
  totalOrders: number;
  totalSalesAmount: number;
  todayOrders: number;
  todaySalesAmount: number;
  averageTicket: number;
}
