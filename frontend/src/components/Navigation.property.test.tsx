/**
 * Navigation Component Property-Based Tests
 */

import {cleanup, render, screen} from '@testing-library/react';
import {BrowserRouter} from 'react-router-dom';
import '@testing-library/jest-dom';
import * as fc from 'fast-check';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import Navigation from './Navigation';
import {AuthProvider, CartProvider} from '../context';
import {Cart, CartItem, User} from '../types';
import {useCart} from '../hooks';

vi.mock('../hooks/useCart', () => ({
  useCart: vi.fn()
}));

const mockUseCart = vi.mocked(useCart);

const mockUser: User = {
  id: 'test-user',
  name: 'Test User',
  email: 'test@example.com',
  role: 'USER'
};

const createMockCartItem = (sku: string, quantity: number): CartItem => ({
  sku,
  title: `Product ${sku}`,
  price: 10.99,
  currency: 'USD',
  imageUrl: `https://example.com/image-${sku}.jpg`,
  description: `Description for product ${sku}`,
  inStock: true,
  cartQuantity: quantity
});

const createMockCart = (totalItems: number): Cart => {
  const items: CartItem[] = [];
  let remainingItems = totalItems;
  let itemIndex = 0;

  while (remainingItems > 0) {
    const quantity = Math.min(remainingItems, Math.floor(Math.random() * 5) + 1);
    items.push(createMockCartItem(`item-${itemIndex}`, quantity));
    remainingItems -= quantity;
    itemIndex++;
  }

  return {
    items,
    total: items.reduce((sum, item) => sum + (item.price * item.cartQuantity), 0)
  };
};

const TestWrapper: React.FC<{
  children: React.ReactNode;
  userId?: string;
  cartItemCount?: number;
}> = ({
  children,
  userId = mockUser.id,
  cartItemCount = 0
}) => {
    const mockCart = createMockCart(cartItemCount);

    mockUseCart.mockReturnValue({
      cart: mockCart,
      loading: false,
      error: null,
      fetchCart: vi.fn(),
      addToCart: vi.fn(),
      removeFromCart: vi.fn(),
      updateCartItem: vi.fn(),
      getCartTotal: vi.fn(() => mockCart.total)
    });

    return (
      <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
        <AuthProvider initialUser={mockUser}>
          <CartProvider userId={userId}>
            {children}
          </CartProvider>
        </AuthProvider>
      </BrowserRouter>
    );
  };

describe('Navigation Component - Property Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  describe('Cart State Display Management', () => {
    it('should display cart badge with accurate item count for any cart state', () => {
      fc.assert(
        fc.property(
          fc.integer({ min: 0, max: 99 }),
          (itemCount) => {
            const { unmount } = render(
              <TestWrapper cartItemCount={itemCount}>
                <Navigation />
              </TestWrapper>
            );

            const cartLink = screen.getByTestId('cart-link');
            expect(cartLink).toBeInTheDocument();
            expect(cartLink).toHaveAttribute('href', '/cart');

            if (itemCount > 0) {
              const cartBadge = screen.getByTestId('cart-badge');
              expect(cartBadge).toBeInTheDocument();
              expect(cartBadge).toHaveTextContent(itemCount.toString());
            } else {
              expect(screen.queryByTestId('cart-badge')).not.toBeInTheDocument();
            }

            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });
  });
});
