import { renderHook, act } from '@testing-library/react';
import { vi } from 'vitest';
import { useCheckout } from './useCheckout';
import { PaymentMethodType } from '@types';

// Hoisted toast mocks to satisfy Vitest hoisting rules
const toast = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
  warn: vi.fn(),
}));

vi.mock('react-toastify', () => ({
  toast,
}));

// Hoisted mocks to satisfy Vitest hoisting rules
const hoisted = vi.hoisted(() => {
  const checkoutAmount = {
    amount: 120,
    currency: 'EUR',
    format: vi.fn(() => '€120.00'),
    multiply: vi.fn(),
  };
  const createOrder = vi.fn().mockResolvedValue({
    id: 'ORDER-1',
    userId: 'user-1',
    items: [],
    total: checkoutAmount,
    status: 'PENDING',
    createdAt: '2024-01-01T00:00:00Z',
  });

  const createPayment = vi.fn().mockResolvedValue({
    paymentId: 'PAY-1',
    orderId: 'ORDER-1',
    amount: checkoutAmount,
    paymentMethodType: 'CREDIT_CARD',
    status: 'CREATED',
  });

  const processPayment = vi.fn().mockResolvedValue({
    paymentId: 'PAY-1',
    orderId: 'ORDER-1',
    amount: checkoutAmount,
    paymentMethodType: 'CREDIT_CARD',
    status: 'COMPLETED',
    isSuccessful: true,
  });

  const clearCart = vi.fn();
  const getCartTotal = vi.fn(() => checkoutAmount);

  return {
    createOrder,
    createPayment,
    processPayment,
    clearCart,
    getCartTotal,
    checkoutAmount,
  };
});

// Mock cart context to control totals and side-effects
vi.mock('@context/CartContext', () => ({
  useCartContext: () => ({
    cart: {
      items: [
        {
          sku: 'SKU-1',
          title: 'Item',
          price: hoisted.checkoutAmount,
          cartQuantity: 1,
        },
      ],
    },
    clearCart: hoisted.clearCart,
    getCartTotal: hoisted.getCartTotal,
  }),
}));

// Mock API client calls
vi.mock('../services/api', () => ({
  apiClient: {
    createOrder: hoisted.createOrder,
    createPayment: hoisted.createPayment,
    processPayment: hoisted.processPayment,
  },
}));

describe('useCheckout', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('creates order then payment and clears cart on successful payment', async () => {
    const { result } = renderHook(() => useCheckout());

    await act(async () => {
      await result.current.processCheckout({
        payment: { method: 'CREDIT_CARD' as PaymentMethodType, details: '****4242' },
      });
    });

    expect(hoisted.createOrder).toHaveBeenCalledWith();
    expect(hoisted.createPayment).toHaveBeenCalledWith({
      orderId: 'ORDER-1',
      amount: hoisted.checkoutAmount,
      paymentMethodType: 'CREDIT_CARD',
      paymentMethodDetails: '****4242',
    });
    expect(hoisted.processPayment).toHaveBeenCalledWith('PAY-1');
    expect(hoisted.clearCart).toHaveBeenCalled();
    expect(toast.success).toHaveBeenCalled();
  });

  it('uses the backend order total when creating a payment', async () => {
    const backendTotal = { ...hoisted.checkoutAmount, amount: 99.99 };
    hoisted.createOrder.mockResolvedValueOnce({
      id: 'ORDER-2',
      userId: 'user-1',
      items: [],
      total: backendTotal,
      status: 'PENDING',
      createdAt: '2024-01-01T00:00:00Z',
    });

    const { result } = renderHook(() => useCheckout());

    await act(async () => {
      await result.current.processCheckout({
        payment: { method: 'CREDIT_CARD' as PaymentMethodType, details: '****4242' },
      });
    });

    expect(hoisted.createPayment).toHaveBeenCalledWith({
      orderId: 'ORDER-2',
      amount: backendTotal,
      paymentMethodType: 'CREDIT_CARD',
      paymentMethodDetails: '****4242',
    });
  });

  it('leaves cart intact and surfaces warning when payment processing fails', async () => {
    hoisted.processPayment.mockRejectedValueOnce(new Error('gateway down'));
    const { result } = renderHook(() => useCheckout());

    await act(async () => {
      await result.current.processCheckout({
        payment: { method: 'PAYPAL', details: 'payer@example.com' },
      });
    });

    expect(hoisted.createOrder).toHaveBeenCalled();
    expect(hoisted.createPayment).toHaveBeenCalled();
    expect(hoisted.clearCart).not.toHaveBeenCalled();
    expect(toast.warn).toHaveBeenCalled();
  });
});
