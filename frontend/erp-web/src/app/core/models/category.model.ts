export interface Category {
  id: number;
  name: string;
  description?: string;
  active: boolean;
  createdAt?: string;
}

export interface CategoryRequest {
  name: string;
  description?: string;
  active?: boolean;
}
