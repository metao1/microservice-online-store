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
  categories?: string[];
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
  /**
   * Authoritative line total for the item when sourced from the order service
   * (OrderItemResponse.totalPrice). Optional because items coming from the
   * shopping cart do not expose a server-computed total.
   */
  lineTotal?: number;
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

export interface PaginatedResult<T> {
  items: T[];
  offset: number;
  limit: number;
  total: number;
  hasNext: boolean;
  hasPrevious: boolean;
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
 *
 * OrderStatus mirrors the backend `OrderStatus` enum emitted on
 * `OrderCreatedEvent` / `OrderStatusChangedEvent`. Keeping this list in
 * lock-step with the order-microservice avoids silently downgrading real
 * statuses (e.g. PAID) to a placeholder on the frontend.
 */
export type OrderStatus =
  | 'CREATED'
  | 'PENDING_PAYMENT'
  | 'PAID'
  | 'PAYMENT_FAILED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED';

export interface Order {
  id: string;
  userId: string;
  items: CartItem[];
  /** Server-computed gross total (subtotal + VAT) in the order currency. */
  total: number;
  /** Server-computed net total before VAT. Present when the backend returns it. */
  subtotal?: number;
  /** Server-computed VAT amount. Present when the backend returns it. */
  tax?: number;
  /** Integer VAT percentage applied to this order (e.g. 21 for 21%). */
  vatPercentage?: number | null;
  /** ISO 4217 currency code of the order totals. */
  currency?: string;
  status: OrderStatus;
  createdAt: string;
  updatedAt?: string;
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
