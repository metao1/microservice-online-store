import { Cart, Category, Order, Payment, PaymentMethodType, PaymentStatistics, Product } from '@types';
import { ApiClientContract, PaymentCommand } from './api.types';
import { BaseApiClient } from './api.base';
import mockProducts from '../../dev/api-mocks/products.json';
import mockCategories from '../../dev/api-mocks/categories.json';
import mockCart from '../../dev/api-mocks/cart.json';
import mockOrders from '../../dev/api-mocks/orders.json';
import mockPayments from '../../dev/api-mocks/payments.json';

export class MockApiClient extends BaseApiClient implements ApiClientContract {
  private products = mockProducts as Product[];
  private categories = mockCategories as Category[];
  private payments = mockPayments as any[];
  private cartData = mockCart as any;
  private orders = mockOrders as any[];

  async getProducts(category?: string, limit = 12, offset = 0): Promise<Product[]> {
    const filtered = category
      ? this.products.filter((p) => p.category?.toLowerCase() === category.toLowerCase())
      : this.products;
    return filtered.slice(offset, offset + limit).map((product, index) => {
      const mockData = this.generateMockVariants(product, index);
      return {
        ...product,
        imageUrl: this.getValidImageUrl(product.imageUrl, product.title, index),
        rating: product.rating || 4.2,
        reviews: product.reviews || 50,
        variants: mockData.variants,
        brand: mockData.brand,
        originalPrice: mockData.originalPrice,
        isNew: mockData.isNew,
        isFeatured: mockData.isFeatured,
        isSale: mockData.isSale,
        salePercentage: mockData.originalPrice
          ? Math.round(((mockData.originalPrice - product.price) / mockData.originalPrice) * 100)
          : undefined,
      };
    });
  }

  async getCategories(limit = 10, offset = 0): Promise<Category[]> {
    return this.categories.slice(offset, offset + limit);
  }

  async getSubcategories(query: string, limit?: number, offset?: number): Promise<Category[]>{
    return this.categories.slice(offset, offset + limit);
  }

  async getProductById(sku: string): Promise<Product> {
    const product = this.products.find((p) => p.sku === sku);
    if (!product) throw new Error('Product not found');
    const mockData = this.generateMockVariants(product, 0);
    return {
      ...product,
      imageUrl: this.getValidImageUrl(product.imageUrl, product.title, 0),
      rating: product.rating || 4.2,
      reviews: product.reviews || 50,
      variants: mockData.variants,
      brand: mockData.brand,
      originalPrice: mockData.originalPrice,
      isNew: mockData.isNew,
      isFeatured: mockData.isFeatured,
      isSale: mockData.isSale,
      salePercentage: mockData.originalPrice
        ? Math.round(((mockData.originalPrice - product.price) / mockData.originalPrice) * 100)
        : undefined,
    };
  }

  async getProductsBySkus(skus: string[]): Promise<Map<string, Product>> {
    const products = this.products.filter((p) => skus.includes(p.sku));
    return new Map(products.map((p) => [p.sku, p]));
  }

  async searchProducts(query: string, limit = 12, offset = 0): Promise<Product[]> {
    const normalized = query.toLowerCase();
    const matched = this.products.filter(
      (p) =>
        p.title.toLowerCase().includes(normalized) ||
        p.description.toLowerCase().includes(normalized) ||
        p.sku.toLowerCase().includes(normalized),
    );
    return matched.slice(offset, offset + limit);
  }

  async getCart(userId: string): Promise<Cart> {
    const cartItems: any[] = this.cartData.shopping_cart_items || [];
    const skus = cartItems.map((item: any) => item.sku);
    const productsBySku = await this.getProductsBySkus(skus);
    const enrichedItems = cartItems.map((item: any) => {
      const productDetails = productsBySku.get(item.sku);
      return {
        sku: item.sku,
        title: productDetails?.title || `Product ${item.sku}`,
        price: item.price,
        currency: item.currency,
        imageUrl: productDetails?.imageUrl || this.getPlaceholderImage(item.sku, 0),
        description: productDetails?.description || 'Product description',
        rating: productDetails?.rating || 4.2,
        reviews: productDetails?.reviews || 0,
        inStock: productDetails?.inStock ?? true,
        quantity: productDetails?.quantity || 0,
        cartQuantity: item.quantity,
      };
    });
    return {
      items: enrichedItems,
      total: cartItems.reduce((sum: number, item: any) => sum + item.price * item.quantity, 0),
    };
  }

  async addToCart(userId: string, productId: string, quantity: number, price: number, currency: string): Promise<Cart> {
    const item = this.cartData.shopping_cart_items.find((i: any) => i.sku === productId);
    if (item) {
      item.quantity += quantity;
    } else {
      this.cartData.shopping_cart_items.push({ sku: productId, quantity, price, currency });
    }
    return this.getCart(userId);
  }

  async removeFromCart(userId: string, productId: string): Promise<Cart> {
    this.cartData.shopping_cart_items = this.cartData.shopping_cart_items.filter((i: any) => i.sku !== productId);
    return this.getCart(userId);
  }

  async updateCartItem(
    userId: string,
    productId: string,
    quantity: number,
    price: number,
    currency: string,
  ): Promise<Cart> {
    const item = this.cartData.shopping_cart_items.find((i: any) => i.sku === productId);
    if (item) {
      item.quantity = quantity;
      item.price = price;
      item.currency = currency;
    }
    return this.getCart(userId);
  }

  async createOrder(userId: string): Promise<Order> {
    const cart = await this.getCart(userId);
    const orderId = `ORDER-${Date.now()}`;
    return {
      id: orderId,
      userId,
      items: cart.items,
      total: cart.total,
      status: 'PENDING',
      createdAt: new Date().toISOString(),
    };
  }

  async getOrders(userId: string): Promise<Order[]> {
    return this.orders.map((o) => ({
      id: o.id,
      userId: o.userId,
      items: (o.items || []).map((item: any) => ({
        sku: item.sku,
        title: this.products.find((p) => p.sku === item.sku)?.title || item.sku,
        price: item.price,
        currency: item.currency,
        imageUrl: this.products.find((p) => p.sku === item.sku)?.imageUrl || this.getPlaceholderImage(item.sku, 0),
        description: this.products.find((p) => p.sku === item.sku)?.description || '',
        rating: 4.2,
        reviews: 0,
        inStock: true,
        quantity: 0,
        cartQuantity: item.quantity,
      })),
      total: o.total,
      status: this.mapOrderStatus(o.status),
      createdAt: o.createdAt,
    }));
  }

  async createPayment(command: PaymentCommand): Promise<Payment> {
    const paymentId = `PAY-${Date.now()}`;
    const payment = {
      paymentId,
      orderId: command.orderId,
      amount: command.amount,
      currency: command.currency,
      paymentMethodType: command.paymentMethodType,
      paymentMethodDetails: command.paymentMethodDetails,
      status: 'CREATED',
      createdAt: new Date().toISOString(),
      isCompleted: false,
      isSuccessful: false,
    };
    this.payments.push(payment);
    return this.mapPayment(payment);
  }

  async processPayment(paymentId: string): Promise<Payment> {
    const payment = this.payments.find((p) => p.paymentId === paymentId);
    if (!payment) throw new Error('Payment not found');
    payment.status = 'COMPLETED';
    payment.isCompleted = true;
    payment.isSuccessful = true;
    payment.processedAt = new Date().toISOString();
    return this.mapPayment(payment);
  }

  async retryPayment(paymentId: string): Promise<Payment> {
    return this.processPayment(paymentId);
  }

  async cancelPayment(paymentId: string): Promise<void> {
    const payment = this.payments.find((p) => p.paymentId === paymentId);
    if (payment) {
      payment.status = 'CANCELLED';
      payment.isCompleted = true;
      payment.isSuccessful = false;
    }
  }

  async getPayment(paymentId: string): Promise<Payment> {
    const payment = this.payments.find((p) => p.paymentId === paymentId);
    if (!payment) throw new Error('Payment not found');
    return this.mapPayment(payment);
  }

  async getPaymentByOrderId(orderId: string): Promise<Payment | null> {
    const payment = this.payments.find((p) => p.orderId === orderId);
    return payment ? this.mapPayment(payment) : null;
  }

  async getPaymentsByStatus(status: string, offset = 0, limit = 10): Promise<Payment[]> {
    const filtered = this.payments.filter((p) => (p.status || '').toLowerCase() === status.toLowerCase());
    return filtered.slice(offset, offset + limit).map((p) => this.mapPayment(p));
  }

  async getPaymentStatistics(): Promise<PaymentStatistics> {
    const totalPayments = this.payments.length;
    const successfulPayments = this.payments.filter((p) => p.status === 'COMPLETED').length;
    const failedPayments = this.payments.filter((p) => p.status === 'FAILED').length;
    const pendingPayments = this.payments.filter((p) => p.status === 'PENDING' || p.status === 'CREATED').length;
    return this.mapPaymentStats({
      totalPayments,
      successfulPayments,
      failedPayments,
      pendingPayments,
    });
  }
}
