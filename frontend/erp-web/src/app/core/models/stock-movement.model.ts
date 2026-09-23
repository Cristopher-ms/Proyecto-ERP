export type MovementType = 'IN' | 'OUT' | 'ADJUSTMENT';

export interface StockMovement {
  id: number;
  productId: number;
  productSku: string;
  productName: string;
  type: MovementType;
  quantity: number;
  previousStock: number;
  newStock: number;
  reason?: string;
  referenceId?: string;
  createdAt: string;
}

export interface StockAdjustmentRequest {
  productId: number;
  type: MovementType;
  quantity: number;
  reason?: string;
  referenceId?: string;
}
