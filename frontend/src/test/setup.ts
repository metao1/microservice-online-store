import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Make vi available globally as jest for compatibility
(global as any).jest = vi;

vi.mock('../services/api', () => ({
  apiClient: {
    getCategories: vi.fn().mockResolvedValue([]),
    searchProducts: vi.fn().mockResolvedValue([]),
    createOrder: vi.fn().mockResolvedValue({
      id: 'ORDER-1',
      userId: 'test-user',
      items: [],
      total: 0,
      status: 'PENDING',
      createdAt: new Date().toISOString(),
    }),
    createPayment: vi.fn().mockResolvedValue({
      paymentId: 'PAY-1',
      orderId: 'ORDER-1',
      amount: 0,
      currency: 'USD',
      paymentMethodType: 'CREDIT_CARD',
      status: 'CREATED',
    }),
    processPayment: vi.fn().mockResolvedValue({
      paymentId: 'PAY-1',
      orderId: 'ORDER-1',
      amount: 0,
      currency: 'USD',
      paymentMethodType: 'CREDIT_CARD',
      status: 'COMPLETED',
      isSuccessful: true,
    }),
  },
}));
