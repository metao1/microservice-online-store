import { describe, expect, it } from 'vitest';
import { createMoney } from '@types';
import { resolveOrderDisplayMoney } from './orders.utils';

describe('resolveOrderDisplayMoney', () => {
  it('uses the payment amount when the mapped order total is zero', () => {
    const result = resolveOrderDisplayMoney(
      { total: createMoney(0, 'EUR'), items: [] },
      { amount: createMoney(24.25, 'EUR') },
    );

    expect(result).toMatchObject({ amount: 24.25, currency: 'EUR' });
  });

  it('prefers the backend order total over a payment amount', () => {
    const result = resolveOrderDisplayMoney(
      { total: createMoney(29.10, 'EUR'), items: [] },
      { amount: createMoney(29.58, 'EUR') },
    );

    expect(result).toMatchObject({ amount: 29.10, currency: 'EUR' });
  });

  it('falls back to the order total and item currency without a payment', () => {
    const result = resolveOrderDisplayMoney(
      { total: createMoney(19.5, 'USD'), items: [] },
      null,
    );

    expect(result).toMatchObject({ amount: 19.5, currency: 'USD' });
  });
});
