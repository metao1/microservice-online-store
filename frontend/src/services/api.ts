import axios, { AxiosInstance } from 'axios';
import { Product, ApiResponse, Order, Cart } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8083';

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  /**
   * Product API Methods
   */
  async getProducts(category?: string, limit: number = 12, offset: number = 0): Promise<Product[]> {
    try {
      let url = '/products';
      if (category) {
        url = `/products/category/${category}`;
      }
      
      const response = await this.client.get<ApiResponse<Product[]>>(url, {
        params: { limit, offset },
      });
      return response.data.data || response.data;
    } catch (error) {
      console.error('Failed to fetch products:', error);
      throw error;
    }
  }

  async getProductById(id: string): Promise<Product> {
    try {
      const response = await this.client.get<ApiResponse<Product>>(`/products/${id}`);
      return response.data.data;
    } catch (error) {
      console.error('Failed to fetch product:', error);
      throw error;
    }
  }

  async searchProducts(query: string): Promise<Product[]> {
    try {
      const response = await this.client.get<ApiResponse<Product[]>>('/products/search', {
        params: { q: query },
      });
      return response.data.data;
    } catch (error) {
      console.error('Failed to search products:', error);
      throw error;
    }
  }

  /**
   * Cart API Methods
   */
  async getCart(userId: string): Promise<Cart> {
    try {
      const response = await this.client.get<ApiResponse<Cart>>(`/carts/${userId}`);
      return response.data.data;
    } catch (error) {
      console.error('Failed to fetch cart:', error);
      throw error;
    }
  }

  async addToCart(userId: string, productId: string, quantity: number): Promise<Cart> {
    try {
      const response = await this.client.post<ApiResponse<Cart>>(`/carts/${userId}/items`, {
        productId,
        quantity,
      });
      return response.data.data;
    } catch (error) {
      console.error('Failed to add to cart:', error);
      throw error;
    }
  }

  async removeFromCart(userId: string, productId: string): Promise<Cart> {
    try {
      const response = await this.client.delete<ApiResponse<Cart>>(
        `/carts/${userId}/items/${productId}`
      );
      return response.data.data;
    } catch (error) {
      console.error('Failed to remove from cart:', error);
      throw error;
    }
  }

  async updateCartItem(userId: string, productId: string, quantity: number): Promise<Cart> {
    try {
      const response = await this.client.put<ApiResponse<Cart>>(
        `/carts/${userId}/items/${productId}`,
        { quantity }
      );
      return response.data.data;
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
      const response = await this.client.post<ApiResponse<Order>>(`/orders`, {
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
      const response = await this.client.get<ApiResponse<Order[]>>(`/orders`, {
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
