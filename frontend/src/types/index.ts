/**
 * Product Types
 */
export interface Product {
  sku: string;
  title: string;
  price: number;
  currency: string;
  imageUrl: string;
  description: string;
  rating?: number;
  reviews?: number;
  inStock: boolean;
  quantity?: number;
}

/**
 * Cart Types
 */
export interface CartItem extends Product {
  cartQuantity: number;
}

export interface Cart {
  items: CartItem[];
  total: number;
}

/**
 * API Response Types
 */
export interface ApiResponse<T> {
  data: T;
  message: string;
  status: number;
}

/**
 * User Types
 */
export interface User {
  id: string;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

/**
 * Order Types
 */
export interface Order {
  id: string;
  userId: string;
  items: CartItem[];
  total: number;
  status: 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED';
  createdAt: string;
}

/**
 * Category Types
 */
export interface Category {
  id: string;
  name: string;
  description?: string;
}
