export interface Money {
  readonly amount: number;
  readonly currency: string;
  format(locale?: string): string;
  multiply(multiplier: number): Money;
}

export const createMoney = (amount: number, currency: string): Money => {
  if (!Number.isFinite(amount)) {
    throw new TypeError('Money amount must be finite');
  }

  const normalizedCurrency = currency.trim().toUpperCase();
  if (!/^[A-Z]{3}$/.test(normalizedCurrency)) {
    throw new TypeError('Money currency must be a three-letter code');
  }

  return Object.freeze({
    amount,
    currency: normalizedCurrency,
    format(locale = 'en-US') {
      return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency: normalizedCurrency,
      }).format(amount);
    },
    multiply(multiplier: number) {
      if (!Number.isFinite(multiplier)) {
        throw new TypeError('Money multiplier must be finite');
      }
      return createMoney(amount * multiplier, normalizedCurrency);
    },
  });
};

export const createMoneyFrom = (value: unknown, fallbackCurrency = 'EUR'): Money => {
  if (value && typeof value === 'object' && 'amount' in value) {
    const payload = value as { amount?: unknown; currency?: unknown };
    const amount = Number(payload.amount);
    return createMoney(
      Number.isFinite(amount) ? amount : 0,
      String(payload.currency || fallbackCurrency),
    );
  }

  const amount = Number(value);
  return createMoney(Number.isFinite(amount) ? amount : 0, fallbackCurrency);
};

export const zeroMoney = (currency = 'EUR'): Money => createMoney(0, currency);
