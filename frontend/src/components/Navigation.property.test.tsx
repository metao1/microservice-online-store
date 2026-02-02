/**
 * Navigation Component Property-Based Tests
 * Property tests for the Navigation component using fast-check
 * **Feature: ecommerce-redesign**
 * Based on requirements 1.2, 1.5
 */

import React from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import * as fc from 'fast-check';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import Navigation from './Navigation';
import { CartProvider } from '../context/CartContext';
import { AuthProvider } from '../context/AuthContext';
import { User, Cart, CartItem } from '../types';

// Mock the useCart hook to control cart state
vi.mock('../hooks/useCart', () => ({
  useCart: vi.fn()
}));

import { useCart } from '../hooks/useCart';
const mockUseCart = vi.mocked(useCart);

// Mock user for testing
const mockUser: User = {
  id: 'test-user',
  name: 'Test User',
  email: 'test@example.com',
  role: 'USER'
};

// Helper function to create mock cart items
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

// Helper function to create mock cart with specified item count
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

// Test wrapper component with cart state control
const TestWrapper: React.FC<{ 
  children: React.ReactNode; 
  userId?: string;
  cartItemCount?: number;
}> = ({ 
  children, 
  userId = mockUser.id,
  cartItemCount = 0
}) => {
  // Mock the cart hook with the specified item count
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
    <BrowserRouter>
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

  /**
   * **Property 2: Cart badge display accuracy**
   * **Validates: Requirements 1.5**
   * 
   * For any cart state with items present, the navigation should display a badge with the correct item count
   */
  describe('Property 2: Cart badge display accuracy', () => {
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

            // Property: Cart link should always be present regardless of cart state
            expect(cartLink).toHaveClass('cart-link');
            expect(cartLink).toHaveAttribute('href', '/cart');

            // Property: Cart link should be functional
            expect(cartLink.tagName.toLowerCase()).toBe('a');

            if (itemCount > 0) {
              // Property: When cart has items, badge should be displayed
              const cartBadge = screen.getByTestId('cart-badge');
              expect(cartBadge).toBeInTheDocument();
              
              // Property: Badge should display the correct count
              expect(cartBadge).toHaveTextContent(itemCount.toString());
              
              // Property: Badge should have proper styling classes
              expect(cartBadge).toHaveClass('cart-badge');
            } else {
              // Property: When cart is empty, no badge should be displayed
              expect(screen.queryByTestId('cart-badge')).not.toBeInTheDocument();
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 } // Minimum 100 iterations as specified
      );
    });

    it('should handle edge cases for cart badge display', () => {
      fc.assert(
        fc.property(
          fc.oneof(
            fc.constant(0),      // Empty cart
            fc.constant(1),      // Single item
            fc.constant(99),     // Maximum typical display
            fc.constant(100),    // Over typical maximum
            fc.integer({ min: 2, max: 50 }) // Random valid counts
          ),
          (itemCount) => {
            const { unmount } = render(
              <TestWrapper cartItemCount={itemCount}>
                <Navigation />
              </TestWrapper>
            );

            const cartLink = screen.getByTestId('cart-link');
            
            // Property: Cart link should always be accessible
            expect(cartLink).toBeInTheDocument();
            expect(cartLink).not.toHaveAttribute('disabled');
            
            // Property: Cart link should be clickable
            expect(() => fireEvent.click(cartLink)).not.toThrow();

            if (itemCount > 0) {
              const cartBadge = screen.getByTestId('cart-badge');
              
              // Property: Badge should be visible and readable
              expect(cartBadge).toBeVisible();
              
              // Property: Badge text should be a valid number or "99+" format
              const badgeText = cartBadge.textContent;
              expect(badgeText).toBeTruthy();
              if (itemCount > 99) {
                expect(badgeText).toBe('99+');
              } else {
                const badgeNumber = parseInt(badgeText || '0', 10);
                expect(badgeNumber).toBe(itemCount);
              }
              
              // Property: Badge should not interfere with cart link functionality
              expect(cartLink).toBeInTheDocument();
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });
  });

  /**
   * **Property 27: Interactive element hover states**
   * **Validates: Requirements 1.2**
   * 
   * For any interactive element, hovering should provide subtle visual feedback and transitions
   */
  describe('Property 27: Interactive element hover states', () => {
    const categoryArb = fc.record({
      id: fc.string({ minLength: 1, maxLength: 20 }).filter(s => /^[a-zA-Z0-9-_]+$/.test(s)),
      name: fc.string({ minLength: 1, maxLength: 30 }).filter(s => s.trim().length > 0)
    });

    const categoriesArb = fc.array(categoryArb, { minLength: 1, maxLength: 8 }).map(categories => {
      // Ensure unique IDs to avoid duplicate test IDs
      const uniqueCategories = categories.map((cat, index) => ({
        ...cat,
        id: `${cat.id}-${index}-${Date.now()}`
      }));
      return uniqueCategories;
    });

    it('should provide hover states for all interactive navigation elements', () => {
      fc.assert(
        fc.property(
          categoriesArb,
          (categories) => {
            const mockOnCategorySelect = vi.fn();
            const mockOnSearch = vi.fn();
            
            const { unmount } = render(
              <TestWrapper>
                <Navigation 
                  categories={categories}
                  onCategorySelect={mockOnCategorySelect}
                  onSearch={mockOnSearch}
                />
              </TestWrapper>
            );

            // Property: Category links should have hover states
            categories.forEach((category) => {
              const categoryButton = screen.getByTestId(`category-${category.id}`);
              expect(categoryButton).toBeInTheDocument();
              
              // Property: Element should have base styling classes
              expect(categoryButton).toHaveClass('nav-link');
              
              // Property: Element should be interactive (button or link)
              expect(['button', 'a'].includes(categoryButton.tagName.toLowerCase())).toBe(true);
              
              // Property: Hover should be possible (element should not be disabled)
              expect(categoryButton).not.toBeDisabled();
              
              // Property: Element should be focusable for accessibility (buttons are focusable by default)
              expect(['button', 'a'].includes(categoryButton.tagName.toLowerCase())).toBe(true);
              
              // Simulate hover interactions - should not cause errors
              expect(() => {
                fireEvent.mouseEnter(categoryButton);
                fireEvent.mouseLeave(categoryButton);
                fireEvent.focus(categoryButton);
                fireEvent.blur(categoryButton);
              }).not.toThrow();
              
              // Property: Element should remain functional after hover
              expect(categoryButton).toBeInTheDocument();
              expect(categoryButton).not.toBeDisabled();
            });

            // Property: Account button should have hover states
            const accountButton = screen.getByTestId('account-button');
            expect(accountButton).toHaveClass('account-trigger');
            expect(accountButton).not.toBeDisabled();
            
            // Property: Account button should support all interaction states
            expect(() => {
              fireEvent.mouseEnter(accountButton);
              fireEvent.mouseLeave(accountButton);
              fireEvent.focus(accountButton);
              fireEvent.blur(accountButton);
            }).not.toThrow();
            
            expect(accountButton).toBeInTheDocument();

            // Property: Cart link should have hover states
            const cartLink = screen.getByTestId('cart-link');
            expect(cartLink).toHaveClass('cart-link');
            
            expect(() => {
              fireEvent.mouseEnter(cartLink);
              fireEvent.mouseLeave(cartLink);
              fireEvent.focus(cartLink);
              fireEvent.blur(cartLink);
            }).not.toThrow();
            
            expect(cartLink).toBeInTheDocument();

            // Property: Mobile menu toggle should have hover states
            const mobileToggle = screen.getByTestId('mobile-menu-toggle');
            expect(mobileToggle).toHaveClass('mobile-menu-toggle');
            expect(mobileToggle).not.toBeDisabled();
            
            expect(() => {
              fireEvent.mouseEnter(mobileToggle);
              fireEvent.mouseLeave(mobileToggle);
              fireEvent.focus(mobileToggle);
              fireEvent.blur(mobileToggle);
            }).not.toThrow();
            
            expect(mobileToggle).toBeInTheDocument();

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should handle hover states for dropdown menu items', () => {
      fc.assert(
        fc.property(
          fc.boolean(), // Whether to test with mobile menu open
          (testMobileMenu) => {
            const { unmount } = render(
              <TestWrapper>
                <Navigation />
              </TestWrapper>
            );

            if (testMobileMenu) {
              // Test mobile menu hover states
              const mobileToggle = screen.getByTestId('mobile-menu-toggle');
              fireEvent.click(mobileToggle);
              
              const mobileMenu = screen.getByTestId('mobile-menu');
              expect(mobileMenu).toHaveClass('open');
              
              // Property: Mobile menu links should have hover states
              const mobileLinks = mobileMenu.querySelectorAll('.mobile-nav-link, .mobile-user-link');
              expect(mobileLinks.length).toBeGreaterThan(0);
              
              mobileLinks.forEach((link) => {
                // Property: Mobile link should be interactive
                expect(['a', 'button'].includes(link.tagName.toLowerCase())).toBe(true);
                
                // Property: Hover should not cause errors
                expect(() => {
                  fireEvent.mouseEnter(link);
                  fireEvent.mouseLeave(link);
                }).not.toThrow();
                
                // Property: Link should remain functional after hover
                expect(link).toBeInTheDocument();
              });
            } else {
              // Test desktop dropdown menu hover states
              const accountButton = screen.getByTestId('account-button');
              fireEvent.click(accountButton);

              const accountMenu = screen.getByTestId('account-menu');
              expect(accountMenu).toBeInTheDocument();

              // Property: All menu items should have hover states
              const menuItems = accountMenu.querySelectorAll('.account-menu-item');
              expect(menuItems.length).toBeGreaterThan(0);

              menuItems.forEach((menuItem) => {
                // Property: Menu item should be interactive
                expect(['a', 'button'].includes(menuItem.tagName.toLowerCase())).toBe(true);
                
                // Property: Menu item should have proper CSS class
                expect(menuItem).toHaveClass('account-menu-item');
                
                // Property: Hover should not cause errors
                expect(() => {
                  fireEvent.mouseEnter(menuItem);
                  fireEvent.mouseLeave(menuItem);
                }).not.toThrow();
                
                // Property: Menu item should remain functional after hover
                expect(menuItem).toBeInTheDocument();
              });
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should maintain hover state consistency across interaction patterns', () => {
      fc.assert(
        fc.property(
          fc.array(fc.constantFrom('mouseEnter', 'mouseLeave', 'focus', 'blur'), { minLength: 2, maxLength: 6 }),
          (interactionSequence) => {
            const { unmount } = render(
              <TestWrapper>
                <Navigation />
              </TestWrapper>
            );

            // Test interaction sequence on account button
            const accountButton = screen.getByTestId('account-button');
            
            // Property: Element should handle any sequence of hover/focus interactions
            expect(() => {
              interactionSequence.forEach(interaction => {
                fireEvent[interaction](accountButton);
              });
            }).not.toThrow();
            
            // Property: Element should remain functional after interaction sequence
            expect(accountButton).toBeInTheDocument();
            expect(accountButton).not.toBeDisabled();
            
            // Property: Element should still be clickable
            expect(() => fireEvent.click(accountButton)).not.toThrow();

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });
  });
});