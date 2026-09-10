import { describe, expect, it } from 'vitest';
import { createMoney, createMoneyFrom } from './money';

describe('Money', () => {
  it('normalizes currency and formats itself', () => {
    const money = createMoney(24.25, 'eur');

    expect(money.currency).toBe('EUR');
    expect(money.format('de-DE')).toContain('24,25');
  });

  it('returns a new value with the same currency when multiplied', () => {
    const unitPrice = createMoney(8.5, 'EUR');

    const total = unitPrice.multiply(3);

    expect(total).toMatchObject({ amount: 25.5, currency: 'EUR' });
    expect(total).not.toBe(unitPrice);
  });

  it('is immutable', () => {
    expect(Object.isFrozen(createMoney(10, 'EUR'))).toBe(true);
  });

  it('hydrates nested and sibling API money shapes', () => {
    expect(createMoneyFrom({ amount: '24.25', currency: 'eur' }))
      .toMatchObject({ amount: 24.25, currency: 'EUR' });
    expect(createMoneyFrom(19.5, 'USD'))
      .toMatchObject({ amount: 19.5, currency: 'USD' });
  });

  it('rejects invalid values', () => {
    expect(() => createMoney(Number.NaN, 'EUR')).toThrow('Money amount must be finite');
    expect(() => createMoney(1, ' ')).toThrow('Money currency must be a three-letter code');
    expect(() => createMoney(1, 'EUR').multiply(Number.POSITIVE_INFINITY))
      .toThrow('Money multiplier must be finite');
  });
});
