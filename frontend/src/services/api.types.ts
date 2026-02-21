import { Cart, Category, Order, Payment, PaymentMethodType, PaymentStatistics, Product } from '@types';

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
  getCart(userId: string): Promise<Cart>;
  addToCart(userId: string, productId: string, quantity: number, price: number, currency: string): Promise<Cart>;
  removeFromCart(userId: string, productId: string): Promise<Cart>;
  updateCartItem(userId: string, productId: string, quantity: number, price: number, currency: string): Promise<Cart>;
  createOrder(userId: string): Promise<Order>;
  getOrders(userId: string): Promise<Order[]>;
  createPayment(command: PaymentCommand): Promise<Payment>;
  processPayment(paymentId: string): Promise<Payment>;
  retryPayment(paymentId: string): Promise<Payment>;
  cancelPayment(paymentId: string): Promise<void>;
  getPayment(paymentId: string): Promise<Payment>;
  getPaymentByOrderId(orderId: string): Promise<Payment | null>;
  getPaymentsByStatus(status: string, offset?: number, limit?: number): Promise<Payment[]>;
  getPaymentStatistics(): Promise<PaymentStatistics>;
}
