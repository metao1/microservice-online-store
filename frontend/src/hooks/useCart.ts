import {useCallback, useEffect, useState} from 'react';
import {Cart} from '@types';
import {apiClient} from '@services/api';
import {useAuthContext} from '@context/AuthContext';

export const useCart = () => {
  const { initialized, isAuthenticated } = useAuthContext();
  const [cart, setCart] = useState<Cart>({ items: [], total: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchCart = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await apiClient.getCart();
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
  }, []);

  const addToCart = useCallback(
      async (sku: string, productTitle: string, quantity: number, price: number, currency: string) => {
      try {
        console.log('useCart: Adding item to cart:', {sku, productTitle, quantity, price, currency});
        const updatedCart = await apiClient.addToCart(sku, productTitle, quantity, price, currency);
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
    []
  );

  const removeFromCart = useCallback(
      async (sku: string) => {
      try {
        const updatedCart = await apiClient.removeFromCart(sku);
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
    []
  );

  const updateCartItem = useCallback(
      async (sku: string, quantity: number, price: number, currency: string) => {
      try {
        const updatedCart = await apiClient.updateCartItem(sku, quantity, price, currency);
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
    []
  );

  const getCartTotal = useCallback(() => {
    return (cart.items || []).reduce((total, item) => total + item.price * item.cartQuantity, 0);
  }, [cart.items]);

  const clearCart = useCallback(async () => {
    await apiClient.clearCart();
    setCart({items: [], total: 0});
  }, []);

  useEffect(() => {
    if (initialized && isAuthenticated) {
      fetchCart();
    } else if (initialized) {
      setCart({items: [], total: 0});
    }
  }, [initialized, isAuthenticated, fetchCart]);

  return {
    cart,
    loading,
    error,
    fetchCart,
    addToCart,
    removeFromCart,
    updateCartItem,
    clearCart,
    getCartTotal,
  };
};
