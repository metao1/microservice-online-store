import { Cart, Category, Money, Order, PaginatedResult, Payment, PaymentMethodType, PaymentStatistics, Product } from '@types';

export interface PaymentCommand {
  orderId: string;
  amount: Money;
  paymentMethodType: PaymentMethodType;
  paymentMethodDetails?: string;
}

export interface ApiClientContract {
  getProducts(category?: string, limit?: number, offset?: number): Promise<Product[]>;
  getCategories(limit?: number, offset?: number): Promise<Category[]>;
  getProductById(sku: string): Promise<Product>;
  getProductsBySkus(skus: string[]): Promise<Map<string, Product>>;
  searchProducts(query: string, limit?: number, offset?: number): Promise<Product[]>;
  getSubcategories(query: string, limit?: number, offset?: number): Promise<Category[]>;
  getCart(): Promise<Cart>;
  addToCart(sku: string, productTitle: string, quantity: number, price: Money): Promise<Cart>;
  removeFromCart(sku: string): Promise<Cart>;
  updateCartItem(sku: string, quantity: number, price: Money): Promise<Cart>;
  clearCart(): Promise<void>;
  createOrder(): Promise<Order>;
  getOrders(): Promise<Order[]>;
  getOrdersPage(limit?: number, offset?: number): Promise<PaginatedResult<Order>>;
  createPayment(command: PaymentCommand): Promise<Payment>;
  processPayment(paymentId: string): Promise<Payment>;
  retryPayment(paymentId: string): Promise<Payment>;
  cancelPayment(paymentId: string): Promise<void>;
  getPayment(paymentId: string): Promise<Payment>;
  getPaymentByOrderId(orderId: string): Promise<Payment | null>;
  getPaymentsByStatus(status: string, offset?: number, limit?: number): Promise<Payment[]>;
  getPaymentStatistics(): Promise<PaymentStatistics>;
}
