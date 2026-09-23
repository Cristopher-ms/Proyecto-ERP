import { Category } from './category.model';

export interface Product {
  id: number;
  sku: string;
  name: string;
  description?: string;
  price: number;
  costPrice?: number;
  stockQuantity: number;
  minStockAlert: number;
  category: Category;
  active: boolean;
  isLowStock: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProductRequest {
  sku: string;
  name: string;
  description?: string;
  price: number;
  costPrice?: number;
  stockQuantity?: number;
  minStockAlert?: number;
  categoryId: number;
  active?: boolean;
}
