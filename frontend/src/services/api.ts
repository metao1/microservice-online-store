import axios, { AxiosInstance } from 'axios';
import { ApiResponse, Cart, Category, Order, Payment, PaymentMethodType, PaymentStatistics, Product } from '@types';
import { ApiClientContract, PaymentCommand } from './api.types';
import { BaseApiClient } from './api.base';
import { MockApiClient } from './api.mock';

const PRODUCTS_API_BASE_URL =
  import.meta.env.VITE_PRODUCTS_API_URL || 'https://microservice-online-store.onrender.com';
const CART_API_BASE_URL =
  import.meta.env.VITE_CART_API_URL || 'https://microservice-online-store-ow52.onrender.com';
const PAYMENT_API_BASE_URL = import.meta.env.VITE_PAYMENT_API_URL || 'http://localhost:8083';
const PROFILE = (import.meta.env.VITE_PROFILE || 'prod').toLowerCase();

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

  async getProducts(category: string = 'books', limit: number = 12, offset: number = 0): Promise<Product[]> {
    const url = `/products/category/${encodeURIComponent(category || 'books')}`;
    const response = await this.productsClient.get<Product[]>(url, { params: { limit, offset } });
    const products = response.data;
    return products.map((product, index) => {
      const mockData = this.generateMockVariants(product, index);
      return {
        ...product,
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
    return {
      ...productData,
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
    return new Map(products.map((product) => [product.sku, product]));
  }

  async searchProducts(query: string, limit: number = 12, offset: number = 0): Promise<Product[]> {
    const response = await this.productsClient.get<Product[]>('/products/search', {
      params: { keyword: query, offset, limit },
    });
    return response.data.map((product, index) => {
      const mockData = this.generateMockVariants(product, index);
      return {
        ...product,
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
      const productDetails = productsBySku.get(item.sku);
      if (!productDetails) {
        return {
          sku: item.sku,
          title: `Product ${item.sku}`,
          price: item.price,
          currency: item.currency,
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
        price: item.price,
        currency: item.currency,
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
      total: cartItems.reduce((sum: number, item: any) => sum + item.price * item.quantity, 0),
    };
  }

  async addToCart(userId: string, productId: string, quantity: number, price: number, currency: string): Promise<Cart> {
    const requestBody = {
      user_id: userId,
      items: [{ sku: productId, quantity, price, currency: currency.toUpperCase() }],
    };
    const response = await this.cartClient.post<ApiResponse<Cart>>(`/cart`, requestBody);
    if (typeof response.data === 'number' || !response.data || !('items' in response.data)) {
      return this.getCart(userId);
    }
    return response.data.data || response.data;
  }

  async removeFromCart(userId: string, productId: string): Promise<Cart> {
    const response = await this.cartClient.delete<ApiResponse<Cart>>(`/cart/${userId}/items/${productId}`);
    return response.data.data;
  }

  async updateCartItem(
    userId: string,
    productId: string,
    quantity: number,
    price: number,
    currency: string,
  ): Promise<Cart> {
    const requestBody = {
      user_id: userId,
      items: [{ sku: productId, quantity, price, currency: currency.toUpperCase() }],
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
    let enrichedItems = [];
    if (backendOrder.items && Array.isArray(backendOrder.items)) {
      enrichedItems = await Promise.all(
        backendOrder.items.map(async (item: any) => {
          try {
            const productDetails = await this.getProductById(item.sku);
            return {
              sku: item.sku,
              title: productDetails.title || item.name || `Product ${item.sku}`,
              price: item.price,
              currency: item.currency || 'USD',
              imageUrl: productDetails.imageUrl || this.getPlaceholderImage(productDetails.title || item.sku, 0),
              description: productDetails.description || 'Product description',
              rating: productDetails.rating || 4.5,
              reviews: productDetails.reviews || 100,
              inStock: productDetails.inStock !== false,
              quantity: productDetails.quantity || 10,
              cartQuantity: item.quantity,
            };
          } catch {
            return {
              sku: item.sku,
              title: item.name || `Product ${item.sku}`,
              price: item.price,
              currency: item.currency || 'USD',
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
    return {
      id: backendOrder.id || backendOrder.orderId || `ORDER-${Date.now()}`,
      userId: backendOrder.userId || backendOrder.customerId || userId,
      items: enrichedItems,
      total: backendOrder.total || enrichedItems.reduce((sum, item) => sum + item.price * item.cartQuantity, 0),
      status: this.mapOrderStatus(backendOrder.status || 'PENDING'),
      createdAt: backendOrder.createdAt || backendOrder.orderDate || new Date().toISOString(),
    };
  }

  async getOrders(userId: string): Promise<Order[]> {
    const response = await this.cartClient.get<Order[]>(`/api/order/customer/${userId}`);
    const orders = Array.isArray(response.data) ? response.data : [];
    const transformedOrders = await Promise.all(
      orders.map(async (backendOrder: any) => {
        const enrichedItems = await Promise.all(
          (backendOrder.items || []).map(async (item: any) => {
            try {
              const productDetails = await this.getProductById(item.sku);
              return {
                sku: item.sku,
                title: productDetails.title || item.name || `Product ${item.sku}`,
                price: item.price,
                currency: item.currency || 'USD',
                imageUrl: productDetails.imageUrl || this.getPlaceholderImage(item.sku, 0),
                description: productDetails.description || 'Product description',
                rating: productDetails.rating || 4.5,
                reviews: productDetails.reviews || 100,
                inStock: productDetails.inStock !== false,
                quantity: productDetails.quantity || 10,
                cartQuantity: item.quantity,
              };
            } catch {
              return {
                sku: item.sku,
                title: item.name || `Product ${item.sku}`,
                price: item.price,
                currency: item.currency || 'USD',
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
        return {
          id: backendOrder.id || backendOrder.orderId,
          userId: backendOrder.userId || backendOrder.customerId,
          items: enrichedItems,
          total: backendOrder.total || enrichedItems.reduce((sum, item) => sum + item.price * item.cartQuantity, 0),
          status: this.mapOrderStatus(backendOrder.status),
          createdAt: backendOrder.createdAt || backendOrder.orderDate || new Date().toISOString(),
        };
      }),
    );
    return transformedOrders;
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
