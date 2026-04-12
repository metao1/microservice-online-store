import { Cart, Category, Order, PaginatedResult, Payment, PaymentMethodType, PaymentStatistics, Product } from '@types';

export interface PaymentCommand {
  orderId: string;
  amount: number;
  currency: string;
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
  getCart(userId: string): Promise<Cart>;
  addToCart(userId: string, sku: string, productTitle: string, quantity: number, price: number, currency: string): Promise<Cart>;
  removeFromCart(userId: string, sku: string): Promise<Cart>;
  updateCartItem(userId: string, sku: string, quantity: number, price: number, currency: string): Promise<Cart>;
  createOrder(userId: string): Promise<Order>;
  getOrders(userId: string): Promise<Order[]>;
  getOrdersPage(userId: string, limit?: number, offset?: number): Promise<PaginatedResult<Order>>;
  createPayment(command: PaymentCommand): Promise<Payment>;
  processPayment(paymentId: string): Promise<Payment>;
  retryPayment(paymentId: string): Promise<Payment>;
  cancelPayment(paymentId: string): Promise<void>;
  getPayment(paymentId: string): Promise<Payment>;
  getPaymentByOrderId(orderId: string): Promise<Payment | null>;
  getPaymentsByStatus(status: string, offset?: number, limit?: number): Promise<Payment[]>;
  getPaymentStatistics(): Promise<PaymentStatistics>;
}
