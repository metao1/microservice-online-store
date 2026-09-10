import axios, {AxiosInstance} from 'axios';
import {ApiResponse, Cart, Category, createMoney, createMoneyFrom, Money, Order, PaginatedResult, Payment, PaymentStatistics, Product} from '@types';
import {ApiClientContract, PaymentCommand} from './api.types';
import {BaseApiClient} from './api.base';
import {MockApiClient} from './api.mock';
import {createAuthenticatedAxios} from './authenticatedAxios';

const PRODUCTS_API_BASE_URL = import.meta.env.VITE_PRODUCTS_API_URL || 'http://localhost:8083';
const CART_API_BASE_URL = import.meta.env.VITE_CART_API_URL || 'http://localhost:8086';
const PAYMENT_API_BASE_URL = import.meta.env.VITE_PAYMENT_API_URL || 'http://localhost:8084';
const PROFILE = (import.meta.env.VITE_PROFILE || 'prod').toLowerCase();

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
    this.cartClient = createAuthenticatedAxios({
      baseURL: CART_API_BASE_URL,
      timeout: 3000,
      headers: { 'Content-Type': 'application/json' },
    });
    this.paymentClient = createAuthenticatedAxios({
      baseURL: PAYMENT_API_BASE_URL,
      timeout: 10000,
      headers: { 'Content-Type': 'application/json' },
    });
  }

  private hydrateProduct(product: any): Product {
    const price = createMoneyFrom(product.price, product.currency || 'EUR');
    return {
      ...product,
      price,
      originalPrice: product.originalPrice == null
        ? undefined
        : createMoneyFrom(product.originalPrice, price.currency),
      variants: Array.isArray(product.variants)
        ? product.variants.map((variant: any) => ({
            ...variant,
            priceModifier: variant.priceModifier == null
              ? undefined
              : createMoneyFrom(variant.priceModifier, price.currency),
          }))
        : product.variants,
    };
  }

  private async mapBackendOrderToOrder(backendOrder: any): Promise<Order> {
    const enrichedItems = await Promise.all(
      (backendOrder.items || []).map(async (item: any) => {
        try {
          const productDetails = await this.getProductById(item.sku);
          const price = createMoneyFrom(item.price ?? item.unitPrice, item.currency || 'EUR');
          return {
            sku: item.sku,
            title: productDetails.title || item.productTitle || item.name || `Product ${item.sku}`,
            price,
            imageUrl: productDetails.imageUrl || this.getPlaceholderImage(item.sku, 0),
            description: productDetails.description || 'Product description',
            rating: productDetails.rating || 4.5,
            reviews: productDetails.reviews || 100,
            inStock: productDetails.inStock,
            quantity: productDetails.quantity || 10,
            cartQuantity: item.quantity,
            lineTotal: item.totalPrice == null
              ? price.multiply(item.quantity)
              : createMoneyFrom(item.totalPrice, price.currency),
          };
        } catch {
          const price = createMoneyFrom(item.price ?? item.unitPrice, item.currency || 'EUR');
          return {
            sku: item.sku,
            title: item.productTitle || item.name || `Product ${item.sku}`,
            price,
            imageUrl: this.getPlaceholderImage(item.sku, 0),
            description: 'Product description',
            rating: 4.5,
            reviews: 100,
            inStock: true,
            quantity: 10,
            cartQuantity: item.quantity,
            lineTotal: item.totalPrice == null
              ? price.multiply(item.quantity)
              : createMoneyFrom(item.totalPrice, price.currency),
          };
        }
      }),
    );

    const backendTotal =
      backendOrder.total === null || backendOrder.total === undefined
        ? undefined
        : createMoneyFrom(backendOrder.total, backendOrder.currency || enrichedItems[0]?.price.currency || 'EUR');
    const computedTotal = createMoney(
      enrichedItems.reduce((sum, item) => sum + item.price.amount * item.cartQuantity, 0),
      enrichedItems[0]?.price.currency || backendOrder.currency || 'EUR',
    );
    const orderId = resolveOrderId(backendOrder);
    if (!orderId) {
      throw new Error('Order payload is missing id');
    }

    return {
      id: orderId,
      userId: backendOrder.userId || '',
      items: enrichedItems,
      total: backendTotal ?? computedTotal,
      subtotal: backendOrder.subtotal == null
        ? undefined
        : createMoneyFrom(backendOrder.subtotal, computedTotal.currency),
      tax: backendOrder.tax == null
        ? undefined
        : createMoneyFrom(backendOrder.tax, computedTotal.currency),
      vatPercentage: backendOrder.vatPercentage,
      status: this.normalizeOrderStatus(backendOrder.status || 'PENDING'),
      createdAt: backendOrder.createdAt || backendOrder.orderDate || new Date().toISOString(),
      updatedAt: backendOrder.updatedAt,
    };
  }

  async getProducts(category: string = 'books', limit: number = 12, offset: number = 0): Promise<Product[]> {
    const url = `/products/category/${encodeURIComponent(category || 'books')}`;
    const response = await this.productsClient.get<any[]>(url, { params: { limit, offset } });
    const products = response.data;
    return products.map((product, index) => {
      const normalizedProduct = this.hydrateProduct(product);
      const mockData = this.generateMockVariants(normalizedProduct, index);
      return {
        ...normalizedProduct,
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
          ? Math.round(((mockData.originalPrice.amount - normalizedProduct.price.amount) / mockData.originalPrice.amount) * 100)
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
    const response = await this.productsClient.get<ApiResponse<any>>(`/products/${sku}`);
    const productData = response.data.data || response.data;
    const normalizedProduct = this.hydrateProduct(productData);
    return {
      ...normalizedProduct,
      inStock: true,
      rating: productData.rating || 4.0 + Math.random(),
      reviews: productData.reviews || Math.floor(Math.random() * 200) + 10,
      quantity: Math.floor(Math.random() * 20) + 1,
      imageUrl: this.getValidImageUrl(productData.imageUrl, productData.title, 0),
      ...this.generateMockVariants(normalizedProduct, 0),
    };
  }

  async getProductsBySkus(skus: string[]): Promise<Map<string, Product>> {
    if (!skus.length) return new Map();
    const response = await this.productsClient.get<any[] | ApiResponse<any[]>>('/products/by-skus', {
      params: { skus },
    });
    const raw = (response.data as ApiResponse<any[]>).data ?? response.data;
    const products = Array.isArray(raw) ? raw : [];
    return new Map(
      products.map((product) => {
        const normalizedProduct = this.hydrateProduct(product);
        return [normalizedProduct.sku, normalizedProduct];
      }),
    );
  }

  async searchProducts(query: string, limit: number = 12, offset: number = 0): Promise<Product[]> {
    const response = await this.productsClient.get<any[]>('/products/search', {
      params: { keyword: query, offset, limit },
    });
    return response.data.map((product, index) => {
      const normalizedProduct = this.hydrateProduct(product);
      const mockData = this.generateMockVariants(normalizedProduct, index);
      return {
        ...normalizedProduct,
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
          ? Math.round(((mockData.originalPrice.amount - normalizedProduct.price.amount) / mockData.originalPrice.amount) * 100)
          : undefined,
      };
    });
  }

  async getCart(): Promise<Cart> {
    const response = await this.cartClient.get('/cart');
    const backendCart = response.data;
    const cartItems: any[] = backendCart.shopping_cart_items || [];
    const skus: string[] = Array.from(new Set(cartItems.map((item) => String(item.sku))));
    let productsBySku = new Map<string, Product>();
    if (skus.length > 0) {
      try {
        productsBySku = await this.getProductsBySkus(skus);
      } catch {
        // Keep cart usable even when product enrichment endpoint is unavailable.
        productsBySku = new Map<string, Product>();
      }
    }
    const enrichedItems = cartItems.map((item: any) => {
      const price = createMoneyFrom(item.price, item.currency || 'EUR');
      const productDetails = productsBySku.get(item.sku);
      if (!productDetails) {
        return {
          sku: item.sku,
          title: `Product ${item.sku}`,
          price,
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
        price,
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
      total: createMoney(
        cartItems.reduce((sum: number, item: any) => sum + createMoneyFrom(item.price, item.currency).amount * item.quantity, 0),
        enrichedItems[0]?.price.currency || 'EUR',
      ),
    };
  }

  async addToCart(sku: string, productTitle: string, quantity: number, price: Money): Promise<Cart> {
    await this.cartClient.post('/cart/items', [
      {sku, productTitle, quantity, price: price.amount, currency: price.currency},
    ]);
    return this.getCart();
  }

  async removeFromCart(sku: string): Promise<Cart> {
    await this.cartClient.delete(`/cart/items/${encodeURIComponent(sku)}`);
    return this.getCart();
  }

  async updateCartItem(
    sku: string,
    quantity: number,
    _price: Money,
  ): Promise<Cart> {
    await this.cartClient.put(`/cart/items/${encodeURIComponent(sku)}`, {quantity});
    return this.getCart();
  }

  async clearCart(): Promise<void> {
    await this.cartClient.delete('/cart');
  }

  async createOrder(): Promise<Order> {
    const response = await this.cartClient.post<any>('/api/order');
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
            const price = createMoneyFrom(item.price ?? item.unitPrice, item.currency || 'EUR');
            return {
              sku: item.sku,
              title: productDetails.title || item.name || `Product ${item.sku}`,
              price,
              imageUrl: productDetails.imageUrl || this.getPlaceholderImage(productDetails.title || item.sku, 0),
              description: productDetails.description || 'Product description',
              rating: productDetails.rating || 4.5,
              reviews: productDetails.reviews || 100,
              inStock: productDetails.inStock,
              quantity: productDetails.quantity || 10,
              cartQuantity: item.quantity,
              lineTotal: item.totalPrice == null
                ? price.multiply(item.quantity)
                : createMoneyFrom(item.totalPrice, price.currency),
            };
          } catch {
            const price = createMoneyFrom(item.price ?? item.unitPrice, item.currency || 'EUR');
            return {
              sku: item.sku,
              title: item.name || `Product ${item.sku}`,
              price,
              imageUrl: this.getPlaceholderImage(item.sku, 0),
              description: 'Product description',
              rating: 4.5,
              reviews: 100,
              inStock: true,
              quantity: 10,
              cartQuantity: item.quantity,
              lineTotal: item.totalPrice == null
                ? price.multiply(item.quantity)
                : createMoneyFrom(item.totalPrice, price.currency),
            };
          }
        }),
      );
    }
    const backendTotal =
      backendOrder.total === null || backendOrder.total === undefined
        ? undefined
        : createMoneyFrom(backendOrder.total, backendOrder.currency || enrichedItems[0]?.price.currency || 'EUR');
    const computedTotal = createMoney(
      enrichedItems.reduce((sum, item) => sum + item.price.amount * item.cartQuantity, 0),
      enrichedItems[0]?.price.currency || backendOrder.currency || 'EUR',
    );
    return {
      id: orderId,
      userId: backendOrder.userId || '',
      items: enrichedItems,
      total: backendTotal ?? computedTotal,
      subtotal: backendOrder.subtotal == null
        ? undefined
        : createMoneyFrom(backendOrder.subtotal, computedTotal.currency),
      tax: backendOrder.tax == null
        ? undefined
        : createMoneyFrom(backendOrder.tax, computedTotal.currency),
      vatPercentage: backendOrder.vatPercentage,
      status: this.normalizeOrderStatus(backendOrder.status || 'PENDING'),
      createdAt: backendOrder.createdAt || backendOrder.orderDate || new Date().toISOString(),
      updatedAt: backendOrder.updatedAt,
    };
  }

  async getOrders(): Promise<Order[]> {
    const response = await this.cartClient.get<Order[]>('/api/order');
    const orders = Array.isArray(response.data) ? response.data : [];
    return Promise.all(orders.map((backendOrder: any) => this.mapBackendOrderToOrder(backendOrder)));
  }

  async getOrdersPage(limit = 10, offset = 0): Promise<PaginatedResult<Order>> {
    const response = await this.cartClient.get('/api/order/paged', {
      params: { offset, limit },
    });
    const payload = response.data || {};
    const items = Array.isArray(payload.items) ? payload.items : [];

    return {
      items: await Promise.all(items.map((backendOrder: any) => this.mapBackendOrderToOrder(backendOrder))),
      offset: Number(payload.offset ?? offset),
      limit: Number(payload.limit ?? limit),
      total: Number(payload.total ?? items.length),
      hasNext: Boolean(payload.hasNext),
      hasPrevious: Boolean(payload.hasPrevious),
    };
  }

  async createPayment(command: PaymentCommand): Promise<Payment> {
    const response = await this.paymentClient.post('/payments', {
      ...command,
      amount: command.amount.amount,
      currency: command.amount.currency,
    });
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
