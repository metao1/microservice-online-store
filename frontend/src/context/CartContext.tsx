import { createContext, useContext, ReactNode, useState, useCallback } from 'react';
import { Cart, CartItem, Product } from '../types';
import { useCart as useCartHook } from '../hooks/useCart';

interface CartContextType {
  cart: Cart;
  loading: boolean;
  error: string | null;
  addToCart: (product: Product, quantity: number) => Promise<void>;
  removeFromCart: (productId: string) => Promise<void>;
  updateCartItem: (productId: string, quantity: number) => Promise<void>;
  clearCart: () => void;
  getCartTotal: () => number;
  getCartItemCount: () => number;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

interface CartProviderProps {
  children: ReactNode;
  userId: string;
}

export const CartProvider: React.FC<CartProviderProps> = ({ children, userId }) => {
  const { cart, loading, error, addToCart: hookAddToCart, removeFromCart: hookRemoveFromCart, updateCartItem: hookUpdateCartItem, getCartTotal } = useCartHook(userId);

  const addToCart = useCallback(
    async (product: Product, quantity: number) => {
      await hookAddToCart(product.id, quantity);
    },
    [hookAddToCart]
  );

  const removeFromCart = useCallback(
    async (productId: string) => {
      await hookRemoveFromCart(productId);
    },
    [hookRemoveFromCart]
  );

  const updateCartItem = useCallback(
    async (productId: string, quantity: number) => {
      await hookUpdateCartItem(productId, quantity);
    },
    [hookUpdateCartItem]
  );

  const clearCart = useCallback(() => {
    if (cart.items && Array.isArray(cart.items)) {
      cart.items.forEach((item) => {
        hookRemoveFromCart(item.id).catch(err => console.error('Error removing item:', err));
      });
    }
  }, [cart.items, hookRemoveFromCart]);

  const getCartItemCount = useCallback(() => {
    return cart.items.reduce((count, item) => count + item.cartQuantity, 0);
  }, [cart.items]);

  const value: CartContextType = {
    cart,
    loading,
    error,
    addToCart,
    removeFromCart,
    updateCartItem,
    clearCart,
    getCartTotal,
    getCartItemCount,
  };

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
};

export const useCartContext = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCartContext must be used within CartProvider');
  }
  return context;
};
