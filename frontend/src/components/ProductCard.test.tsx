import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { vi } from 'vitest';
import ProductCard from './ProductCard';
import { CartProvider } from '../context/CartContext';
import { Product } from '../types';

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

const mockProduct: Product = {
  sku: 'TEST-001',
  title: 'Test Product Title',
  price: 99.99,
  currency: '$',
  imageUrl: 'https://example.com/test-image.jpg',
  description: 'Test product description',
  rating: 4.5,
  reviews: 123,
  inStock: true,
  quantity: 10,
};

const renderProductCard = (product: Product = mockProduct, props = {}) => {
  return render(
    <BrowserRouter>
      <CartProvider userId="test-user">
        <ProductCard product={product} {...props} />
      </CartProvider>
    </BrowserRouter>
  );
};

describe('ProductCard Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Rendering', () => {
    it('renders product card with essential information', () => {
      renderProductCard();
      
      expect(screen.getByTestId(`product-card-${mockProduct.sku}`)).toBeInTheDocument();
      expect(screen.getByText(mockProduct.title)).toBeInTheDocument();
      expect(screen.getByText(`${mockProduct.currency} ${mockProduct.price.toFixed(2)}`)).toBeInTheDocument();
      expect(screen.getByAltText(mockProduct.title)).toBeInTheDocument();
    });

    it('displays brand name extracted from title', () => {
      renderProductCard();
      
      const brandName = mockProduct.title.split(' ')[0];
      expect(screen.getByText(brandName)).toBeInTheDocument();
    });

    it('displays rating when available', () => {
      renderProductCard();
      
      expect(screen.getByLabelText(`Rating: ${mockProduct.rating} out of 5 stars`)).toBeInTheDocument();
      expect(screen.getByText(`(${mockProduct.reviews})`)).toBeInTheDocument();
    });

    it('shows bestseller badge for high-rated products', () => {
      renderProductCard();
      
      expect(screen.getByText('Bestseller')).toBeInTheDocument();
    });

    it('displays sold out badge for out-of-stock products', () => {
      const outOfStockProduct = { ...mockProduct, inStock: false };
      const { container } = renderProductCard(outOfStockProduct);
      
      // Check for the status badge specifically using querySelector
      const statusBadge = container.querySelector('.status-badge.sold-out');
      expect(statusBadge).toBeInTheDocument();
      expect(statusBadge).toHaveTextContent('Sold Out');
    });
  });

  describe('Wishlist Functionality', () => {
    it('renders wishlist button', () => {
      renderProductCard();
      
      const wishlistBtn = screen.getByTestId(`wishlist-button-${mockProduct.sku}`);
      expect(wishlistBtn).toBeInTheDocument();
      expect(wishlistBtn).toHaveAttribute('aria-label', 'Add to wishlist');
    });

    it('toggles wishlist state when clicked', () => {
      const onToggleWishlist = vi.fn();
      renderProductCard(mockProduct, { onToggleWishlist });
      
      const wishlistBtn = screen.getByTestId(`wishlist-button-${mockProduct.sku}`);
      fireEvent.click(wishlistBtn);
      
      expect(onToggleWishlist).toHaveBeenCalledWith(mockProduct.sku);
      expect(wishlistBtn).toHaveAttribute('aria-label', 'Remove from wishlist');
    });
  });

  describe('Quick Actions', () => {
    it('shows action buttons on hover', () => {
      renderProductCard();
      
      const productCard = screen.getByTestId(`product-card-${mockProduct.sku}`);
      fireEvent.mouseEnter(productCard);
      
      expect(screen.getByTestId(`view-button-${mockProduct.sku}`)).toBeInTheDocument();
      expect(screen.getByTestId(`add-button-${mockProduct.sku}`)).toBeInTheDocument();
    });

    it('calls quick add function when quick add button is clicked', async () => {
      const onAddToCart = vi.fn();
      renderProductCard(mockProduct, { onAddToCart });
      
      const productCard = screen.getByTestId(`product-card-${mockProduct.sku}`);
      fireEvent.mouseEnter(productCard);
      
      const quickAddBtn = screen.getByTestId(`add-button-${mockProduct.sku}`);
      fireEvent.click(quickAddBtn);
      
      await waitFor(() => {
        expect(mockAddToCart).toHaveBeenCalledWith(mockProduct, 1);
      });
    });

    it('disables quick add button for out-of-stock products', () => {
      const outOfStockProduct = { ...mockProduct, inStock: false };
      renderProductCard(outOfStockProduct);
      
      const productCard = screen.getByTestId(`product-card-${outOfStockProduct.sku}`);
      fireEvent.mouseEnter(productCard);
      
      const quickAddBtn = screen.getByTestId(`add-button-${outOfStockProduct.sku}`);
      expect(quickAddBtn).toBeDisabled();
      expect(quickAddBtn).toHaveTextContent('Sold Out');
    });

    it('calls quick view function when view details button is clicked', () => {
      const onQuickView = vi.fn();
      renderProductCard(mockProduct, { onQuickView });
      
      const productCard = screen.getByTestId(`product-card-${mockProduct.sku}`);
      fireEvent.mouseEnter(productCard);
      
      const viewBtn = screen.getByTestId(`view-button-${mockProduct.sku}`);
      fireEvent.click(viewBtn);
      
      expect(onQuickView).toHaveBeenCalledWith(mockProduct);
    });
  });

  describe('Navigation', () => {
    it('navigates to product detail page when card is clicked', () => {
      renderProductCard();
      
      const productCard = screen.getByTestId(`product-card-${mockProduct.sku}`);
      fireEvent.click(productCard);
      
      expect(mockNavigate).toHaveBeenCalledWith(`/products/${mockProduct.sku}`);
    });

    it('supports keyboard navigation', () => {
      renderProductCard();
      
      const productCard = screen.getByTestId(`product-card-${mockProduct.sku}`);
      fireEvent.keyDown(productCard, { key: 'Enter' });
      
      expect(mockNavigate).toHaveBeenCalledWith(`/products/${mockProduct.sku}`);
    });
  });

  describe('Accessibility', () => {
    it('has proper ARIA labels', () => {
      renderProductCard();
      
      const productCard = screen.getByTestId(`product-card-${mockProduct.sku}`);
      expect(productCard).toHaveAttribute('aria-label', `View details for ${mockProduct.title}`);
      expect(productCard).toHaveAttribute('role', 'button');
      expect(productCard).toHaveAttribute('tabIndex', '0');
    });

    it('provides proper alt text for product image', () => {
      renderProductCard();
      
      const productImage = screen.getByAltText(mockProduct.title);
      expect(productImage).toBeInTheDocument();
    });

    it('has proper ARIA labels for price information', () => {
      renderProductCard();
      
      expect(screen.getByLabelText('Current price')).toBeInTheDocument();
    });
  });

  describe('Variants', () => {
    it('applies compact variant class', () => {
      renderProductCard(mockProduct, { variant: 'compact' });
      
      const productCard = screen.getByTestId(`product-card-${mockProduct.sku}`);
      expect(productCard).toHaveClass('compact');
    });

    it('applies featured variant class', () => {
      renderProductCard(mockProduct, { variant: 'featured' });
      
      const productCard = screen.getByTestId(`product-card-${mockProduct.sku}`);
      expect(productCard).toHaveClass('featured');
    });

    it('hides quick actions when showQuickActions is false', () => {
      renderProductCard(mockProduct, { showQuickActions: false });
      
      const productCard = screen.getByTestId(`product-card-${mockProduct.sku}`);
      fireEvent.mouseEnter(productCard);
      
      expect(screen.queryByTestId(`view-button-${mockProduct.sku}`)).not.toBeInTheDocument();
      expect(screen.queryByTestId(`add-button-${mockProduct.sku}`)).not.toBeInTheDocument();
    });
  });

  describe('Loading States', () => {
    it('shows loading state during add to cart', async () => {
      mockAddToCart.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));
      
      renderProductCard();
      
      const productCard = screen.getByTestId(`product-card-${mockProduct.sku}`);
      fireEvent.mouseEnter(productCard);
      
      const quickAddBtn = screen.getByTestId(`add-button-${mockProduct.sku}`);
      fireEvent.click(quickAddBtn);
      
      expect(quickAddBtn).toHaveTextContent('Adding...');
      expect(quickAddBtn).toHaveClass('loading');
      
      await waitFor(() => {
        expect(quickAddBtn).toHaveTextContent('Quick Add');
      });
    });
  });
});