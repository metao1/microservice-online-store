import { useState } from 'react';
import { Order } from '../types';
import { apiClient } from '../services/api';
import { useCartContext } from '../context/CartContext';
import { toast } from 'react-toastify';

interface UseCheckoutResult {
  isProcessing: boolean;
  error: string | null;
  processCheckout: (userId: string, onSuccess?: (order: Order) => void) => Promise<Order | null>;
}

export const useCheckout = (): UseCheckoutResult => {
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { clearCart } = useCartContext();

  const processCheckout = async (userId: string, onSuccess?: (order: Order) => void): Promise<Order | null> => {
    try {
      setIsProcessing(true);
      setError(null);
      
      // Create the order
      const order = await apiClient.createOrder(userId);
      
      // Clear the cart after successful order creation
      await clearCart();
      
      // Show success message
      toast.success('Order placed successfully!');
      
      // Call success callback if provided
      if (onSuccess) {
        onSuccess(order);
      }
      
      return order;
    } catch (err) {
      console.error('Checkout failed:', err);
      const errorMessage = 'Failed to process checkout. Please try again.';
      setError(errorMessage);
      toast.error(errorMessage);
      return null;
    } finally {
      setIsProcessing(false);
    }
  };

  return {
    isProcessing,
    error,
    processCheckout,
  };
};