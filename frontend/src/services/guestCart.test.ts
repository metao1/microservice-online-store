import { beforeEach, describe, expect, it } from 'vitest';
import { addGuestCartLine, clearGuestCart, guestCartAsCart, readGuestCart } from './guestCart';

describe('guest cart storage', () => {
  beforeEach(() => localStorage.clear());

  it('persists and combines quantities for the same product', () => {
    addGuestCartLine({ sku: 'BOOK-1', title: 'Book', quantity: 1, price: { amount: 10, currency: 'EUR' } });
    addGuestCartLine({ sku: 'BOOK-1', title: 'Book', quantity: 2, price: { amount: 10, currency: 'EUR' } });
    expect(readGuestCart()).toEqual([
      { sku: 'BOOK-1', title: 'Book', quantity: 3, price: { amount: 10, currency: 'EUR' } },
    ]);
  });

  it('projects guest lines into the cart shape used by the UI', () => {
    const cart = guestCartAsCart([
      { sku: 'BOOK-1', title: 'Book', quantity: 2, price: { amount: 10, currency: 'EUR' } },
    ]);
    expect(cart.items[0].cartQuantity).toBe(2);
    expect(cart.total.amount).toBe(20);
  });

  it('clears the guest cart after it is merged', () => {
    addGuestCartLine({ sku: 'BOOK-1', title: 'Book', quantity: 1, price: { amount: 10, currency: 'EUR' } });
    clearGuestCart();
    expect(readGuestCart()).toEqual([]);
  });
});
