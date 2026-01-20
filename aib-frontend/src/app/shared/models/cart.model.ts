// src/app/shared/models/cart.model.ts

export interface CartItem {
  productId: number;
  name: string;
  slug: string;
  imageUrl: string;
  price: number;
  discount?: number;
  unitPrice: number;
  quantity: number;
  subtotal: number;
  stockQuantity: number;
  active: boolean;
}

export interface CartResponse {
  cartId: number;
  itemCount: number;
  subtotal: number;
  total: number;
  items: CartItem[];
}
