import {useCallback, useEffect, useState} from 'react';
import {Cart, createMoney, Money, zeroMoney} from '@types';
import {apiClient} from '@services/api';
import {useAuthContext} from '@context/AuthContext';

export const useCart = () => {
  const { initialized, isAuthenticated } = useAuthContext();
  const [cart, setCart] = useState<Cart>({ items: [], total: zeroMoney() });
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
        total: data.total || zeroMoney()
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch cart');
      // Set empty cart on error
      setCart({ items: [], total: zeroMoney() });
    } finally {
      setLoading(false);
    }
  }, []);

  const addToCart = useCallback(
      async (sku: string, productTitle: string, quantity: number, price: Money) => {
      try {
        console.log('useCart: Adding item to cart:', {sku, productTitle, quantity, price});
        const updatedCart = await apiClient.addToCart(sku, productTitle, quantity, price);
        console.log('useCart: Received updated cart:', updatedCart);
        // Ensure cart has proper structure
        const newCartState = {
          items: updatedCart.items || [],
          total: updatedCart.total || zeroMoney()
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
          total: updatedCart.total || zeroMoney()
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
      async (sku: string, quantity: number, price: Money) => {
      try {
        const updatedCart = await apiClient.updateCartItem(sku, quantity, price);
        // Ensure cart has proper structure
        setCart({
          items: updatedCart.items || [],
          total: updatedCart.total || zeroMoney()
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
    const currency = cart.items[0]?.price.currency || cart.total.currency;
    return createMoney(
      (cart.items || []).reduce(
        (total, item) => total + item.price.multiply(item.cartQuantity).amount,
        0,
      ),
      currency,
    );
  }, [cart.items]);

  const clearCart = useCallback(async () => {
    await apiClient.clearCart();
    setCart({items: [], total: zeroMoney()});
  }, []);

  useEffect(() => {
    if (initialized && isAuthenticated) {
      fetchCart();
    } else if (initialized) {
      setCart({items: [], total: zeroMoney()});
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
