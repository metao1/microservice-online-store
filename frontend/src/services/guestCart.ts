import { CartItem, createMoney, Money, zeroMoney } from '../types';

export interface GuestCartLine {
  sku: string;
  title: string;
  quantity: number;
  price: { amount: number; currency: string };
}

const STORAGE_KEY = 'bookstore:guest-cart:v1';

export function readGuestCart(): GuestCartLine[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed.filter(isGuestCartLine);
  } catch {
    return [];
  }
}

export function writeGuestCart(lines: GuestCartLine[]): void {
  if (lines.length === 0) {
    localStorage.removeItem(STORAGE_KEY);
    return;
  }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(lines));
}

export function addGuestCartLine(line: GuestCartLine): GuestCartLine[] {
  const lines = readGuestCart();
  const existing = lines.find((candidate) => candidate.sku === line.sku);
  if (existing) {
    existing.quantity += line.quantity;
  } else {
    lines.push(line);
  }
  writeGuestCart(lines);
  return lines;
}

export function clearGuestCart(): void {
  writeGuestCart([]);
}

export function guestCartAsCart(lines: GuestCartLine[]): { items: CartItem[]; total: Money } {
  const items = lines.map((line) => ({
    sku: line.sku,
    title: line.title,
    price: createMoney(line.price.amount, line.price.currency),
    imageUrl: '',
    description: '',
    inStock: true,
    cartQuantity: line.quantity,
  }));
  const total = createMoney(
    items.reduce((sum, item) => sum + item.price.amount * item.cartQuantity, 0),
    items[0]?.price.currency || zeroMoney().currency,
  );
  return { items, total };
}

function isGuestCartLine(value: unknown): value is GuestCartLine {
  if (!value || typeof value !== 'object') return false;
  const line = value as Partial<GuestCartLine>;
  return typeof line.sku === 'string'
    && typeof line.title === 'string'
    && typeof line.quantity === 'number'
    && line.quantity > 0
    && typeof line.price?.amount === 'number'
    && typeof line.price?.currency === 'string';
}
