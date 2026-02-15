import axios, { AxiosInstance, AxiosError, AxiosRequestConfig } from 'axios';
import { ApiResponse, Cart, CartItem, Category, Order, Product } from '@types';

// ============================================================================
// Configuration & Constants
// ============================================================================

const CONFIG = {
  PRODUCTS_API_BASE_URL: import.meta.env.VITE_PRODUCTS_API_URL || 'http://localhost:8083',
  CART_API_BASE_URL: import.meta.env.VITE_CART_API_URL || 'http://localhost:8086',
  PRODUCTS_TIMEOUT: Number(import.meta.env.VITE_PRODUCTS_TIMEOUT) || 10000,
  CART_TIMEOUT: Number(import.meta.env.VITE_CART_TIMEOUT) || 5000,
  MAX_RETRY_ATTEMPTS: 3,
  RETRY_DELAY: 1000,
  DEFAULT_CATEGORY: 'books',
  LOG_LEVEL: (import.meta.env.VITE_LOG_LEVEL || 'info') as LogLevel,
} as const;

const PLACEHOLDER_CONFIG = {
  COLORS: ['4A90E2', '7ED321', 'F5A623', 'D0021B', '9013FE', '50E3C2'],
  TEXT_COLOR: 'FFFFFF',
  DIMENSIONS: '400x500',
} as const;

// ============================================================================
// Types & Interfaces
// ============================================================================

type LogLevel = 'debug' | 'info' | 'warn' | 'error';

interface RetryConfig {
  maxAttempts: number;
  delay: number;
  shouldRetry?: (error: AxiosError) => boolean;
}

interface ApiClientConfig {
  authToken?: string;
  onAuthError?: () => void;
  onNetworkError?: (error: Error) => void;
}

interface NormalizedPrice {
  price: number;
  currency: string;
}

// ============================================================================
// Logger
// ============================================================================

class Logger {
  private levels: Record<LogLevel, number> = {
    debug: 0,
    info: 1,
    warn: 2,
    error: 3,
  };

  private currentLevel: number;

  constructor(level: LogLevel = 'info') {
    this.currentLevel = this.levels[level];
  }

  debug(message: string, ...args: any[]): void {
    if (this.currentLevel <= this.levels.debug) {
      console.debug(`[DEBUG] ${message}`, ...args);
    }
  }

  info(message: string, ...args: any[]): void {
    if (this.currentLevel <= this.levels.info) {
      console.info(`[INFO] ${message}`, ...args);
    }
  }

  warn(message: string, ...args: any[]): void {
    if (this.currentLevel <= this.levels.warn) {
      console.warn(`[WARN] ${message}`, ...args);
    }
  }

  error(message: string, error?: any, ...args: any[]): void {
    if (this.currentLevel <= this.levels.error) {
      console.error(`[ERROR] ${message}`, error, ...args);
    }
  }
}

// ============================================================================
// Custom Errors
// ============================================================================

class ApiError extends Error {
  constructor(
      message: string,
      public statusCode?: number,
      public originalError?: any
  ) {
    super(message);
    this.name = 'ApiError';
    Object.setPrototypeOf(this, ApiError.prototype);
  }
}

class AuthenticationError extends ApiError {
  constructor(message: string = 'Authentication required') {
    super(message, 401);
    this.name = 'AuthenticationError';
    Object.setPrototypeOf(this, AuthenticationError.prototype);
  }
}

class NetworkError extends ApiError {
  constructor(message: string = 'Network request failed') {
    super(message);
    this.name = 'NetworkError';
    Object.setPrototypeOf(this, NetworkError.prototype);
  }
}

// ============================================================================
// API Client
// ============================================================================

class ApiClient {
  private productsClient: AxiosInstance;
  private cartClient: AxiosInstance;
  private logger: Logger;
  private authToken?: string;
  private onAuthError?: () => void;
  private onNetworkError?: (error: Error) => void;

  constructor(config: ApiClientConfig = {}) {
    this.logger = new Logger(CONFIG.LOG_LEVEL);
    this.authToken = config.authToken;
    this.onAuthError = config.onAuthError;
    this.onNetworkError = config.onNetworkError;

    this.productsClient = this.createAxiosInstance(
        CONFIG.PRODUCTS_API_BASE_URL,
        CONFIG.PRODUCTS_TIMEOUT
    );

    this.cartClient = this.createAxiosInstance(
        CONFIG.CART_API_BASE_URL,
        CONFIG.CART_TIMEOUT
    );

    this.setupInterceptors();
  }

  /**
   * Create configured Axios instance
   */
  private createAxiosInstance(baseURL: string, timeout: number): AxiosInstance {
    return axios.create({
      baseURL,
      timeout,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  /**
   * Setup request/response interceptors for both clients
   */
  private setupInterceptors(): void {
    const clients = [this.productsClient, this.cartClient];

    clients.forEach((client) => {
      // Request interceptor - add auth token
      client.interceptors.request.use(
          (config) => {
            if (this.authToken) {
              config.headers.Authorization = `Bearer ${this.authToken}`;
            }
            this.logger.debug(`${config.method?.toUpperCase()} ${config.url}`, config.params);
            return config;
          },
          (error) => {
            this.logger.error('Request interceptor error', error);
            return Promise.reject(error);
          }
      );

      // Response interceptor - handle common errors
      client.interceptors.response.use(
          (response) => {
            this.logger.debug(`Response from ${response.config.url}`, {
              status: response.status,
              dataType: typeof response.data,
            });
            return response;
          },
          (error: AxiosError) => {
            return this.handleInterceptorError(error);
          }
      );
    });
  }

  /**
   * Handle errors in interceptor
   */
  private handleInterceptorError(error: AxiosError): Promise<never> {
    if (error.response) {
      const status = error.response.status;
      this.logger.error(`API Error ${status}`, error.response.data);

      // Handle authentication errors
      if (status === 401 || status === 403) {
        this.onAuthError?.();
        return Promise.reject(
            new AuthenticationError('Please log in to continue')
        );
      }

      // Handle other HTTP errors
      const message = this.extractErrorMessage(error.response.data);
      return Promise.reject(new ApiError(message, status, error));
    } else if (error.request) {
      // Network error
      this.logger.error('Network error - no response received', error);
      const networkError = new NetworkError('Unable to connect to server');
      this.onNetworkError?.(networkError);
      return Promise.reject(networkError);
    } else {
      // Other errors
      this.logger.error('Request setup error', error);
      return Promise.reject(new ApiError(error.message, undefined, error));
    }
  }

  /**
   * Extract error message from various response formats
   */
  private extractErrorMessage(data: any): string {
    if (typeof data === 'string') return data;
    if (data?.message) return data.message;
    if (data?.error) return data.error;
    if (data?.errors?.[0]) return data.errors[0];
    return 'An error occurred';
  }

  /**
   * Update authentication token
   */
  public setAuthToken(token: string): void {
    this.authToken = token;
    this.logger.info('Auth token updated');
  }

  /**
   * Clear authentication token
   */
  public clearAuthToken(): void {
    this.authToken = undefined;
    this.logger.info('Auth token cleared');
  }

  /**
   * Retry failed requests with exponential backoff
   */
  private async retryRequest<T>(
      requestFn: () => Promise<T>,
      config: RetryConfig = {
        maxAttempts: CONFIG.MAX_RETRY_ATTEMPTS,
        delay: CONFIG.RETRY_DELAY,
      }
  ): Promise<T> {
    let lastError: Error;

    for (let attempt = 1; attempt <= config.maxAttempts; attempt++) {
      try {
        return await requestFn();
      } catch (error) {
        lastError = error as Error;

        // Don't retry on auth errors or client errors (4xx)
        if (
            error instanceof AuthenticationError ||
            (error instanceof ApiError && error.statusCode && error.statusCode < 500)
        ) {
          throw error;
        }

        // Check custom retry condition
        if (config.shouldRetry && !config.shouldRetry(error as AxiosError)) {
          throw error;
        }

        if (attempt < config.maxAttempts) {
          const delay = config.delay * Math.pow(2, attempt - 1); // Exponential backoff
          this.logger.warn(
              `Request failed, retrying in ${delay}ms (attempt ${attempt}/${config.maxAttempts})`,
              error
          );
          await this.sleep(delay);
        }
      }
    }

    this.logger.error('Max retry attempts reached', lastError!);
    throw lastError!;
  }

  /**
   * Sleep utility for retry delays
   */
  private sleep(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }

  // ==========================================================================
  // Data Transformation & Validation
  // ==========================================================================

  /**
   * Get valid image URL, converting HTTP to HTTPS and handling missing images
   */
  private getValidImageUrl(originalUrl: string, title: string, index: number): string {
    if (!originalUrl || typeof originalUrl !== 'string') {
      return this.getPlaceholderImage(title, index);
    }

    // Convert HTTP to HTTPS for Amazon images
    if (
        originalUrl.startsWith('http://ecx.images-amazon.com') ||
        originalUrl.startsWith('http://images-amazon.com')
    ) {
      return originalUrl.replace('http://', 'https://');
    }

    // For other HTTP URLs, use placeholder to avoid mixed content issues
    if (originalUrl.startsWith('http://')) {
      this.logger.warn('Converting insecure HTTP image URL to placeholder', originalUrl);
      return this.getPlaceholderImage(title, index);
    }

    // Return HTTPS URLs as-is
    return originalUrl;
  }

  /**
   * Generate placeholder image URL
   */
  private getPlaceholderImage(title: string, index: number): string {
    const bgColor = PLACEHOLDER_CONFIG.COLORS[index % PLACEHOLDER_CONFIG.COLORS.length];
    const cleanTitle = encodeURIComponent(
        title.substring(0, 20).replace(/[^a-zA-Z0-9\s]/g, '')
    );

    return `https://via.placeholder.com/${PLACEHOLDER_CONFIG.DIMENSIONS}/${bgColor}/${PLACEHOLDER_CONFIG.TEXT_COLOR}?text=${cleanTitle}`;
  }

  /**
   * Normalize price data from various backend formats
   */
  private normalizePrice(product: any): NormalizedPrice {
    const defaultCurrency =
        typeof product?.currency === 'string' ? product.currency : 'USD';
    const rawPrice = product?.price;

    // Handle price as object { amount, currency }
    if (rawPrice && typeof rawPrice === 'object') {
      const amount =
          typeof rawPrice.amount === 'number'
              ? rawPrice.amount
              : Number(rawPrice.amount);
      return {
        price: Number.isFinite(amount) ? amount : 0,
        currency:
            typeof rawPrice.currency === 'string'
                ? rawPrice.currency
                : defaultCurrency,
      };
    }

    // Handle price as number or string
    const numericPrice =
        typeof rawPrice === 'number' ? rawPrice : Number(rawPrice);
    return {
      price: Number.isFinite(numericPrice) ? numericPrice : 0,
      currency: defaultCurrency,
    };
  }

  /**
   * Normalize total amounts
   */
  private normalizeTotal(total: any, fallbackCurrency: string = 'USD'): number {
    if (total && typeof total === 'object') {
      const amount =
          typeof total.amount === 'number' ? total.amount : Number(total.amount);
      return Number.isFinite(amount) ? amount : 0;
    }
    const numericTotal = typeof total === 'number' ? total : Number(total);
    return Number.isFinite(numericTotal) ? numericTotal : 0;
  }

  /**
   * Unwrap API response from various backend formats
   */
  private unwrapApiResponse<T>(payload: any): T {
    const data = payload?.data ?? payload;

    // Check for error responses
    if (data && typeof data === 'object' && 'status' in data) {
      const status = (data as any).status;
      if (typeof status === 'number' && status >= 400) {
        throw new ApiError((data as any).message || 'Request failed', status);
      }
    }

    return data as T;
  }

  /**
   * Transform raw backend data to Product type
   */
  private toProduct(raw: any): Product {
    if (!raw || typeof raw !== 'object') {
      throw new ApiError('Invalid product data received');
    }

    const { price, currency } = this.normalizePrice(raw);
    const sku = typeof raw?.sku === 'string' ? raw.sku : '';

    if (!sku) {
      this.logger.warn('Product missing SKU', raw);
    }

    const title =
        typeof raw?.title === 'string' ? raw.title : `Product ${sku || 'Unknown'}`;
    const imageUrl =
        typeof raw?.imageUrl === 'string'
            ? this.getValidImageUrl(raw.imageUrl, title, 0)
            : this.getPlaceholderImage(title, 0);

    return {
      sku,
      title,
      brand: typeof raw?.brand === 'string' ? raw.brand : undefined,
      price,
      originalPrice:
          typeof raw?.originalPrice === 'number' ? raw.originalPrice : undefined,
      currency,
      imageUrl,
      images: Array.isArray(raw?.images) ? raw.images : undefined,
      description:
          typeof raw?.description === 'string'
              ? raw.description
              : 'No description available',
      rating: typeof raw?.rating === 'number' ? raw.rating : undefined,
      reviews: typeof raw?.reviews === 'number' ? raw.reviews : undefined,
      inStock: typeof raw?.inStock === 'boolean' ? raw.inStock : true,
      total: typeof raw?.total === 'number' ? raw.total : 0,
      quantity: typeof raw?.quantity === 'number' ? raw.quantity : undefined,
      variants: Array.isArray(raw?.variants) ? raw.variants : undefined,
      category: typeof raw?.category === 'string' ? raw.category : undefined,
      tags: Array.isArray(raw?.tags) ? raw.tags : undefined,
      isNew: typeof raw?.isNew === 'boolean' ? raw.isNew : undefined,
      isFeatured:
          typeof raw?.isFeatured === 'boolean' ? raw.isFeatured : undefined,
      isSale: typeof raw?.isSale === 'boolean' ? raw.isSale : undefined,
      salePercentage:
          typeof raw?.salePercentage === 'number' ? raw.salePercentage : undefined,
      createdAt:
          typeof raw?.createdAt === 'string' ? raw.createdAt : undefined,
      updatedAt:
          typeof raw?.updatedAt === 'string' ? raw.updatedAt : undefined,
    };
  }

  /**
   * Transform raw backend data to CartItem type
   */
  private toCartItem(
      item: any,
      productDetails: Product | null = null
  ): CartItem {
    const { price, currency } = this.normalizePrice(item);
    const sku = typeof item?.sku === 'string' ? item.sku : 'UNKNOWN-SKU';
    const title = productDetails?.title || item?.name || `Product ${sku}`;
    const imageUrl =
        productDetails?.imageUrl ||
        'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=500&fit=crop';
    const description =
        productDetails?.description || 'No description available';
    const rating = productDetails?.rating ?? 4.5;
    const reviews = productDetails?.reviews ?? 100;
    const inStock = productDetails?.inStock !== false;
    const quantity = productDetails?.quantity ?? 10;
    const cartQuantity = typeof item?.quantity === 'number' ? item.quantity : 1;

    return {
      sku,
      title,
      price,
      currency,
      imageUrl,
      description,
      rating,
      reviews,
      inStock,
      total: 0,
      quantity,
      cartQuantity,
    };
  }

  /**
   * Enrich cart item with full product details
   */
  private async enrichCartItem(item: any): Promise<CartItem> {
    const sku = typeof item?.sku === 'string' ? item.sku : '';

    if (!sku) {
      this.logger.warn('Cart item missing SKU', item);
      return this.toCartItem(item, null);
    }

    try {
      const productDetails = await this.getProductById(sku);
      return this.toCartItem(item, productDetails);
    } catch (error) {
      this.logger.warn(`Failed to enrich cart item ${sku}, using basic data`, error);
      return this.toCartItem(item, null);
    }
  }

  /**
   * Map backend order status to frontend status
   */
  private mapOrderStatus(
      backendStatus: string
  ): 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' {
    const status = (backendStatus || '').toUpperCase();
    const statusMap: Record<string, 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED'> = {
      PENDING: 'PENDING',
      PROCESSING: 'PENDING',
      CONFIRMED: 'CONFIRMED',
      CONFIRMED_ORDER: 'CONFIRMED',
      SHIPPED: 'SHIPPED',
      IN_TRANSIT: 'SHIPPED',
      DELIVERED: 'DELIVERED',
      COMPLETED: 'DELIVERED',
    };

    return statusMap[status] || 'PENDING';
  }

  // ==========================================================================
  // Public API Methods - Products
  // ==========================================================================

  /**
   * Fetch products by category with pagination
   */
  async getProducts(
      category?: string,
      limit: number = 12,
      offset: number = 0
  ): Promise<Product[]> {
    return this.retryRequest(async () => {
      const categoryToUse = category || CONFIG.DEFAULT_CATEGORY;
      const url = `/products/category/${encodeURIComponent(categoryToUse)}`;

      this.logger.info(`Fetching products: category=${categoryToUse}, limit=${limit}, offset=${offset}`);

      const response = await this.productsClient.get<Product[]>(url, {
        params: { limit, offset },
      });

      const rawProducts = this.unwrapApiResponse<Product[]>(response.data) || [];

      if (!Array.isArray(rawProducts)) {
        this.logger.error('Invalid products response format', rawProducts);
        throw new ApiError('Invalid response format from products API');
      }

      const products = rawProducts.map((product) => this.toProduct(product));
      this.logger.info(`Successfully fetched ${products.length} products`);

      return products;
    });
  }

  /**
   * Fetch all available categories
   */
  async getCategories(limit: number = 10, offset: number = 0): Promise<Category[]> {
    return this.retryRequest(async () => {
      this.logger.info(`Fetching categories: limit=${limit}, offset=${offset}`);

      const response = await this.productsClient.get<
          { category: string }[] | ApiResponse<{ category: string }[]>
      >('/products/categories', {
        params: { limit, offset },
      });

      const raw = this.unwrapApiResponse<{ category: string }[]>(response.data);
      const list = Array.isArray(raw) ? raw : [];

      return list.map((item, index) => {
        const name = item.category?.trim() || `Category ${index + 1}`;
        return {category: name};
      });
    });
  }



  /**
   * Fetch a single product by SKU
   */
  async getProductById(sku: string): Promise<Product> {
    if (!sku) {
      throw new ApiError('Invalid product SKU');
    }

    return this.retryRequest(async () => {
      this.logger.info(`Fetching product: ${sku}`);

      const response = await this.productsClient.get<ApiResponse<Product>>(
          `/products/${encodeURIComponent(sku)}`
      );

      const rawProduct = this.unwrapApiResponse<Product>(response.data);
      const product = this.toProduct(rawProduct);

      this.logger.info(`Successfully fetched product: ${product.sku}`);
      return product;
    });
  }

  /**
   * Search products by keyword
   */
  async searchProducts(
      query: string,
      limit: number = 12,
      offset: number = 0
  ): Promise<Product[]> {
    if (!query || typeof query !== 'string') {
      throw new ApiError('Search query is required');
    }

    return this.retryRequest(async () => {
      this.logger.info(`Searching products: query="${query}", limit=${limit}, offset=${offset}`);

      const response = await this.productsClient.get<Product[]>('/products/search', {
        params: {
          keyword: query,
          offset: offset,
          limit: limit,
        },
      });

      const rawProducts = this.unwrapApiResponse<Product[]>(response.data) || [];

      if (!Array.isArray(rawProducts)) {
        this.logger.error('Invalid search response format', rawProducts);
        throw new ApiError('Invalid response format from search API');
      }

      const products = rawProducts.map((product) => this.toProduct(product));
      this.logger.info(`Search returned ${products.length} products`);

      return products;
    });
  }

  // ==========================================================================
  // Public API Methods - Cart
  // ==========================================================================

  /**
   * Fetch user's shopping cart
   */
  async getCart(userId: string): Promise<Cart> {
    if (!userId) {
      throw new ApiError('User ID is required');
    }

    return this.retryRequest(async () => {
      this.logger.info(`Fetching cart for user: ${userId}`);

      const response = await this.cartClient.get(`/cart/${encodeURIComponent(userId)}`);
      const backendCart = this.unwrapApiResponse<any>(response.data);

      // Transform backend format to frontend format
      const items = backendCart?.shopping_cart_items || [];
      const enrichedItems = await Promise.all(
          items.map((item: any) => this.enrichCartItem(item))
      );

      const total = items.reduce((sum: number, item: any) => {
        const { price } = this.normalizePrice(item);
        return sum + price * (item.quantity || 0);
      }, 0);

      const cart: Cart = {
        items: enrichedItems,
        total,
      };

      this.logger.info(`Successfully fetched cart: ${cart.items.length} items, total: ${cart.total}`);
      return cart;
    });
  }

  /**
   * Add item to cart
   */
  async addToCart(
      userId: string,
      productId: string,
      quantity: number,
      price: number,
      currency: string
  ): Promise<Cart> {
    // Validation
    if (!userId || typeof userId !== 'string') {
      throw new ApiError('User ID is required');
    }
    if (!productId || typeof productId !== 'string') {
      throw new ApiError('Product ID is required');
    }
    if (typeof quantity !== 'number' || quantity <= 0) {
      throw new ApiError('Quantity must be a positive number');
    }
    if (typeof price !== 'number' || price < 0) {
      throw new ApiError('Price must be a non-negative number');
    }

    return this.retryRequest(async () => {
      const requestBody = {
        user_id: userId,
        items: [
          {
            sku: productId,
            quantity: quantity,
            price: price,
            currency: currency.toUpperCase(),
          },
        ],
      };

      this.logger.info(`Adding to cart: ${productId} x${quantity} for user ${userId}`);

      const response = await this.cartClient.post<ApiResponse<Cart>>(
          `/cart`,
          requestBody
      );

      const payload = this.unwrapApiResponse<any>(response.data);

      // Backend returns simple response, fetch updated cart
      if (typeof payload === 'number' || !payload || !('items' in payload)) {
        this.logger.debug('Backend returned simple response, fetching updated cart');
        const updatedCart = await this.getCart(userId);
        return updatedCart;
      }

      this.logger.info('Successfully added item to cart');
      return payload as Cart;
    });
  }

  /**
   * Remove item from cart
   */
  async removeFromCart(userId: string, productId: string): Promise<Cart> {
    if (!userId || typeof userId !== 'string') {
      throw new ApiError('User ID is required');
    }
    if (!productId || typeof productId !== 'string') {
      throw new ApiError('Product ID is required');
    }

    return this.retryRequest(async () => {
      this.logger.info(`Removing from cart: ${productId} for user ${userId}`);

      const response = await this.cartClient.delete<ApiResponse<Cart>>(
          `/cart/${encodeURIComponent(userId)}/${encodeURIComponent(productId)}`
      );

      const payload = this.unwrapApiResponse<any>(response.data);

      if (payload && typeof payload === 'object' && 'items' in payload) {
        this.logger.info('Successfully removed item from cart');
        return payload as Cart;
      }

      // Backend may return simple status, fetch updated cart
      this.logger.debug('Backend returned simple response, fetching updated cart');
      return await this.getCart(userId);
    });
  }

  /**
   * Update cart item quantity
   */
  async updateCartItem(
      userId: string,
      productId: string,
      quantity: number,
      price: number,
      currency: string
  ): Promise<Cart> {
    // Validation
    if (!userId || typeof userId !== 'string') {
      throw new ApiError('User ID is required');
    }
    if (!productId || typeof productId !== 'string') {
      throw new ApiError('Product ID is required');
    }
    if (typeof quantity !== 'number' || quantity <= 0) {
      throw new ApiError('Quantity must be a positive number');
    }

    return this.retryRequest(async () => {
      const requestBody = {
        user_id: userId,
        sku: productId,
        quantity: quantity,
      };

      this.logger.info(`Updating cart item: ${productId} to quantity ${quantity} for user ${userId}`);

      const response = await this.cartClient.put<ApiResponse<Cart>>(
          `/cart/${encodeURIComponent(userId)}/${encodeURIComponent(productId)}`,
          requestBody
      );

      const payload = this.unwrapApiResponse<any>(response.data);

      // Backend returns simple response, fetch updated cart
      if (typeof payload === 'number' || !payload || !('items' in payload)) {
        this.logger.debug('Backend returned simple response, fetching updated cart');
        return await this.getCart(userId);
      }

      this.logger.info('Successfully updated cart item');
      return payload as Cart;
    });
  }

  // ==========================================================================
  // Public API Methods - Orders
  // ==========================================================================

  /**
   * Create order from cart
   */
  async createOrder(userId: string): Promise<Order> {
    if (!userId || typeof userId !== 'string') {
      throw new ApiError('User ID is required');
    }

    return this.retryRequest(async () => {
      this.logger.info(`Creating order for user: ${userId}`);

      const response = await this.cartClient.post<any>(`/api/order`, {
        user_id: userId,
      });

      const backendOrder = this.unwrapApiResponse<any>(response.data);

      // Enrich order items with product details
      let enrichedItems: CartItem[] = [];
      if (backendOrder.items && Array.isArray(backendOrder.items)) {
        enrichedItems = await Promise.all(
            backendOrder.items.map((item: any) => this.enrichCartItem(item))
        );
      }

      const total =
          this.normalizeTotal(backendOrder.total) ||
          enrichedItems.reduce((sum, item) => sum + item.price * item.cartQuantity, 0);

      const order: Order = {
        id:
            backendOrder.id ||
            backendOrder.orderId ||
            `ORDER-${Date.now()}`,
        userId: backendOrder.userId || backendOrder.customerId || userId,
        items: enrichedItems,
        total,
        status: this.mapOrderStatus(backendOrder.status || 'PENDING'),
        createdAt:
            backendOrder.createdAt ||
            backendOrder.orderDate ||
            new Date().toISOString(),
      };

      this.logger.info(`Successfully created order: ${order.id}`);
      return order;
    });
  }

  /**
   * Fetch all orders for a user
   */
  async getOrders(userId: string): Promise<Order[]> {
    if (!userId || typeof userId !== 'string') {
      throw new ApiError('User ID is required');
    }

    return this.retryRequest(async () => {
      this.logger.info(`Fetching orders for user: ${userId}`);

      const response = await this.cartClient.get<Order[]>(
          `/api/order/customer/${encodeURIComponent(userId)}`
      );

      const orders = this.unwrapApiResponse<any[]>(response.data) || [];
      const normalizedOrders = Array.isArray(orders) ? orders : [];

      // Transform backend order format to frontend format
      const transformedOrders = await Promise.all(
          normalizedOrders.map(async (backendOrder: any) => {
            // Enrich order items with product details
            const enrichedItems = await Promise.all(
                (backendOrder.items || []).map((item: any) => this.enrichCartItem(item))
            );

            const total =
                this.normalizeTotal(backendOrder.total) ||
                enrichedItems.reduce((sum, item) => sum + item.price * item.cartQuantity, 0);

            return {
              id: backendOrder.id || backendOrder.orderId,
              userId: backendOrder.userId || backendOrder.customerId,
              items: enrichedItems,
              total,
              status: this.mapOrderStatus(backendOrder.status),
              createdAt:
                  backendOrder.createdAt ||
                  backendOrder.orderDate ||
                  new Date().toISOString(),
            };
          })
      );

      this.logger.info(`Successfully fetched ${transformedOrders.length} orders`);
      return transformedOrders;
    });
  }
}

// ============================================================================
// Export singleton instance
// ============================================================================

export const apiClient = new ApiClient();

// Export for testing or custom configuration
export { ApiClient, ApiError, AuthenticationError, NetworkError };