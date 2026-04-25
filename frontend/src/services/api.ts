import axios, {AxiosInstance} from 'axios';
import {ApiResponse, Cart, Category, Order, PaginatedResult, Payment, PaymentStatistics, Product} from '@types';
import {ApiClientContract, PaymentCommand} from './api.types';
import {BaseApiClient} from './api.base';
import {MockApiClient} from './api.mock';

const PRODUCTS_API_BASE_URL = import.meta.env.VITE_PRODUCTS_API_URL || 'http://localhost:8083';
const CART_API_BASE_URL = import.meta.env.VITE_CART_API_URL || 'http://localhost:8086';
const PAYMENT_API_BASE_URL = import.meta.env.VITE_PAYMENT_API_URL || 'http://localhost:8084';
const PROFILE = (import.meta.env.VITE_PROFILE || 'prod').toLowerCase();

const coerceMoneyAmount = (value: unknown): number => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value;
  }
  if (value && typeof value === 'object' && 'amount' in value) {
    const amount = Number((value as { amount?: unknown }).amount);
    return Number.isFinite(amount) ? amount : 0;
  }
  return 0;
};

const coerceMoneyCurrency = (value: unknown, fallback: string): string => {
  if (value && typeof value === 'object' && 'currency' in value) {
    const currency = String((value as { currency?: unknown }).currency || fallback);
    return currency.toUpperCase();
  }
  return String(fallback || 'USD').toUpperCase();
};

const resolveOrderId = (value: unknown): string | undefined => {
  if (value && typeof value === 'object') {
    const payload = value as { id?: unknown; orderId?: unknown; value?: unknown; order_id?: unknown };
    const candidate = payload.id ?? payload.orderId ?? payload.value ?? payload.order_id;
    if (typeof candidate === 'string' && candidate.trim().length > 0) {
      return candidate;
    }
  }
  return undefined;
};

class RemoteApiClient extends BaseApiClient implements ApiClientContract {
  private productsClient: AxiosInstance;
  private cartClient: AxiosInstance;
  private paymentClient: AxiosInstance;

  constructor() {
    super();
    this.productsClient = axios.create({
      baseURL: PRODUCTS_API_BASE_URL,
      timeout: 10000,
      headers: { 'Content-Type': 'application/json' },
    });
    this.cartClient = axios.create({
      baseURL: CART_API_BASE_URL,
      timeout: 3000,
      headers: { 'Content-Type': 'application/json' },
    });
    this.paymentClient = axios.create({
      baseURL: PAYMENT_API_BASE_URL,
      timeout: 10000,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  private async mapBackendOrderToOrder(backendOrder: any, userId: string): Promise<Order> {
    const enrichedItems = await Promise.all(
      (backendOrder.items || []).map(async (item: any) => {
        try {
          const productDetails = await this.getProductById(item.sku);
          const priceAmount = coerceMoneyAmount(item.price ?? item.unitPrice);
          const priceCurrency = coerceMoneyCurrency(item.price ?? item.unitPrice, item.currency || 'USD');
          return {
            sku: item.sku,
            title: productDetails.title || item.productTitle || item.name || `Product ${item.sku}`,
            price: priceAmount,
            currency: priceCurrency,
            imageUrl: productDetails.imageUrl || this.getPlaceholderImage(item.sku, 0),
            description: productDetails.description || 'Product description',
            rating: productDetails.rating || 4.5,
            reviews: productDetails.reviews || 100,
            inStock: productDetails.inStock,
            quantity: productDetails.quantity || 10,
            cartQuantity: item.quantity,
          };
        } catch {
          const priceAmount = coerceMoneyAmount(item.price ?? item.unitPrice);
          const priceCurrency = coerceMoneyCurrency(item.price ?? item.unitPrice, item.currency || 'USD');
          return {
            sku: item.sku,
            title: item.productTitle || item.name || `Product ${item.sku}`,
            price: priceAmount,
            currency: priceCurrency,
            imageUrl: this.getPlaceholderImage(item.sku, 0),
            description: 'Product description',
            rating: 4.5,
            reviews: 100,
            inStock: true,
            quantity: 10,
            cartQuantity: item.quantity,
          };
        }
      }),
    );

    const backendTotal =
      backendOrder.total === null || backendOrder.total === undefined
        ? undefined
        : coerceMoneyAmount(backendOrder.total);
    const computedTotal = enrichedItems.reduce((sum, item) => sum + item.price * item.cartQuantity, 0);
    const orderId = resolveOrderId(backendOrder);
    if (!orderId) {
      throw new Error('Order payload is missing id');
    }

    return {
      id: orderId,
      userId: backendOrder.userId || userId,
      items: enrichedItems,
      total: backendTotal ?? computedTotal,
      status: this.normalizeOrderStatus(backendOrder.status || 'PENDING'),
      createdAt: backendOrder.createdAt || backendOrder.orderDate || new Date().toISOString(),
    };
  }

  async getProducts(category: string = 'books', limit: number = 12, offset: number = 0): Promise<Product[]> {
    const url = `/products/category/${encodeURIComponent(category || 'books')}`;
    const response = await this.productsClient.get<Product[]>(url, { params: { limit, offset } });
    const products = response.data;
    return products.map((product, index) => {
      const normalizedPrice = coerceMoneyAmount(product.price);
      const normalizedCurrency = coerceMoneyCurrency(product.price, product.currency || 'USD');
      const mockData = this.generateMockVariants(product, index);
      return {
        ...product,
        price: normalizedPrice,
        currency: normalizedCurrency,
        inStock: index % 3 === 0,
        rating: product.rating || 4.0 + Math.random(),
        reviews: product.reviews || Math.floor(Math.random() * 200) + 10,
        quantity: index % 3 === 0 ? Math.floor(Math.random() * 20) + 1 : 0,
        imageUrl: this.getValidImageUrl(product.imageUrl, product.title, index),
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

  async getCategories(limit: number = 10, offset: number = 0): Promise<Category[]> {
    const response = await this.productsClient.get<{ category: string }[] | ApiResponse<{ category: string }[]>>(
      '/products/categories',
      { params: { limit, offset } },
    );
    const raw = (response.data as ApiResponse<{ category: string }[]>).data ?? response.data;
    const list = Array.isArray(raw) ? raw : [];
    return list.map((item, index) => ({ category: item.category?.trim() || `Category ${index + 1}` }));
  }

  async getSubcategories(category: string, limit: number = 12, offset: number = 0): Promise<Category[]> {
    const response = await this.productsClient.get<{ category: string }[] | ApiResponse<{ category: string }[]>>(
      `/products/categories/${encodeURIComponent(category)}/subcategories`,
      { params: { limit, offset } },
    );
    const raw = (response.data as ApiResponse<{ category: string }[]>).data ?? response.data;
    const list = Array.isArray(raw) ? raw : [];
    return list.map((item, index) => ({ category: item.category?.trim() || `Subcategory ${index + 1}` }));
  }

  async getProductById(sku: string): Promise<Product> {
    const response = await this.productsClient.get<ApiResponse<Product>>(`/products/${sku}`);
    const productData = response.data.data || response.data;
    const normalizedPrice = coerceMoneyAmount(productData.price);
    const normalizedCurrency = coerceMoneyCurrency(productData.price, productData.currency || 'USD');
    return {
      ...productData,
      price: normalizedPrice,
      currency: normalizedCurrency,
      inStock: true,
      rating: productData.rating || 4.0 + Math.random(),
      reviews: productData.reviews || Math.floor(Math.random() * 200) + 10,
      quantity: Math.floor(Math.random() * 20) + 1,
      imageUrl: this.getValidImageUrl(productData.imageUrl, productData.title, 0),
      ...this.generateMockVariants(productData, 0),
    };
  }

  async getProductsBySkus(skus: string[]): Promise<Map<string, Product>> {
    if (!skus.length) return new Map();
    const response = await this.productsClient.get<Product[] | ApiResponse<Product[]>>('/products/by-skus', {
      params: { skus },
    });
    const raw = (response.data as ApiResponse<Product[]>).data ?? response.data;
    const products = Array.isArray(raw) ? raw : [];
    return new Map(
      products.map((product) => {
        const normalizedPrice = coerceMoneyAmount(product.price);
        const normalizedCurrency = coerceMoneyCurrency(product.price, product.currency || 'USD');
        return [
          product.sku,
          {
            ...product,
            price: normalizedPrice,
            currency: normalizedCurrency,
          },
        ];
      }),
    );
  }

  async searchProducts(query: string, limit: number = 12, offset: number = 0): Promise<Product[]> {
    const response = await this.productsClient.get<Product[]>('/products/search', {
      params: { keyword: query, offset, limit },
    });
    return response.data.map((product, index) => {
      const normalizedPrice = coerceMoneyAmount(product.price);
      const normalizedCurrency = coerceMoneyCurrency(product.price, product.currency || 'USD');
      const mockData = this.generateMockVariants(product, index);
      return {
        ...product,
        price: normalizedPrice,
        currency: normalizedCurrency,
        inStock: index % 3 === 0,
        rating: product.rating || 4.0 + Math.random(),
        reviews: product.reviews || Math.floor(Math.random() * 200) + 10,
        quantity: index % 3 === 0 ? Math.floor(Math.random() * 20) + 1 : 0,
        imageUrl: this.getValidImageUrl(product.imageUrl, product.title, index),
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

  async getCart(userId: string): Promise<Cart> {
    const response = await this.cartClient.get(`/cart/${userId}`);
    const backendCart = response.data;
    const cartItems: any[] = backendCart.shopping_cart_items || [];
    const skus: string[] = Array.from(new Set(cartItems.map((item) => String(item.sku))));
    const productsBySku = await this.getProductsBySkus(skus);
    const enrichedItems = cartItems.map((item: any) => {
      const priceAmount = coerceMoneyAmount(item.price);
      const priceCurrency = coerceMoneyCurrency(item.price, item.currency || 'USD');
      const productDetails = productsBySku.get(item.sku);
      if (!productDetails) {
        return {
          sku: item.sku,
          title: `Product ${item.sku}`,
          price: priceAmount,
          currency: priceCurrency,
          imageUrl: this.getPlaceholderImage(item.sku, 0),
          description: 'Product description',
          rating: 4.5,
          reviews: 100,
          inStock: true,
          quantity: 10,
          cartQuantity: item.quantity,
        };
      }
      return {
        sku: item.sku,
        title: productDetails.title || `Product ${item.sku}`,
        price: priceAmount,
        currency: priceCurrency,
        imageUrl: productDetails.imageUrl || this.getPlaceholderImage(productDetails.title, 0),
        description: productDetails.description || 'Product description',
        rating: productDetails.rating || 4.5,
        reviews: productDetails.reviews || 100,
        inStock: productDetails.inStock,
        quantity: productDetails.quantity || 10,
        cartQuantity: item.quantity,
      };
    });
    return {
      items: enrichedItems,
      total: cartItems.reduce(
        (sum: number, item: any) => sum + coerceMoneyAmount(item.price) * item.quantity,
        0,
      ),
    };
  }

  async addToCart(userId: string, sku: string, productTitle: string, quantity: number, price: number, currency: string): Promise<Cart> {
    const requestBody = {
      user_id: userId,
      items: [{sku: sku, productTitle, quantity, price, currency: currency.toUpperCase()}],
    };
    const response = await this.cartClient.post<ApiResponse<Cart>>(`/cart`, requestBody);
    if (typeof response.data === 'number' || !response.data || !('items' in response.data)) {
      return this.getCart(userId);
    }
    return response.data.data || response.data;
  }

  async removeFromCart(userId: string, sku: string): Promise<Cart> {
    const response = await this.cartClient.delete<ApiResponse<Cart>>(`/cart/${userId}/${sku}`);
    return response.data.data;
  }

  async updateCartItem(
    userId: string,
    sku: string,
    quantity: number,
    price: number,
    currency: string,
  ): Promise<Cart> {
    const requestBody = {
      user_id: userId,
      items: [{sku: sku, quantity, price, currency: currency.toUpperCase()}],
    };
    const response = await this.cartClient.put<ApiResponse<Cart>>(`/cart`, requestBody);
    if (typeof response.data === 'number' || !response.data || !('items' in response.data)) {
      return this.getCart(userId);
    }
    return response.data.data || response.data;
  }

  async createOrder(userId: string): Promise<Order> {
    const response = await this.cartClient.post<any>(`/api/order`, { user_id: userId });
    const backendOrder = response.data;
    const orderId = resolveOrderId(backendOrder);
    if (!orderId) {
      throw new Error('Order API returned no order id');
    }
    let enrichedItems = [];
    if (backendOrder.items && Array.isArray(backendOrder.items)) {
      enrichedItems = await Promise.all(
        backendOrder.items.map(async (item: any) => {
          try {
            const productDetails = await this.getProductById(item.sku);
            const priceAmount = coerceMoneyAmount(item.price);
            const priceCurrency = coerceMoneyCurrency(item.price, item.currency || 'USD');
            return {
              sku: item.sku,
              title: productDetails.title || item.name || `Product ${item.sku}`,
              price: priceAmount,
              currency: priceCurrency,
              imageUrl: productDetails.imageUrl || this.getPlaceholderImage(productDetails.title || item.sku, 0),
              description: productDetails.description || 'Product description',
              rating: productDetails.rating || 4.5,
              reviews: productDetails.reviews || 100,
              inStock: productDetails.inStock,
              quantity: productDetails.quantity || 10,
              cartQuantity: item.quantity,
            };
          } catch {
            const priceAmount = coerceMoneyAmount(item.price);
            const priceCurrency = coerceMoneyCurrency(item.price, item.currency || 'USD');
            return {
              sku: item.sku,
              title: item.name || `Product ${item.sku}`,
              price: priceAmount,
              currency: priceCurrency,
              imageUrl: this.getPlaceholderImage(item.sku, 0),
              description: 'Product description',
              rating: 4.5,
              reviews: 100,
              inStock: true,
              quantity: 10,
              cartQuantity: item.quantity,
            };
          }
        }),
      );
    }
    const backendTotal =
      backendOrder.total === null || backendOrder.total === undefined
        ? undefined
        : coerceMoneyAmount(backendOrder.total);
    const computedTotal = enrichedItems.reduce((sum, item) => sum + item.price * item.cartQuantity, 0);
    return {
      id: orderId,
      userId: backendOrder.userId || backendOrder.userId || userId,
      items: enrichedItems,
      total: backendTotal ?? computedTotal,
      status: this.normalizeOrderStatus(backendOrder.status || 'PENDING'),
      createdAt: backendOrder.createdAt || backendOrder.orderDate || new Date().toISOString(),
    };
  }

  async getOrders(userId: string): Promise<Order[]> {
    const response = await this.cartClient.get<Order[]>(`/api/order/customer/${userId}`);
    const orders = Array.isArray(response.data) ? response.data : [];
    return Promise.all(orders.map((backendOrder: any) => this.mapBackendOrderToOrder(backendOrder, userId)));
  }

  async getOrdersPage(userId: string, limit = 10, offset = 0): Promise<PaginatedResult<Order>> {
    const response = await this.cartClient.get(`/api/order/customer/${userId}/paged`, {
      params: { limit, offset },
    });
    const payload = response.data || {};
    const items = Array.isArray(payload.items) ? payload.items : [];

    return {
      items: await Promise.all(items.map((backendOrder: any) => this.mapBackendOrderToOrder(backendOrder, userId))),
      offset: Number(payload.offset ?? offset),
      limit: Number(payload.limit ?? limit),
      total: Number(payload.total ?? items.length),
      hasNext: Boolean(payload.hasNext),
      hasPrevious: Boolean(payload.hasPrevious),
    };
  }

  async createPayment(command: PaymentCommand): Promise<Payment> {
    const response = await this.paymentClient.post('/payments', command);
    return this.mapPayment(response.data);
  }

  async processPayment(paymentId: string): Promise<Payment> {
    const response = await this.paymentClient.post(`/payments/${paymentId}/process`);
    return this.mapPayment(response.data);
  }

  async retryPayment(paymentId: string): Promise<Payment> {
    const response = await this.paymentClient.post(`/payments/${paymentId}/retry`);
    return this.mapPayment(response.data);
  }

  async cancelPayment(paymentId: string): Promise<void> {
    await this.paymentClient.post(`/payments/${paymentId}/cancel`);
  }

  async getPayment(paymentId: string): Promise<Payment> {
    const response = await this.paymentClient.get(`/payments/${paymentId}`);
    return this.mapPayment(response.data);
  }

  async getPaymentByOrderId(orderId: string): Promise<Payment | null> {
    const response = await this.paymentClient.get(`/payments/order/${orderId}`);
    return response.data ? this.mapPayment(response.data) : null;
  }

  async getPaymentsByStatus(status: string, offset = 0, limit = 10): Promise<Payment[]> {
    const response = await this.paymentClient.get(`/payments/status/${status}`, { params: { offset, limit } });
    const payload = Array.isArray(response.data) ? response.data : [];
    return payload.map((item) => this.mapPayment(item));
  }

  async getPaymentStatistics(): Promise<PaymentStatistics> {
    const response = await this.paymentClient.get('/payments/statistics');
    return this.mapPaymentStats(response.data);
  }
}

const apiClientInstance: ApiClientContract = PROFILE === 'dev' ? new MockApiClient() : new RemoteApiClient();

export const apiClient = apiClientInstance;
