/**
 * ProductCard Component Property-Based Tests
 * Property tests for the ProductCard component using fast-check
 * **Feature: ecommerce-redesign**
 * Based on requirements 3.1, 3.3, 3.4
 */

import React from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import '@testing-library/jest-dom';
import * as fc from 'fast-check';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import ProductCard from './ProductCard';
import { CartProvider } from '../context/CartContext';

// Mock the cart context
const mockAddToCart = vi.fn();
const mockCartContext = {
  cart: { items: [], total: 0 },
  loading: false,
  error: null,
  addToCart: mockAddToCart,
  removeFromCart: vi.fn(),
  updateCartItem: vi.fn(),
  clearCart: vi.fn(),
  getCartTotal: vi.fn(() => 0),
  getCartItemCount: vi.fn(() => 0),
};

vi.mock('../context/CartContext', () => ({
  useCartContext: () => mockCartContext,
  CartProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

// Mock react-router-dom
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Test wrapper component
const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <BrowserRouter>
    <CartProvider userId="test-user">
      {children}
    </CartProvider>
  </BrowserRouter>
);

describe('ProductCard Component - Property Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  // Fast-check arbitraries for generating test data
  const productSkuArb = fc.string({ minLength: 3, maxLength: 20 }).filter(s => /^[A-Z0-9-]+$/i.test(s));
  const productTitleArb = fc.string({ minLength: 5, maxLength: 50 })
    .filter(s => s.trim().length >= 5)
    .map(s => s.replace(/[<>&"']/g, '').trim()) // Remove problematic characters
    .filter(s => s.length >= 5);
  const productPriceArb = fc.float({ min: Math.fround(0.01), max: Math.fround(999.99), noNaN: true });
  const currencyArb = fc.constantFrom('USD', 'EUR', 'GBP', 'CAD');
  const imageUrlArb = fc.webUrl().filter(url => url.includes('http'));
  const descriptionArb = fc.string({ minLength: 10, maxLength: 200 })
    .filter(s => s.trim().length >= 10)
    .map(s => s.replace(/[<>&"']/g, '').trim())
    .filter(s => s.length >= 10);
  const ratingArb = fc.option(fc.float({ min: Math.fround(0.1), max: Math.fround(5), noNaN: true }), { nil: undefined });
  const reviewsArb = fc.option(fc.integer({ min: 1, max: 1000 }), { nil: undefined });
  const booleanArb = fc.boolean();
  const quantityArb = fc.option(fc.integer({ min: 0, max: 100 }), { nil: undefined });

  const productArb = fc.record({
    sku: productSkuArb,
    title: productTitleArb,
    price: productPriceArb,
    currency: currencyArb,
    imageUrl: imageUrlArb,
    description: descriptionArb,
    rating: ratingArb,
    reviews: reviewsArb,
    inStock: booleanArb,
    quantity: quantityArb,
  });

  const variantArb = fc.constantFrom('default', 'compact', 'featured');

  /**
   * **Property 7: Product card essential information display**
   * **Validates: Requirements 3.1**
   * 
   * For any product, the product card should display image, brand name, title, price, and rating in a consistent layout
   */
  describe('Property 7: Product card essential information display', () => {
    it('should display all essential product information consistently across all product configurations', () => {
      fc.assert(
        fc.property(
          productArb,
          variantArb,
          booleanArb, // showQuickActions
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (product, variant, showQuickActions, uniqueId) => {
            const testId = `product-card-test-${uniqueId}-${Date.now()}`;
            const productWithTestId = { ...product, sku: `${product.sku}-${uniqueId}` };

            const { unmount } = render(
              <TestWrapper>
                <ProductCard
                  product={productWithTestId}
                  variant={variant}
                  showQuickActions={showQuickActions}
                  data-testid={testId}
                />
              </TestWrapper>
            );

            const productCard = screen.getByTestId(`product-card-${productWithTestId.sku}`);
            expect(productCard).toBeInTheDocument();

            // Property: Product image should always be displayed
            const productImage = productCard.querySelector('.product-image');
            expect(productImage).toBeInTheDocument();
            expect(productImage).toHaveAttribute('src', expect.stringContaining('http'));
            expect(productImage).toHaveClass('product-image');
            expect(productImage).toHaveAttribute('alt'); // Just check alt exists

            // Property: Brand name should be extracted and displayed from title
            const brandName = product.title.split(' ')[0];
            const brandElement = screen.getByText(brandName);
            expect(brandElement).toBeInTheDocument();
            expect(brandElement).toHaveClass('brand-name');

            // Property: Product title should be displayed as heading
            const titleElement = screen.getByText(product.title);
            expect(titleElement).toBeInTheDocument();
            expect(titleElement).toHaveClass('product-title');
            expect(titleElement.tagName.toLowerCase()).toBe('h3');

            // Property: Price should be displayed with currency (no space format)
            const priceText = `${product.currency}${product.price.toFixed(2)}`;
            const priceElement = screen.getByText(priceText);
            expect(priceElement).toBeInTheDocument();
            expect(priceElement).toHaveClass('current-price');
            expect(priceElement).toHaveAttribute('aria-label', 'Current price');

            // Property: Rating should be displayed when available
            if (product.rating !== undefined && product.rating > 0) {
              const ratingElement = screen.getByLabelText(`Rating: ${product.rating} out of 5 stars`);
              expect(ratingElement).toBeInTheDocument();
              expect(ratingElement).toHaveClass('stars');

              // Property: Star elements should be present
              const stars = ratingElement.querySelectorAll('.star');
              expect(stars).toHaveLength(5);

              // Property: Correct number of stars should be filled
              const filledStars = ratingElement.querySelectorAll('.star.filled');
              expect(filledStars).toHaveLength(Math.floor(product.rating));
            }

            // Property: Review count should be displayed when available
            if (product.reviews !== undefined && product.reviews > 0) {
              const reviewText = `(${product.reviews})`;
              const reviewElement = screen.getByText(reviewText);
              expect(reviewElement).toBeInTheDocument();
              expect(reviewElement).toHaveClass('rating-text');
            }

            // Property: Product card should have consistent layout structure
            const productInfo = productCard.querySelector('.product-info');
            expect(productInfo).toBeInTheDocument();

            const imageContainer = productCard.querySelector('.product-image-container');
            expect(imageContainer).toBeInTheDocument();

            // Property: Product card should have proper variant class
            expect(productCard).toHaveClass(variant);

            // Property: Product card should be accessible
            expect(productCard).toHaveAttribute('role', 'button');
            expect(productCard).toHaveAttribute('tabIndex', '0');
            expect(productCard).toHaveAttribute('aria-label', `View details for ${product.title}`);

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should handle edge cases in product information display', () => {
      fc.assert(
        fc.property(
          fc.record({
            sku: productSkuArb,
            title: fc.oneof(
              fc.string({ minLength: 5, maxLength: 20 }).map(s => s.replace(/[<>&"']/g, '').trim()).filter(s => s.length >= 5), // Short title
              fc.string({ minLength: 30, maxLength: 50 }).map(s => s.replace(/[<>&"']/g, '').trim()).filter(s => s.length >= 10), // Long title
              fc.constant('Brand Product Name') // Standard format
            ),
            price: fc.oneof(
              fc.constant(0.01), // Minimum price
              fc.constant(999.99), // Maximum price
              fc.float({ min: Math.fround(1), max: Math.fround(100), noNaN: true }) // Normal price
            ),
            currency: currencyArb,
            imageUrl: imageUrlArb,
            description: descriptionArb,
            rating: fc.oneof(
              fc.constant(undefined),
              fc.constant(0.1), // Very low rating
              fc.constant(5), // Perfect rating
              fc.float({ min: Math.fround(1), max: Math.fround(4.9), noNaN: true })
            ),
            reviews: fc.oneof(
              fc.constant(undefined),
              fc.constant(0),
              fc.constant(1),
              fc.integer({ min: 2, max: 10000 })
            ),
            inStock: booleanArb,
            quantity: quantityArb,
          }),
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (product, uniqueId) => {
            const productWithTestId = { ...product, sku: `${product.sku}-edge-${uniqueId}` };

            const { unmount } = render(
              <TestWrapper>
                <ProductCard product={productWithTestId} />
              </TestWrapper>
            );

            const productCard = screen.getByTestId(`product-card-${productWithTestId.sku}`);

            // Property: Essential elements should always be present regardless of edge cases
            expect(productCard).toBeInTheDocument();
            const productImage = productCard.querySelector('.product-image');
            expect(productImage).toBeInTheDocument();
            expect(productImage).toHaveAttribute('alt'); // Just check alt exists
            
            const titleElement = productCard.querySelector('.product-title');
            expect(titleElement).toBeInTheDocument();

            const priceText = `${product.currency}${product.price.toFixed(2)}`;
            const priceElement = productCard.querySelector('.current-price');
            expect(priceElement).toBeInTheDocument();
            expect(priceElement).toHaveTextContent(priceText);

            // Property: Brand name should be extracted even from edge case titles
            const brandElement = productCard.querySelector('.brand-name');
            expect(brandElement).toBeInTheDocument();

            // Property: Rating section should only appear when rating exists and is greater than 0
            if (product.rating !== undefined && product.rating > 0) {
              expect(screen.getByLabelText(`Rating: ${product.rating} out of 5 stars`)).toBeInTheDocument();
            } else {
              expect(screen.queryByLabelText(/Rating:/)).not.toBeInTheDocument();
            }

            // Property: Review count should only appear when reviews exist
            if (product.reviews !== undefined && product.reviews > 0) {
              const reviewText = `(${product.reviews})`;
              expect(screen.getByText(reviewText)).toBeInTheDocument();
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
   * **Property 9: Wishlist toggle functionality**
   * **Validates: Requirements 3.3**
   * 
   * For any product card, clicking the wishlist icon should toggle between filled and unfilled states and persist the selection
   */
  describe('Property 9: Wishlist toggle functionality', () => {
    it('should toggle wishlist state consistently for any product configuration', () => {
      fc.assert(
        fc.property(
          productArb,
          variantArb,
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (product, variant, uniqueId) => {
            const mockOnToggleWishlist = vi.fn();
            const productWithTestId = { ...product, sku: `${product.sku}-wishlist-${uniqueId}` };

            const { unmount } = render(
              <TestWrapper>
                <ProductCard
                  product={productWithTestId}
                  variant={variant}
                  onToggleWishlist={mockOnToggleWishlist}
                />
              </TestWrapper>
            );

            const wishlistButton = screen.getByTestId(`wishlist-button-${productWithTestId.sku}`);

            // Property: Wishlist button should always be present
            expect(wishlistButton).toBeInTheDocument();
            expect(wishlistButton).toHaveClass('wishlist-btn');
            expect(wishlistButton.tagName.toLowerCase()).toBe('button');

            // Property: Initial state should be unfilled (not active)
            expect(wishlistButton).not.toHaveClass('active');
            expect(wishlistButton).toHaveAttribute('aria-label', 'Add to wishlist');

            // Property: Wishlist button should contain SVG heart icon
            const heartIcon = wishlistButton.querySelector('svg');
            expect(heartIcon).toBeInTheDocument();
            expect(heartIcon).toHaveAttribute('fill', 'none'); // Initial unfilled state

            // Property: First click should toggle to filled state
            fireEvent.click(wishlistButton);

            expect(mockOnToggleWishlist).toHaveBeenCalledTimes(1);
            expect(mockOnToggleWishlist).toHaveBeenCalledWith(productWithTestId.sku);
            expect(wishlistButton).toHaveClass('active');
            expect(wishlistButton).toHaveAttribute('aria-label', 'Remove from wishlist');

            // Property: Heart icon should be filled after first click
            expect(heartIcon).toHaveAttribute('fill', 'currentColor');

            // Property: Second click should toggle back to unfilled state
            fireEvent.click(wishlistButton);

            expect(mockOnToggleWishlist).toHaveBeenCalledTimes(2);
            expect(mockOnToggleWishlist).toHaveBeenLastCalledWith(productWithTestId.sku);
            expect(wishlistButton).not.toHaveClass('active');
            expect(wishlistButton).toHaveAttribute('aria-label', 'Add to wishlist');
            expect(heartIcon).toHaveAttribute('fill', 'none');

            // Property: Multiple toggles should work consistently
            for (let i = 0; i < 3; i++) {
              fireEvent.click(wishlistButton);
              const expectedCallCount = 3 + i;
              const shouldBeActive = (expectedCallCount % 2) === 1;

              expect(mockOnToggleWishlist).toHaveBeenCalledTimes(expectedCallCount);

              if (shouldBeActive) {
                expect(wishlistButton).toHaveClass('active');
                expect(wishlistButton).toHaveAttribute('aria-label', 'Remove from wishlist');
                expect(heartIcon).toHaveAttribute('fill', 'currentColor');
              } else {
                expect(wishlistButton).not.toHaveClass('active');
                expect(wishlistButton).toHaveAttribute('aria-label', 'Add to wishlist');
                expect(heartIcon).toHaveAttribute('fill', 'none');
              }
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should handle wishlist toggle with event propagation correctly', async () => {
      fc.assert(
        fc.asyncProperty(
          productArb,
          fc.integer({ min: 1, max: 10000 }), // unique ID
          async (product, uniqueId) => {
            const mockOnToggleWishlist = vi.fn();
            const mockCardClick = vi.fn();
            const productWithTestId = { ...product, sku: `${product.sku}-propagation-${uniqueId}` };

            const { unmount } = render(
              <TestWrapper>
                <div onClick={mockCardClick}>
                  <ProductCard
                    product={productWithTestId}
                    onToggleWishlist={mockOnToggleWishlist}
                  />
                </div>
              </TestWrapper>
            );

            const wishlistButton = screen.getByTestId(`wishlist-button-${productWithTestId.sku}`);
            const productCard = screen.getByTestId(`product-card-${productWithTestId.sku}`);

            // Property: Wishlist click should not trigger parent card click
            fireEvent.click(wishlistButton);

            expect(mockOnToggleWishlist).toHaveBeenCalledTimes(1);
            expect(mockCardClick).not.toHaveBeenCalled();
            
            // Wait a bit to ensure no async navigation occurs
            await new Promise(resolve => setTimeout(resolve, 10));
            expect(mockNavigate).not.toHaveBeenCalled();

            // Reset mocks before testing card click
            mockNavigate.mockClear();

            // Property: Card click should still work after wishlist interaction
            fireEvent.click(productCard);
            expect(mockNavigate).toHaveBeenCalledWith(`/products/${productWithTestId.sku}`);

            // Property: Wishlist button should remain functional after card interaction
            fireEvent.click(wishlistButton);
            expect(mockOnToggleWishlist).toHaveBeenCalledTimes(2);

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should maintain wishlist accessibility across all interactions', () => {
      fc.assert(
        fc.property(
          productArb,
          fc.array(fc.constantFrom('click', 'focus', 'blur', 'mouseEnter', 'mouseLeave'), { minLength: 2, maxLength: 8 }),
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (product, interactionSequence, uniqueId) => {
            const mockOnToggleWishlist = vi.fn();
            const productWithTestId = { ...product, sku: `${product.sku}-a11y-${uniqueId}` };

            const { unmount } = render(
              <TestWrapper>
                <ProductCard
                  product={productWithTestId}
                  onToggleWishlist={mockOnToggleWishlist}
                />
              </TestWrapper>
            );

            const wishlistButton = screen.getByTestId(`wishlist-button-${productWithTestId.sku}`);

            // Property: Wishlist button should maintain accessibility through interaction sequence
            let clickCount = 0;
            interactionSequence.forEach(interaction => {
              if (interaction === 'click') {
                clickCount++;
              }

              expect(() => fireEvent[interaction](wishlistButton)).not.toThrow();

              // Property: Button should remain accessible after each interaction
              expect(wishlistButton).toBeInTheDocument();
              expect(wishlistButton.tagName.toLowerCase()).toBe('button');
              expect(wishlistButton).toHaveAttribute('aria-label');

              // Property: State should be consistent with click count
              const shouldBeActive = (clickCount % 2) === 1;
              if (shouldBeActive) {
                expect(wishlistButton).toHaveClass('active');
                expect(wishlistButton).toHaveAttribute('aria-label', 'Remove from wishlist');
              } else {
                expect(wishlistButton).not.toHaveClass('active');
                expect(wishlistButton).toHaveAttribute('aria-label', 'Add to wishlist');
              }
            });

            expect(mockOnToggleWishlist).toHaveBeenCalledTimes(clickCount);

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });
  });

  /**
   * **Property 10: Product card hover action reveal**
   * **Validates: Requirements 3.4**
   * 
   * For any product card, hovering should reveal quick action buttons (Quick Add, View Details) with smooth transitions
   */
  describe('Property 10: Product card hover action reveal', () => {
    it('should reveal and hide action buttons consistently on hover for any product configuration', () => {
      fc.assert(
        fc.property(
          productArb,
          variantArb,
          booleanArb, // showQuickActions
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (product, variant, showQuickActions, uniqueId) => {
            const mockOnAddToCart = vi.fn();
            const mockOnQuickView = vi.fn();
            const productWithTestId = { ...product, sku: `${product.sku}-hover-${uniqueId}` };

            const { unmount } = render(
              <TestWrapper>
                <ProductCard
                  product={productWithTestId}
                  variant={variant}
                  showQuickActions={showQuickActions}
                  onAddToCart={mockOnAddToCart}
                  onQuickView={mockOnQuickView}
                />
              </TestWrapper>
            );

            const productCard = screen.getByTestId(`product-card-${productWithTestId.sku}`);

            // Property: Product card should initially not have hover state
            expect(productCard).not.toHaveClass('hovered');

            if (showQuickActions) {
              // Property: Action buttons should exist but not be visible initially
              const actionButtons = productCard.querySelector('.action-buttons');
              expect(actionButtons).toBeInTheDocument();
              expect(actionButtons).not.toHaveClass('visible');

              const viewButton = screen.getByTestId(`view-button-${productWithTestId.sku}`);
              const addButton = screen.getByTestId(`add-button-${productWithTestId.sku}`);

              expect(viewButton).toBeInTheDocument();
              expect(addButton).toBeInTheDocument();
              expect(viewButton).toHaveClass('action-btn', 'view-details-btn');
              expect(addButton).toHaveClass('action-btn', 'quick-add-btn');

              // Property: Mouse enter should reveal action buttons
              fireEvent.mouseEnter(productCard);

              expect(productCard).toHaveClass('hovered');
              expect(actionButtons).toHaveClass('visible');

              // Property: Action buttons should be functional when visible
              expect(viewButton).toHaveTextContent('View Details');
              expect(viewButton).toHaveAttribute('aria-label', 'Quick view product details');

              if (product.inStock) {
                expect(addButton).toHaveTextContent('Quick Add');
                expect(addButton).toHaveAttribute('aria-label', 'Quick add to cart');
                expect(addButton).not.toBeDisabled();
              } else {
                expect(addButton).toHaveTextContent('Sold Out');
                expect(addButton).toBeDisabled();
              }

              // Property: Action buttons should work when clicked during hover
              fireEvent.click(viewButton);
              expect(mockOnQuickView).toHaveBeenCalledWith(productWithTestId);

              if (product.inStock) {
                fireEvent.click(addButton);
                // Wait for async operation
                expect(mockAddToCart).toHaveBeenCalled();
              }

              // Property: Mouse leave should hide action buttons
              fireEvent.mouseLeave(productCard);

              expect(productCard).not.toHaveClass('hovered');
              expect(actionButtons).not.toHaveClass('visible');

              // Property: Multiple hover cycles should work consistently
              for (let i = 0; i < 3; i++) {
                fireEvent.mouseEnter(productCard);
                expect(productCard).toHaveClass('hovered');
                expect(actionButtons).toHaveClass('visible');

                fireEvent.mouseLeave(productCard);
                expect(productCard).not.toHaveClass('hovered');
                expect(actionButtons).not.toHaveClass('visible');
              }
            } else {
              // Property: When showQuickActions is false, action buttons should not be present
              expect(productCard.querySelector('.action-buttons')).not.toBeInTheDocument();
              expect(screen.queryByTestId(`view-button-${productWithTestId.sku}`)).not.toBeInTheDocument();
              expect(screen.queryByTestId(`add-button-${productWithTestId.sku}`)).not.toBeInTheDocument();

              // Property: Hover should still work for card styling
              fireEvent.mouseEnter(productCard);
              expect(productCard).toHaveClass('hovered');

              fireEvent.mouseLeave(productCard);
              expect(productCard).not.toHaveClass('hovered');
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should handle action button states correctly based on product stock status', () => {
      fc.assert(
        fc.property(
          fc.record({
            sku: productSkuArb,
            title: productTitleArb,
            price: productPriceArb,
            currency: currencyArb,
            imageUrl: imageUrlArb,
            description: descriptionArb,
            rating: ratingArb,
            reviews: reviewsArb,
            inStock: booleanArb,
            quantity: quantityArb,
          }),
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (product, uniqueId) => {
            const mockOnAddToCart = vi.fn();
            const mockOnQuickView = vi.fn();
            const productWithTestId = { ...product, sku: `${product.sku}-stock-${uniqueId}` };

            const { unmount } = render(
              <TestWrapper>
                <ProductCard
                  product={productWithTestId}
                  showQuickActions={true}
                  onAddToCart={mockOnAddToCart}
                  onQuickView={mockOnQuickView}
                />
              </TestWrapper>
            );

            const productCard = screen.getByTestId(`product-card-${productWithTestId.sku}`);
            fireEvent.mouseEnter(productCard);

            const viewButton = screen.getByTestId(`view-button-${productWithTestId.sku}`);
            const addButton = screen.getByTestId(`add-button-${productWithTestId.sku}`);

            // Property: View button should always be functional regardless of stock status
            expect(viewButton).not.toBeDisabled();
            expect(viewButton).toHaveTextContent('View Details');

            fireEvent.click(viewButton);
            expect(mockOnQuickView).toHaveBeenCalledWith(productWithTestId);

            // Property: Add button state should reflect stock status
            if (product.inStock) {
              expect(addButton).not.toBeDisabled();
              expect(addButton).toHaveTextContent('Quick Add');
              expect(addButton).not.toHaveClass('loading');

              // Property: In-stock products should allow add to cart
              fireEvent.click(addButton);
              expect(mockAddToCart).toHaveBeenCalled();
            } else {
              expect(addButton).toBeDisabled();
              expect(addButton).toHaveTextContent('Sold Out');

              // Property: Out-of-stock products should not allow add to cart
              fireEvent.click(addButton);
              expect(mockOnAddToCart).not.toHaveBeenCalled();
            }

            // Property: Product card should have appropriate out-of-stock styling
            if (!product.inStock) {
              expect(productCard).toHaveClass('out-of-stock');
            } else {
              expect(productCard).not.toHaveClass('out-of-stock');
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should handle loading states during add to cart operations', () => {
      fc.assert(
        fc.property(
          productArb.filter(p => p.inStock), // Only test with in-stock products
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (product, uniqueId) => {
            const mockOnAddToCart = vi.fn();
            const productWithTestId = { ...product, sku: `${product.sku}-loading-${uniqueId}` };

            // Mock the cart context's addToCart method to return a resolved promise
            mockAddToCart.mockResolvedValue(undefined);

            const { unmount } = render(
              <TestWrapper>
                <ProductCard
                  product={productWithTestId}
                  showQuickActions={true}
                  onAddToCart={mockOnAddToCart}
                />
              </TestWrapper>
            );

            const productCard = screen.getByTestId(`product-card-${productWithTestId.sku}`);
            fireEvent.mouseEnter(productCard);

            const addButton = screen.getByTestId(`add-button-${productWithTestId.sku}`);

            // Property: Button should be in normal state initially
            expect(addButton).not.toBeDisabled();
            expect(addButton).toHaveTextContent('Quick Add');
            expect(addButton).not.toHaveClass('loading');

            // Property: Clicking should trigger the cart context's addToCart method
            fireEvent.click(addButton);
            expect(mockAddToCart).toHaveBeenCalledWith(productWithTestId, 1);

            // Property: Button should show loading state immediately after click
            expect(addButton).toHaveTextContent('Adding...');
            expect(addButton).toHaveClass('loading');
            expect(addButton).toBeDisabled();

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });

    it('should maintain hover state consistency across rapid interactions', () => {
      fc.assert(
        fc.property(
          productArb,
          fc.array(fc.constantFrom('mouseEnter', 'mouseLeave'), { minLength: 4, maxLength: 10 }),
          fc.integer({ min: 1, max: 10000 }), // unique ID
          (product, hoverSequence, uniqueId) => {
            const productWithTestId = { ...product, sku: `${product.sku}-rapid-${uniqueId}` };

            const { unmount } = render(
              <TestWrapper>
                <ProductCard
                  product={productWithTestId}
                  showQuickActions={true}
                />
              </TestWrapper>
            );

            const productCard = screen.getByTestId(`product-card-${productWithTestId.sku}`);
            const actionButtons = productCard.querySelector('.action-buttons');

            // Property: Rapid hover interactions should maintain consistent state
            let expectedHoverState = false;

            hoverSequence.forEach(interaction => {
              fireEvent[interaction](productCard);

              if (interaction === 'mouseEnter') {
                expectedHoverState = true;
              } else if (interaction === 'mouseLeave') {
                expectedHoverState = false;
              }

              // Property: Hover state should be consistent with last interaction
              if (expectedHoverState) {
                expect(productCard).toHaveClass('hovered');
                expect(actionButtons).toHaveClass('visible');
              } else {
                expect(productCard).not.toHaveClass('hovered');
                expect(actionButtons).not.toHaveClass('visible');
              }
            });

            // Property: Final state should match the last interaction
            const lastInteraction = hoverSequence[hoverSequence.length - 1];
            if (lastInteraction === 'mouseEnter') {
              expect(productCard).toHaveClass('hovered');
              expect(actionButtons).toHaveClass('visible');
            } else {
              expect(productCard).not.toHaveClass('hovered');
              expect(actionButtons).not.toHaveClass('visible');
            }

            // Cleanup
            unmount();
          }
        ),
        { numRuns: 100 }
      );
    });
  });
});