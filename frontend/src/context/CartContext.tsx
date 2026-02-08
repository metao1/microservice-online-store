import { createContext, useContext, ReactNode, useCallback } from 'react';
import { Cart, Product } from '@types';
import { useCart as useCartHook } from '../hooks/useCart';

interface CartContextType {
  cart: Cart;
  loading: boolean;
  error: string | null;
  addToCart: (product: Product, quantity: number) => Promise<void>;
  removeFromCart: (productId: string) => Promise<void>;
  updateCartItem: (product: Product, quantity: number) => Promise<void>;
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
      try {
        console.log('CartContext: Adding product to cart:', product.sku);
        await hookAddToCart(product.sku, quantity, product.price, product.currency);
        console.log('CartContext: Product added successfully');
        console.log('CartContext: Current cart after add:', cart);
      } catch (error) {
        console.error('CartContext: Failed to add product to cart:', error);
        // Don't re-throw the error to prevent app crashes
        // The UI will handle the error state through the error state from useCart
      }
    },
    [hookAddToCart, cart]
  );

  const removeFromCart = useCallback(
    async (productId: string) => {
      await hookRemoveFromCart(productId);
    },
    [hookRemoveFromCart]
  );

  const updateCartItem = useCallback(
    async (product: Product, quantity: number) => {
      await hookUpdateCartItem(product.sku, quantity, product.price, product.currency);
    },
    [hookUpdateCartItem]
  );

  const clearCart = useCallback(() => {
    if (cart.items && Array.isArray(cart.items)) {
      cart.items.forEach((item) => {
        hookRemoveFromCart(item.sku).catch(err => console.error('Error removing item:', err));
      });
    }
  }, [cart.items, hookRemoveFromCart]);

  const getCartItemCount = useCallback(() => {
    return (cart.items || []).reduce((count, item) => count + item.cartQuantity, 0);
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

export function useCartContext() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCartContext must be used within CartProvider');
  }
  return context;
}
