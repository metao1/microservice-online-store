import { useState } from 'react';
import { Money, Order, Payment, PaymentMethodType } from '@types';
import { apiClient } from '../services/api';
import { useCartContext } from '../context/CartContext';
import { toast } from 'react-toastify';

interface PaymentInput {
  method?: PaymentMethodType;
  details?: string;
  amount?: Money;
}

interface CheckoutOptions {
  payment?: PaymentInput;
  onSuccess?: (order: Order, payment?: Payment) => void;
}

interface CheckoutResult {
  order: Order;
  payment?: Payment;
}

interface UseCheckoutResult {
  isProcessing: boolean;
  error: string | null;
  processCheckout: (options?: CheckoutOptions) => Promise<CheckoutResult | null>;
}

export const useCheckout = (): UseCheckoutResult => {
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const { clearCart } = useCartContext();

  const processCheckout = async (options?: CheckoutOptions): Promise<CheckoutResult | null> => {
    try {
      setIsProcessing(true);
      setError(null);

      const order = await apiClient.createOrder();

      // The order service is the source of truth for totals and VAT.
      const amount = order.total;

      const paymentMethod = options?.payment?.method || 'CREDIT_CARD';
      const paymentDetails = options?.payment?.details || '';

      let payment: Payment | undefined;

      try {
        const createdPayment = await apiClient.createPayment({
          orderId: order.id,
          amount,
          paymentMethodType: paymentMethod,
          paymentMethodDetails: paymentDetails
        });

        // Immediately process payment to keep flow simple for checkout
        payment = await apiClient.processPayment(createdPayment.paymentId);
      } catch (paymentError) {
        console.error('Payment attempt failed:', paymentError);
        const message = 'Order created but payment failed. Please retry payment from your orders page.';
        setError(message);
        toast.error(message);
      }

      const paymentSuccessful = payment?.isSuccessful || payment?.status === 'COMPLETED' || payment?.status === 'SUCCESSFUL';

      if (paymentSuccessful) {
        await clearCart();
        toast.success('Payment processed and order placed successfully!');
      } else {
        toast.warn('Order created. Payment is pending or failed, please retry.');
      }

      if (options?.onSuccess) {
        options.onSuccess(order, payment);
      }

      return { order, payment };
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
