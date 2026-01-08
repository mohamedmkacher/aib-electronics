// category.model.ts
export interface Category {
  id: number;
  name: string;
  slug: string;
  description?: string;
  imageUrl?: string;
  active: boolean;
  displayOrder: number;
  productCount: number;
  createdAt: string;
}

export interface CategoryRequest {
  name: string;
  description?: string;
  imageUrl?: string;
  active?: boolean;
  displayOrder?: number;
}
