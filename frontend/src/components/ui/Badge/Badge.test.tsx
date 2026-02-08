/**
 * Badge Component Tests
 * Unit tests for the Badge component
 * Based on requirements 1.5, 3.2, 8.2
 */

import {render, screen} from '@testing-library/react';
import '@testing-library/jest-dom';
import Badge from './Badge';

describe('Badge Component', () => {
  // Basic rendering tests
  describe('Rendering', () => {
    it('renders badge with text', () => {
      render(<Badge>New</Badge>);
      expect(screen.getByText('New')).toBeInTheDocument();
    });

    it('applies custom className', () => {
      render(<Badge className="custom-badge">Badge</Badge>);
      expect(screen.getByText('Badge')).toHaveClass('custom-badge');
    });

    it('does not render when no content and not dot', () => {
      const { container } = render(<Badge />);
      expect(container.firstChild).toBeNull();
    });
  });

  // Variant tests
  describe('Variants', () => {
    it('applies default variant', () => {
      render(<Badge>Default</Badge>);
      expect(screen.getByText('Default')).toHaveClass('badge-default');
    });

    it('applies primary variant', () => {
      render(<Badge variant="primary">Primary</Badge>);
      expect(screen.getByText('Primary')).toHaveClass('badge-primary');
    });

    it('applies success variant', () => {
      render(<Badge variant="success">Success</Badge>);
      expect(screen.getByText('Success')).toHaveClass('badge-success');
    });

    it('applies error variant', () => {
      render(<Badge variant="error">Error</Badge>);
      expect(screen.getByText('Error')).toHaveClass('badge-error');
    });

    it('applies sale variant', () => {
      render(<Badge variant="sale">Sale</Badge>);
      expect(screen.getByText('Sale')).toHaveClass('badge-sale');
    });
  });

  // Size tests
  describe('Sizes', () => {
    it('applies medium size by default', () => {
      render(<Badge>Medium</Badge>);
      expect(screen.getByText('Medium')).toHaveClass('badge-md');
    });

    it('applies small size', () => {
      render(<Badge size="sm">Small</Badge>);
      expect(screen.getByText('Small')).toHaveClass('badge-sm');
    });

    it('applies large size', () => {
      render(<Badge size="lg">Large</Badge>);
      expect(screen.getByText('Large')).toHaveClass('badge-lg');
    });
  });

  // Count functionality tests
  describe('Count Functionality', () => {
    it('displays count value', () => {
      render(<Badge count={5} />);
      expect(screen.getByText('5')).toBeInTheDocument();
    });

    it('displays maxCount+ when count exceeds maxCount', () => {
      render(<Badge count={150} maxCount={99} />);
      expect(screen.getByText('99+')).toBeInTheDocument();
    });

    it('does not render when count is 0 and showZero is false', () => {
      const { container } = render(<Badge count={0} />);
      expect(container.firstChild).toBeNull();
    });

    it('renders when count is 0 and showZero is true', () => {
      render(<Badge count={0} showZero />);
      expect(screen.getByText('0')).toBeInTheDocument();
    });

    it('prioritizes count over children', () => {
      render(<Badge count={3}>Text</Badge>);
      expect(screen.getByText('3')).toBeInTheDocument();
      expect(screen.queryByText('Text')).not.toBeInTheDocument();
    });
  });

  // Dot variant tests
  describe('Dot Variant', () => {
    it('renders dot badge without text', () => {
      render(<Badge dot data-testid="dot-badge" />);
      const badge = screen.getByTestId('dot-badge');
      expect(badge).toHaveClass('badge-dot');
      expect(badge).toBeEmptyDOMElement();
    });

    it('renders dot badge even with children', () => {
      render(<Badge dot data-testid="dot-badge">Text</Badge>);
      const badge = screen.getByTestId('dot-badge');
      expect(badge).toHaveClass('badge-dot');
      expect(badge).toBeEmptyDOMElement();
    });

    it('renders dot badge even with count', () => {
      render(<Badge dot count={5} data-testid="dot-badge" />);
      const badge = screen.getByTestId('dot-badge');
      expect(badge).toHaveClass('badge-dot');
      expect(badge).toBeEmptyDOMElement();
    });
  });

  // Cart badge specific tests
  describe('Cart Badge Use Cases', () => {
    it('displays cart count correctly', () => {
      render(<Badge variant="primary" count={3} />);
      expect(screen.getByText('3')).toBeInTheDocument();
    });

    it('shows 99+ for high cart counts', () => {
      render(<Badge variant="primary" count={150} />);
      expect(screen.getByText('99+')).toBeInTheDocument();
    });

    it('hides badge when cart is empty', () => {
      const { container } = render(<Badge variant="primary" count={0} />);
      expect(container.firstChild).toBeNull();
    });
  });

  // Sale badge specific tests
  describe('Sale Badge Use Cases', () => {
    it('displays sale percentage', () => {
      render(<Badge variant="sale">-20%</Badge>);
      expect(screen.getByText('-20%')).toBeInTheDocument();
    });

    it('displays sale text', () => {
      render(<Badge variant="sale">SALE</Badge>);
      expect(screen.getByText('SALE')).toBeInTheDocument();
    });
  });

  // Status badge tests
  describe('Status Badge Use Cases', () => {
    it('displays in stock status', () => {
      render(<Badge variant="success">In Stock</Badge>);
      expect(screen.getByText('In Stock')).toBeInTheDocument();
    });

    it('displays out of stock status', () => {
      render(<Badge variant="error">Out of Stock</Badge>);
      expect(screen.getByText('Out of Stock')).toBeInTheDocument();
    });

    it('displays low stock warning', () => {
      render(<Badge variant="warning">Low Stock</Badge>);
      expect(screen.getByText('Low Stock')).toBeInTheDocument();
    });

    it('displays new product indicator', () => {
      render(<Badge variant="info">New</Badge>);
      expect(screen.getByText('New')).toBeInTheDocument();
    });
  });

  // Edge cases
  describe('Edge Cases', () => {
    it('handles empty string children', () => {
      const { container } = render(<Badge>{''}</Badge>);
      expect(container.firstChild).toBeNull();
    });

    it('handles null children', () => {
      const { container } = render(<Badge>{null}</Badge>);
      expect(container.firstChild).toBeNull();
    });

    it('handles undefined children', () => {
      const { container } = render(<Badge>{undefined}</Badge>);
      expect(container.firstChild).toBeNull();
    });

    it('handles zero count with custom maxCount', () => {
      const { container } = render(<Badge count={0} maxCount={50} />);
      expect(container.firstChild).toBeNull();
    });

    it('handles negative count', () => {
      render(<Badge count={-5} />);
      expect(screen.getByText('-5')).toBeInTheDocument();
    });

    it('combines multiple CSS classes correctly', () => {
      render(
        <Badge 
          variant="primary" 
          size="lg" 
          dot 
          className="custom-class"
          data-testid="complex-badge"
        />
      );
      const badge = screen.getByTestId('complex-badge');
      expect(badge).toHaveClass('badge');
      expect(badge).toHaveClass('badge-primary');
      expect(badge).toHaveClass('badge-lg');
      expect(badge).toHaveClass('badge-dot');
      expect(badge).toHaveClass('custom-class');
    });
  });
});