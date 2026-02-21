/**
 * Product Types
 */
export interface ProductImage {
  id: string;
  url: string;
  alt: string;
  isPrimary: boolean;
  sortOrder: number;
}

export interface ProductVariant {
  id: string;
  type: 'color' | 'size' | 'style';
  name: string;
  value: string;
  hexColor?: string;
  inStock: boolean;
  priceModifier?: number;
}

export interface Product {
  sku: string;
  title: string;
  brand?: string;
  price: number;
  originalPrice?: number;
  currency: string;
  imageUrl: string;
  images?: ProductImage[];
  description: string;
  rating?: number;
  reviews?: number;
  inStock: boolean;
  quantity?: number;
  variants?: ProductVariant[];
  category?: string;
  tags?: string[];
  isNew?: boolean;
  isFeatured?: boolean;
  isSale?: boolean;
  salePercentage?: number;
  createdAt?: string;
  updatedAt?: string;
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
  id?: string;
  name?: string;
  category?: string;
}

/**
 * Payment Types
 */
export type PaymentMethodType =
  | 'CREDIT_CARD'
  | 'DEBIT_CARD'
  | 'PAYPAL'
  | 'BANK_TRANSFER'
  | 'DIGITAL_WALLET';

export type PaymentStatus =
  | 'CREATED'
  | 'PROCESSING'
  | 'COMPLETED'
  | 'FAILED'
  | 'CANCELLED'
  | 'PENDING'
  | 'SUCCESSFUL'
  | string;

export interface Payment {
  paymentId: string;
  orderId: string;
  amount: number;
  currency: string;
  paymentMethodType: PaymentMethodType;
  paymentMethodDetails?: string;
  status: PaymentStatus;
  failureReason?: string;
  processedAt?: string;
  createdAt?: string;
  isCompleted?: boolean;
  isSuccessful?: boolean;
}

export interface PaymentStatistics {
  totalPayments: number;
  successfulPayments: number;
  failedPayments: number;
  pendingPayments: number;
  successRate: number;
  failureRate: number;
}

// Re-export navigation types for convenience
export * from './navigation';
