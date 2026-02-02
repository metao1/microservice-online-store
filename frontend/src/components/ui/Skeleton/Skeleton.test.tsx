/**
 * Skeleton Component Tests
 * Unit tests for the Skeleton component
 * Based on requirements 2.4, 7.7, 9.2
 */

import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import Skeleton, { 
  SkeletonText, 
  SkeletonAvatar, 
  SkeletonButton, 
  SkeletonCard,
  SkeletonProductCard,
  SkeletonNavigation
} from './Skeleton';
import { describe, expect, it } from 'vitest';

describe('Skeleton Component', () => {
  // Basic rendering tests
  describe('Rendering', () => {
    it('renders skeleton element', () => {
      render(<Skeleton data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toBeInTheDocument();
    });

    it('applies custom className', () => {
      render(<Skeleton className="custom-skeleton" data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveClass('custom-skeleton');
    });

    it('applies custom styles', () => {
      render(
        <Skeleton 
          style={{ backgroundColor: 'red' }} 
          data-testid="skeleton" 
        />
      );
      expect(screen.getByTestId('skeleton')).toHaveStyle('background-color: rgb(255, 0, 0)');
    });
  });

  // Variant tests
  describe('Variants', () => {
    it('applies text variant by default', () => {
      render(<Skeleton data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveClass('skeleton-text');
    });

    it('applies rectangular variant', () => {
      render(<Skeleton variant="rectangular" data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveClass('skeleton-rectangular');
    });

    it('applies circular variant', () => {
      render(<Skeleton variant="circular" data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveClass('skeleton-circular');
    });

    it('applies rounded variant', () => {
      render(<Skeleton variant="rounded" data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveClass('skeleton-rounded');
    });
  });

  // Animation tests
  describe('Animations', () => {
    it('applies pulse animation by default', () => {
      render(<Skeleton data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveClass('skeleton-pulse');
    });

    it('applies wave animation', () => {
      render(<Skeleton animation="wave" data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveClass('skeleton-wave');
    });

    it('applies no animation', () => {
      render(<Skeleton animation="none" data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveClass('skeleton-none');
    });
  });

  // Dimension tests
  describe('Dimensions', () => {
    it('applies width as string', () => {
      render(<Skeleton width="200px" data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveStyle('width: 200px');
    });

    it('applies width as number', () => {
      render(<Skeleton width={200} data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveStyle('width: 200px');
    });

    it('applies height as string', () => {
      render(<Skeleton height="100px" data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveStyle('height: 100px');
    });

    it('applies height as number', () => {
      render(<Skeleton height={100} data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveStyle('height: 100px');
    });

    it('applies aspect ratio', () => {
      render(<Skeleton aspectRatio="16/9" data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveStyle('aspect-ratio: 16/9');
    });
  });

  // Multi-line text tests
  describe('Multi-line Text', () => {
    it('renders single line by default', () => {
      render(<Skeleton variant="text" data-testid="skeleton" />);
      expect(screen.getByTestId('skeleton')).toHaveClass('skeleton-text');
    });

    it('renders multiple lines', () => {
      render(<Skeleton variant="text" lines={3} data-testid="skeleton-container" />);
      const container = screen.getByTestId('skeleton-container');
      expect(container).toHaveClass('skeleton-container');
      
      const skeletonElements = container.querySelectorAll('.skeleton-text');
      expect(skeletonElements).toHaveLength(3);
    });

    it('applies last line styling to final line', () => {
      render(<Skeleton variant="text" lines={2} data-testid="skeleton-container" />);
      const container = screen.getByTestId('skeleton-container');
      const skeletonElements = container.querySelectorAll('.skeleton-text');
      
      expect(skeletonElements[0]).not.toHaveClass('skeleton-text-last');
      expect(skeletonElements[1]).toHaveClass('skeleton-text-last');
    });
  });
});

// Predefined skeleton component tests
describe('Predefined Skeleton Components', () => {
  describe('SkeletonText', () => {
    it('renders text skeleton', () => {
      render(<SkeletonText data-testid="skeleton-text" />);
      expect(screen.getByTestId('skeleton-text')).toHaveClass('skeleton-text');
    });

    it('supports multiple lines', () => {
      render(<SkeletonText lines={3} data-testid="skeleton-text" />);
      const container = screen.getByTestId('skeleton-text');
      expect(container.querySelectorAll('.skeleton-text')).toHaveLength(3);
    });
  });

  describe('SkeletonAvatar', () => {
    it('renders circular skeleton with default size', () => {
      render(<SkeletonAvatar data-testid="skeleton-avatar" />);
      const avatar = screen.getByTestId('skeleton-avatar');
      expect(avatar).toHaveClass('skeleton-circular');
      expect(avatar).toHaveStyle('width: 40px');
      expect(avatar).toHaveStyle('height: 40px');
    });

    it('supports custom size', () => {
      render(<SkeletonAvatar width={60} height={60} data-testid="skeleton-avatar" />);
      const avatar = screen.getByTestId('skeleton-avatar');
      expect(avatar).toHaveStyle('width: 60px');
      expect(avatar).toHaveStyle('height: 60px');
    });
  });

  describe('SkeletonButton', () => {
    it('renders rounded skeleton with default size', () => {
      render(<SkeletonButton data-testid="skeleton-button" />);
      const button = screen.getByTestId('skeleton-button');
      expect(button).toHaveClass('skeleton-rounded');
      expect(button).toHaveStyle('width: 100px');
      expect(button).toHaveStyle('height: 40px');
    });

    it('supports custom size', () => {
      render(<SkeletonButton width={150} height={50} data-testid="skeleton-button" />);
      const button = screen.getByTestId('skeleton-button');
      expect(button).toHaveStyle('width: 150px');
      expect(button).toHaveStyle('height: 50px');
    });
  });

  describe('SkeletonCard', () => {
    it('renders card skeleton with all elements by default', () => {
      render(<SkeletonCard data-testid="skeleton-card" />);
      const card = screen.getByTestId('skeleton-card');
      expect(card).toHaveClass('skeleton-card');
      
      // Should have image, title, and description
      expect(card.querySelector('.skeleton-card-image')).toBeInTheDocument();
      expect(card.querySelector('.skeleton-card-title')).toBeInTheDocument();
      expect(card.querySelector('.skeleton-card-description')).toBeInTheDocument();
    });

    it('hides image when showImage is false', () => {
      render(<SkeletonCard showImage={false} data-testid="skeleton-card" />);
      const card = screen.getByTestId('skeleton-card');
      expect(card.querySelector('.skeleton-card-image')).not.toBeInTheDocument();
    });

    it('hides title when showTitle is false', () => {
      render(<SkeletonCard showTitle={false} data-testid="skeleton-card" />);
      const card = screen.getByTestId('skeleton-card');
      expect(card.querySelector('.skeleton-card-title')).not.toBeInTheDocument();
    });

    it('hides description when showDescription is false', () => {
      render(<SkeletonCard showDescription={false} data-testid="skeleton-card" />);
      const card = screen.getByTestId('skeleton-card');
      expect(card.querySelector('.skeleton-card-description')).not.toBeInTheDocument();
    });
  });

  describe('SkeletonProductCard', () => {
    it('renders product card skeleton', () => {
      render(<SkeletonProductCard data-testid="skeleton-product" />);
      const productCard = screen.getByTestId('skeleton-product');
      expect(productCard).toHaveClass('skeleton-product-card');
      
      // Should have all product card elements
      expect(productCard.querySelector('.skeleton-product-image')).toBeInTheDocument();
      expect(productCard.querySelector('.skeleton-product-brand')).toBeInTheDocument();
      expect(productCard.querySelector('.skeleton-product-title')).toBeInTheDocument();
      expect(productCard.querySelector('.skeleton-product-price')).toBeInTheDocument();
      expect(productCard.querySelector('.skeleton-product-rating')).toBeInTheDocument();
    });

    it('applies custom className', () => {
      render(<SkeletonProductCard className="custom-product" data-testid="skeleton-product" />);
      expect(screen.getByTestId('skeleton-product')).toHaveClass('custom-product');
    });
  });

  describe('SkeletonNavigation', () => {
    it('renders navigation skeleton', () => {
      render(<SkeletonNavigation data-testid="skeleton-nav" />);
      const nav = screen.getByTestId('skeleton-nav');
      expect(nav).toHaveClass('skeleton-navigation');
      
      // Should have left, center, and right sections
      expect(nav.querySelector('.skeleton-nav-left')).toBeInTheDocument();
      expect(nav.querySelector('.skeleton-nav-center')).toBeInTheDocument();
      expect(nav.querySelector('.skeleton-nav-right')).toBeInTheDocument();
    });

    it('applies custom className', () => {
      render(<SkeletonNavigation className="custom-nav" data-testid="skeleton-nav" />);
      expect(screen.getByTestId('skeleton-nav')).toHaveClass('custom-nav');
    });
  });
});