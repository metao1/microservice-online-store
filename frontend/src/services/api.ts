import axios, {AxiosInstance} from 'axios';
import {ApiResponse, Cart, Category, Order, Product} from '@types';

const PRODUCTS_API_BASE_URL = import.meta.env.VITE_PRODUCTS_API_URL || 'https://microservice-online-store.onrender.com';
const CART_API_BASE_URL = import.meta.env.VITE_CART_API_URL || 'https://microservice-online-store-ow52.onrender.com';

class ApiClient {
  private productsClient: AxiosInstance;
  private cartClient: AxiosInstance;

  constructor() {
    this.productsClient = axios.create({
      baseURL: PRODUCTS_API_BASE_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.cartClient = axios.create({
      baseURL: CART_API_BASE_URL,
      timeout: 3000, // 3 second timeout to prevent hanging
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  /**
   * Helper method to get valid image URLs
   */
  private getValidImageUrl(originalUrl: string, title: string, index: number): string {
    // If no original URL, use placeholder
    if (!originalUrl) {
      return this.getPlaceholderImage(title, index);
    }

    // Convert HTTP to HTTPS for Amazon images
    if (originalUrl.startsWith('http://ecx.images-amazon.com') || 
        originalUrl.startsWith('http://images-amazon.com')) {
      return originalUrl.replace('http://', 'https://');
    }

    // For other HTTP URLs, use placeholder to avoid mixed content issues
    if (originalUrl.startsWith('http://')) {
      return this.getPlaceholderImage(title, index);
    }

    // Return HTTPS URLs as-is
    return originalUrl;
  }

  /**
   * Generate mock variant data for products
   */
  private generateMockVariants(product: Product, index: number): { variants: any[], brand: string, originalPrice?: number, isNew?: boolean, isFeatured?: boolean, isSale?: boolean } {
    // Generate color variants
    const colorOptions = [
      { name: 'Black', value: '#000000', hexColor: '#000000' },
      { name: 'Navy', value: '#1e3a8a', hexColor: '#1e3a8a' },
      { name: 'Brown', value: '#8b4513', hexColor: '#8b4513' },
      { name: 'Gray', value: '#6b7280', hexColor: '#6b7280' },
      { name: 'White', value: '#ffffff', hexColor: '#ffffff' },
      { name: 'Red', value: '#dc2626', hexColor: '#dc2626' },
      { name: 'Blue', value: '#2563eb', hexColor: '#2563eb' },
      { name: 'Green', value: '#16a34a', hexColor: '#16a34a' }
    ];

    // Keep sizes aligned with the ProductsPage filter UI (shoe sizes).
    const sizeOptions = ['36', '37', '38', '39', '40', '41', '42', '43', '44', '45'];
    
    // Generate 2-4 color variants per product
    const numColors = Math.floor(Math.random() * 3) + 2;
    const selectedColors = colorOptions.slice(0, numColors);
    
    const colorVariants = selectedColors.map((color, i) => ({
      id: `color-${product.sku}-${i}`,
      type: 'color' as const,
      name: color.name,
      value: color.value,
      hexColor: color.hexColor,
      inStock: Math.random() > 0.2, // 80% chance of being in stock
      priceModifier: 0
    }));

    // Generate 3-5 size variants per product
    const numSizes = Math.floor(Math.random() * 3) + 3;
    const selectedSizes = sizeOptions.slice(0, numSizes);
    
    const sizeVariants = selectedSizes.map((size, i) => ({
      id: `size-${product.sku}-${i}`,
      type: 'size' as const,
      name: size,
      value: size,
      inStock: Math.random() > 0.3, // 70% chance of being in stock
      priceModifier: size === 'XXL' ? 5 : 0 // XXL costs $5 more
    }));

    // Use deterministic mock brands so the Brand filter can reliably work.
    const brands = ['Nike', 'Adidas', 'Puma', 'Reebok', 'Converse', 'New Balance'];
    const brand = brands[index % brands.length];
    
    // Generate discount data
    const hasDiscount = Math.random() > 0.7; // 30% chance of discount
    const originalPrice = hasDiscount ? Math.round(product.price * (1.2 + Math.random() * 0.3) * 100) / 100 : undefined;
    
    // Product flags
    const isNew = Math.random() > 0.8; // 20% chance of being new
    const isFeatured = Math.random() > 0.9; // 10% chance of being featured
    const isSale = hasDiscount;

    return {
      variants: [...colorVariants, ...sizeVariants],
      brand,
      originalPrice,
      isNew,
      isFeatured,
      isSale
    };
  }
  private getPlaceholderImage(title: string, index: number): string {
    // Use a variety of placeholder images for better visual appeal
    const colors = ['4A90E2', '7ED321', 'F5A623', 'D0021B', '9013FE', '50E3C2'];
    const bgColor = colors[index % colors.length];
    const textColor = 'FFFFFF';
    
    // Clean title for URL
    const cleanTitle = encodeURIComponent(title.substring(0, 20).replace(/[^a-zA-Z0-9\s]/g, ''));
    
    return `https://via.placeholder.com/400x500/${bgColor}/${textColor}?text=${cleanTitle}`;
  }

  /**
   * Product API Methods
   */
  async getProducts(category?: string, limit: number = 12, offset: number = 0): Promise<Product[]> {
    try {
      // Backend requires category, so default to 'books' if none provided
      const categoryToUse = category || 'books';
      const url = `/products/category/${encodeURIComponent(categoryToUse)}`;
      
      const response = await this.productsClient.get<Product[]>(url, {
        params: { limit, offset },
      });
      
      console.log('Successfully fetched products from backend:', response.data);
      // Backend returns array directly, not wrapped in ApiResponse
      const products = response.data;
      
      // Temporary: Override inStock status for testing (remove this in production)
      const productsWithStock = products.map((product, index) => {
        const mockData = this.generateMockVariants(product, index);
        return {
          ...product,
          // Make every 3rd product in stock for testing
          inStock: index % 3 === 0,
          // Add some mock ratings for products that don't have them
          rating: product.rating || (4.0 + Math.random()),
          reviews: product.reviews || Math.floor(Math.random() * 200) + 10,
          quantity: index % 3 === 0 ? Math.floor(Math.random() * 20) + 1 : 0,
          // Fix image URLs - convert HTTP to HTTPS and add fallback
          imageUrl: this.getValidImageUrl(product.imageUrl, product.title, index),
          // Add enhanced product data
          variants: mockData.variants,
          brand: mockData.brand,
          originalPrice: mockData.originalPrice,
          isNew: mockData.isNew,
          isFeatured: mockData.isFeatured,
          isSale: mockData.isSale,
          salePercentage: mockData.originalPrice ? Math.round(((mockData.originalPrice - product.price) / mockData.originalPrice) * 100) : undefined
        };
      });
      
      return productsWithStock;
    } catch (error) {
      console.error('Failed to fetch products from backend:', error);
      throw error;
    }
  }

  async getCategories(limit: number = 10, offset: number = 0): Promise<Category[]> {
    try {
      const response = await this.productsClient.get<{ category: string }[] | ApiResponse<{
        category: string
      }[]>>('/products/categories', {
        params: {limit, offset},
      });
      // Backend might return either a wrapped response or a direct array.
      const raw = (response.data as ApiResponse<{ category: string }[]>).data ?? response.data;
      const list = Array.isArray(raw) ? raw : [];

      return list.map((item, index) => {
        const name = item.category?.trim() || `Category ${index + 1}`;
        return {category: name};
      });
    } catch (error) {
      console.error('Failed to fetch categories:', error);
      throw error;
    }
  }

  async getProductById(sku: string): Promise<Product> {
    console.log(`Attempting to fetch product with SKU: ${sku} from ${this.productsClient.defaults.baseURL}`);
    
    try {
      const response = await this.productsClient.get<ApiResponse<Product>>(`/products/${sku}`);
      console.log('Successfully fetched product from backend:', response.data);
      // Handle both wrapped and direct response formats
      const productData = response.data.data || response.data;
      
      // Temporary: Override inStock status for testing (remove this in production)
      const productWithStock = {
        ...productData,
        // Make product in stock for testing
        inStock: true,
        // Add some mock ratings if not present
        rating: productData.rating || (4.0 + Math.random()),
        reviews: productData.reviews || Math.floor(Math.random() * 200) + 10,
        quantity: Math.floor(Math.random() * 20) + 1,
        // Fix image URL
        imageUrl: this.getValidImageUrl(productData.imageUrl, productData.title, 0),
        // Add enhanced product data
        ...this.generateMockVariants(productData, 0)
      };
      
      return productWithStock;
    } catch (error) {
      console.error('Failed to fetch product from backend:', error);
      throw error;
    }
  }

  async searchProducts(query: string, limit: number = 12, offset: number = 0): Promise<Product[]> {
    try {
      const response = await this.productsClient.get<Product[]>('/products/search', {
        params: { 
          keyword: query,
          offset: offset,
          limit: limit
        },
      });
      console.log('Search response from backend:', response.data);
      
      // Apply the same stock status override as in getProducts
      const productsWithStock = response.data.map((product, index) => {
        const mockData = this.generateMockVariants(product, index);
        return {
          ...product,
          // Make every 3rd product in stock for testing
          inStock: index % 3 === 0,
          // Add some mock ratings for products that don't have them
          rating: product.rating || (4.0 + Math.random()),
          reviews: product.reviews || Math.floor(Math.random() * 200) + 10,
          quantity: index % 3 === 0 ? Math.floor(Math.random() * 20) + 1 : 0,
          // Fix image URLs
          imageUrl: this.getValidImageUrl(product.imageUrl, product.title, index),
          // Add enhanced product data
          variants: mockData.variants,
          brand: mockData.brand,
          originalPrice: mockData.originalPrice,
          isNew: mockData.isNew,
          isFeatured: mockData.isFeatured,
          isSale: mockData.isSale,
          salePercentage: mockData.originalPrice ? Math.round(((mockData.originalPrice - product.price) / mockData.originalPrice) * 100) : undefined
        };
      });
      
      return productsWithStock;
    } catch (error) {
      console.error('Failed to search products from backend:', error);
      throw error;
    }
  }

  /**
   * Cart API Methods
   */
  async getCart(userId: string): Promise<Cart> {
    try {
      const response = await this.cartClient.get(`/cart/${userId}`);
      console.log('Cart response from backend:', response.data);
      
      // Backend returns: { user_id: string, shopping_cart_items: ShoppingCartItem[] }
      const backendCart = response.data;
      
      // Transform backend format to frontend format and enrich with product data
      const enrichedItems = await Promise.all(
        (backendCart.shopping_cart_items || []).map(async (item: any) => {
          try {
            // Try to fetch product details for each cart item
            const productDetails = await this.getProductById(item.sku);
            return {
              sku: item.sku,
              title: productDetails.title || `Product ${item.sku}`,
              price: item.price,
              currency: item.currency,
              imageUrl: productDetails.imageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=500&fit=crop',
              description: productDetails.description || 'Product description',
              rating: productDetails.rating || 4.5,
              reviews: productDetails.reviews || 100,
              inStock: productDetails.inStock !== false,
              quantity: productDetails.quantity || 10,
              cartQuantity: item.quantity
            };
          } catch (error) {
            console.warn(`Failed to fetch details for product ${item.sku}:`, error);
            // Fallback to basic item data
            return {
              sku: item.sku,
              title: `Product ${item.sku}`,
              price: item.price,
              currency: item.currency,
              imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=500&fit=crop',
              description: 'Product description',
              rating: 4.5,
              reviews: 100,
              inStock: true,
              quantity: 10,
              cartQuantity: item.quantity
            };
          }
        })
      );
      
      const frontendCart: Cart = {
        items: enrichedItems,
        total: (backendCart.shopping_cart_items || []).reduce((sum: number, item: any) => 
          sum + (item.price * item.quantity), 0)
      };
      
      return frontendCart;
    } catch (error) {
      console.error('Failed to fetch cart:', error);
      throw error;
    }
  }

  async addToCart(userId: string, productId: string, quantity: number, price: number, currency: string): Promise<Cart> {
    try {
      const requestBody = {
        user_id: userId,
        items: [{
          sku: productId,
          quantity: quantity,
          price: price,
          currency: currency.toUpperCase()
        }]
      };

      console.log('Sending add to cart request:', requestBody);
      const response = await this.cartClient.post<ApiResponse<Cart>>(`/cart`, requestBody);
      console.log('Add to cart response:', response.data);
      
      // Backend seems to return just a number instead of cart data
      // Let's fetch the updated cart after adding the item
      if (typeof response.data === 'number' || !response.data || !('items' in response.data)) {
        console.log('Backend returned simple response, fetching updated cart...');
        const updatedCart = await this.getCart(userId);
        return updatedCart;
      }
      
      return response.data.data || response.data;
    } catch (error) {
      console.error('Failed to add to cart:', error);
      
      // If the API fails, simulate success by returning the current cart with the new item
      // This prevents the UI from breaking while the backend issue is resolved
      try {
        const currentCart = await this.getCart(userId);
        console.log('Falling back to mock add for cart:', currentCart);
        
        // Add the new item to the existing cart (mock behavior)
        const newItem = {
          sku: productId,
          title: `Product ${productId}`,
          price: price,
          currency: currency,
          imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=500&fit=crop',
          description: 'Product description',
          rating: 4.5,
          reviews: 100,
          inStock: true,
          quantity: 10,
          cartQuantity: quantity
        };
        
        return {
          items: [...(currentCart.items || []), newItem],
          total: currentCart.total + (price * quantity)
        };
      } catch (getCartError) {
        console.error('Failed to get current cart for fallback:', getCartError);
        throw getCartError;
      }
    }
  }

  async removeFromCart(userId: string, productId: string): Promise<Cart> {
    try {
      const response = await this.cartClient.delete<ApiResponse<Cart>>(
        `/cart/${userId}/items/${productId}`
      );
      return response.data.data;
    } catch (error) {
      console.error('Failed to remove from cart:', error);
      throw error;
    }
  }

  async updateCartItem(userId: string, productId: string, quantity: number, price: number, currency: string): Promise<Cart> {
    try {
      const requestBody = {
        user_id: userId,
        items: [{
          sku: productId,
          quantity: quantity,
          price: price,
          currency: currency.toUpperCase()
        }]
      };

      const response = await this.cartClient.put<ApiResponse<Cart>>(
        `/cart`,
        requestBody
      );
      
      // Backend seems to return just a number instead of cart data
      // Let's fetch the updated cart after updating the item
      if (typeof response.data === 'number' || !response.data || !('items' in response.data)) {
        console.log('Backend returned simple response, fetching updated cart...');
        const updatedCart = await this.getCart(userId);
        return updatedCart;
      }
      
      return response.data.data || response.data;
    } catch (error) {
      console.error('Failed to update cart item:', error);
      throw error;
    }
  }

  /**
   * Order API Methods
   */
  async createOrder(userId: string): Promise<Order> {
    try {
      const response = await this.cartClient.post<any>(`/api/order`, {
        user_id: userId,
      });
      console.log('Create order response from backend:', response.data);
      
      // Backend returns order directly, transform to match frontend Order interface
      const backendOrder = response.data;
      
      // Enrich order items with product details if items exist
      let enrichedItems = [];
      if (backendOrder.items && Array.isArray(backendOrder.items)) {
        enrichedItems = await Promise.all(
          backendOrder.items.map(async (item: any) => {
            try {
              // Try to fetch product details for each order item
              const productDetails = await this.getProductById(item.sku);
              return {
                sku: item.sku,
                title: productDetails.title || item.name || `Product ${item.sku}`,
                price: item.price,
                currency: item.currency || 'USD',
                imageUrl: productDetails.imageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=500&fit=crop',
                description: productDetails.description || 'Product description',
                rating: productDetails.rating || 4.5,
                reviews: productDetails.reviews || 100,
                inStock: productDetails.inStock !== false,
                quantity: productDetails.quantity || 10,
                cartQuantity: item.quantity
              };
            } catch (error) {
              console.warn(`Failed to fetch details for product ${item.sku}:`, error);
              // Fallback to basic item data
              return {
                sku: item.sku,
                title: item.name || `Product ${item.sku}`,
                price: item.price,
                currency: item.currency || 'USD',
                imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=500&fit=crop',
                description: 'Product description',
                rating: 4.5,
                reviews: 100,
                inStock: true,
                quantity: 10,
                cartQuantity: item.quantity
              };
            }
          })
        );
      }

      return {
        id: backendOrder.id || backendOrder.orderId || `ORDER-${Date.now()}`,
        userId: backendOrder.userId || backendOrder.customerId || userId,
        items: enrichedItems,
        total: backendOrder.total || enrichedItems.reduce((sum, item) => sum + (item.price * item.cartQuantity), 0),
        status: this.mapOrderStatus(backendOrder.status || 'PENDING'),
        createdAt: backendOrder.createdAt || backendOrder.orderDate || new Date().toISOString()
      };
    } catch (error) {
      console.error('Failed to create order:', error);
      throw error;
    }
  }

  async getOrders(userId: string): Promise<Order[]> {
    try {
      const response = await this.cartClient.get<Order[]>(`/api/order/customer/${userId}`);
      console.log('Orders response from backend:', response.data);
      
      // Backend returns array directly, transform to match frontend Order interface
      const orders = Array.isArray(response.data) ? response.data : [];
      
      // Transform backend order format to frontend format
      const transformedOrders = await Promise.all(
        orders.map(async (backendOrder: any) => {
          // Enrich order items with product details
          const enrichedItems = await Promise.all(
            (backendOrder.items || []).map(async (item: any) => {
              try {
                // Try to fetch product details for each order item
                const productDetails = await this.getProductById(item.sku);
                return {
                  sku: item.sku,
                  title: productDetails.title || item.name || `Product ${item.sku}`,
                  price: item.price,
                  currency: item.currency || 'USD',
                  imageUrl: productDetails.imageUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=500&fit=crop',
                  description: productDetails.description || 'Product description',
                  rating: productDetails.rating || 4.5,
                  reviews: productDetails.reviews || 100,
                  inStock: productDetails.inStock !== false,
                  quantity: productDetails.quantity || 10,
                  cartQuantity: item.quantity
                };
              } catch (error) {
                console.warn(`Failed to fetch details for product ${item.sku}:`, error);
                // Fallback to basic item data
                return {
                  sku: item.sku,
                  title: item.name || `Product ${item.sku}`,
                  price: item.price,
                  currency: item.currency || 'USD',
                  imageUrl: 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=500&fit=crop',
                  description: 'Product description',
                  rating: 4.5,
                  reviews: 100,
                  inStock: true,
                  quantity: 10,
                  cartQuantity: item.quantity
                };
              }
            })
          );

          return {
            id: backendOrder.id || backendOrder.orderId,
            userId: backendOrder.userId || backendOrder.customerId,
            items: enrichedItems,
            total: backendOrder.total || enrichedItems.reduce((sum, item) => sum + (item.price * item.cartQuantity), 0),
            status: this.mapOrderStatus(backendOrder.status),
            createdAt: backendOrder.createdAt || backendOrder.orderDate || new Date().toISOString()
          };
        })
      );
      
      return transformedOrders;
    } catch (error) {
      console.error('Failed to fetch orders:', error);
      throw error;
    }
  }

  /**
   * Map backend order status to frontend status
   */
  private mapOrderStatus(backendStatus: string): 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' {
    const status = (backendStatus || '').toUpperCase();
    switch (status) {
      case 'PENDING':
      case 'PROCESSING':
        return 'PENDING';
      case 'CONFIRMED':
      case 'CONFIRMED_ORDER':
        return 'CONFIRMED';
      case 'SHIPPED':
      case 'IN_TRANSIT':
        return 'SHIPPED';
      case 'DELIVERED':
      case 'COMPLETED':
        return 'DELIVERED';
      default:
        return 'PENDING';
    }
  }
}

export const apiClient = new ApiClient();
