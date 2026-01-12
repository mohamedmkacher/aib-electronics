export interface Product {
  id: number;
  name: string;
  slug: string;
  sku: string;
  description: string;
  brand?: string;
  model?: string;
  categoryId: number;
  categoryName: string;
  categorySlug: string;
  price: number;
  discountPercentage?: number; // Champ reçu du backend
  stockQuantity: number;
  inStock: boolean;
  mainImageUrl?: string;
  isFeatured: boolean;
  active: boolean;
  categoryActive?: boolean;
  createdAt: string;
  updatedAt: string;

  // Propriétés calculées côté client
  currentPrice?: number;
  hasDiscount?: boolean;
}

export interface ProductRequest {
  name: string;
  description: string;
  categoryId: number;
  price: number;
  discount?: number; // Champ envoyé au backend
  stockQuantity: number;
  sku: string;
  brand?: string;
  model?: string;
  mainImageUrl?: string;
  isFeatured?: boolean;
  active?: boolean;
}

export interface ProductSearchRequest {
  keyword?: string;
  categoryId?: number;
  brand?: string;
  brands?: string[];
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  hasDiscount?: boolean;
  isFeatured?: boolean;
  active?: boolean;
  stockStatus?: string; // Added stock status filter
  sortBy?: string;
  sortDirection?: string;
  page?: number;
  size?: number;
}

export interface ProductListResponse {
  products: Product[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
  filterSummary: FilterSummary;
}

export interface FilterSummary {
  availableBrands: string[];
  minPrice: number;
  maxPrice: number;
  totalProducts: number;
}

export interface PriceRange {
  minPrice: number;
  maxPrice: number;
}
