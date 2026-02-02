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
      // Ensure cart always has items array
      setCart({
        items: data.items || [],
        total: data.total || 0
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch cart');
      // Set empty cart on error
      setCart({ items: [], total: 0 });
    } finally {
      setLoading(false);
    }
  }, [userId]);

  const addToCart = useCallback(
    async (productId: string, quantity: number, price: number, currency: string) => {
      try {
        console.log('useCart: Adding item to cart:', { productId, quantity, price, currency });
        const updatedCart = await apiClient.addToCart(userId, productId, quantity, price, currency);
        console.log('useCart: Received updated cart:', updatedCart);
        // Ensure cart has proper structure
        const newCartState = {
          items: updatedCart.items || [],
          total: updatedCart.total || 0
        };
        console.log('useCart: Setting cart state to:', newCartState);
        setCart(newCartState);
        return updatedCart;
      } catch (err) {
        console.error('useCart: Error adding to cart:', err);
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
        // Ensure cart has proper structure
        setCart({
          items: updatedCart.items || [],
          total: updatedCart.total || 0
        });
        return updatedCart;
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to remove from cart');
        throw err;
      }
    },
    [userId]
  );

  const updateCartItem = useCallback(
    async (productId: string, quantity: number, price: number, currency: string) => {
      try {
        const updatedCart = await apiClient.updateCartItem(userId, productId, quantity, price, currency);
        // Ensure cart has proper structure
        setCart({
          items: updatedCart.items || [],
          total: updatedCart.total || 0
        });
        return updatedCart;
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to update cart');
        throw err;
      }
    },
    [userId]
  );

  const getCartTotal = useCallback(() => {
    return (cart.items || []).reduce((total, item) => total + item.price * item.cartQuantity, 0);
  }, [cart.items]);

  useEffect(() => {
    // Enable cart fetching now that backend API is working
    if (userId) {
      fetchCart();
    }
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
