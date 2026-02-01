import { useState, useCallback, useEffect } from 'react';
import { Cart, CartItem } from '../types';
import { apiClient } from '../services/api';

export const useCart = (userId: string) => {
  const [cart, setCart] = useState<Cart>({ items: [], total: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchCart = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiClient.getCart(userId);
      setCart(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch cart');
    } finally {
      setLoading(false);
    }
  }, [userId]);

  const addToCart = useCallback(
    async (productId: string, quantity: number) => {
      try {
        const updatedCart = await apiClient.addToCart(userId, productId, quantity);
        setCart(updatedCart);
        return updatedCart;
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to add to cart');
        throw err;
      }
    },
    [userId]
  );

  const removeFromCart = useCallback(
    async (productId: string) => {
      try {
        const updatedCart = await apiClient.removeFromCart(userId, productId);
        setCart(updatedCart);
        return updatedCart;
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to remove from cart');
        throw err;
      }
    },
    [userId]
  );

  const updateCartItem = useCallback(
    async (productId: string, quantity: number) => {
      try {
        const updatedCart = await apiClient.updateCartItem(userId, productId, quantity);
        setCart(updatedCart);
        return updatedCart;
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to update cart');
        throw err;
      }
    },
    [userId]
  );

  const getCartTotal = useCallback(() => {
    return cart.items.reduce((total, item) => total + item.price * item.cartQuantity, 0);
  }, [cart.items]);

  useEffect(() => {
    // Commented out to avoid API calls on initial mount
    // Uncomment when backend API is ready
    // if (userId) {
    //   fetchCart();
    // }
  }, [userId, fetchCart]);

  return {
    cart,
    loading,
    error,
    fetchCart,
    addToCart,
    removeFromCart,
    updateCartItem,
    getCartTotal,
  };
};
