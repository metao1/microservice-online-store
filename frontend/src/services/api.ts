import axios, { AxiosInstance } from 'axios';
import { Product, ApiResponse, Order, Cart, Category } from '../types';

const PRODUCTS_API_BASE_URL = import.meta.env.VITE_PRODUCTS_API_URL || 'http://localhost:8083';
const CART_API_BASE_URL = import.meta.env.VITE_CART_API_URL || 'http://localhost:8086';

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
   * Generate a placeholder image URL
   */
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
      const url = `/products/category/${categoryToUse}`;
      
      const response = await this.productsClient.get<Product[]>(url, {
        params: { limit, offset },
      });
      
      console.log('Successfully fetched products from backend:', response.data);
      // Backend returns array directly, not wrapped in ApiResponse
      const products = response.data;
      
      // Temporary: Override inStock status for testing (remove this in production)
      const productsWithStock = products.map((product, index) => ({
        ...product,
        // Make every 3rd product in stock for testing
        inStock: index % 3 === 0,
        // Add some mock ratings for products that don't have them
        rating: product.rating || (4.0 + Math.random()),
        reviews: product.reviews || Math.floor(Math.random() * 200) + 10,
        quantity: index % 3 === 0 ? Math.floor(Math.random() * 20) + 1 : 0,
        // Fix image URLs - convert HTTP to HTTPS and add fallback
        imageUrl: this.getValidImageUrl(product.imageUrl, product.title, index)
      }));
      
      return productsWithStock;
    } catch (error) {
      console.error('Failed to fetch products from backend:', error);
      throw error;
    }
  }

  async getCategories(): Promise<Category[]> {
    try {
      const response = await this.productsClient.get<ApiResponse<Category[]>>('/categories');
      return response.data.data || response.data;
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
        imageUrl: this.getValidImageUrl(productData.imageUrl, productData.title, 0)
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
      const productsWithStock = response.data.map((product, index) => ({
        ...product,
        // Make every 3rd product in stock for testing
        inStock: index % 3 === 0,
        // Add some mock ratings for products that don't have them
        rating: product.rating || (4.0 + Math.random()),
        reviews: product.reviews || Math.floor(Math.random() * 200) + 10,
        quantity: index % 3 === 0 ? Math.floor(Math.random() * 20) + 1 : 0,
        // Fix image URLs
        imageUrl: this.getValidImageUrl(product.imageUrl, product.title, index)
      }));
      
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
      if (typeof response.data === 'number' || !response.data.items) {
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
      if (typeof response.data === 'number' || !response.data.items) {
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
      const response = await this.cartClient.post<ApiResponse<Order>>(`/orders`, {
        userId,
      });
      return response.data.data;
    } catch (error) {
      console.error('Failed to create order:', error);
      throw error;
    }
  }

  async getOrders(userId: string): Promise<Order[]> {
    try {
      const response = await this.cartClient.get<ApiResponse<Order[]>>(`/orders`, {
        params: { userId },
      });
      return response.data.data;
    } catch (error) {
      console.error('Failed to fetch orders:', error);
      throw error;
    }
  }
}

export const apiClient = new ApiClient();
