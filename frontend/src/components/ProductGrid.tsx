/**
 * ProductGrid Component - E-commerce Redesign
 * Responsive product grid with infinite scroll and loading states
 * Based on requirements 2.1, 2.3, 2.4, 2.6
 */

import React, { FC, useEffect, useCallback, useRef } from 'react';
import { Product, ProductVariant } from '@types';
import ProductCard from './ProductCard';
import { Grid } from './layout/Grid/Grid';
import Skeleton from './ui/Skeleton/Skeleton';
import './ProductGrid.css';

export interface ProductGridProps {
  /** Array of products to display */
  products: Product[];
  /** Loading state for initial load */
  loading: boolean;
  /** Loading state for loading more products */
  loadingMore?: boolean;
  /** Whether there are more products to load */
  hasMore?: boolean;
  /** View mode for product display */
  viewMode?: 'grid' | 'list';
  /** Number of skeleton items to show while loading */
  skeletonCount?: number;
  /** Callback when a product is selected */
  onProductSelect?: (product: Product) => void;
  /** Callback when a product is added to cart */
  onAddToCart?: (product: Product, selectedVariants?: ProductVariant[]) => void;
  /** Callback when wishlist is toggled */
  onToggleWishlist?: (productId: string) => void;
  /** Callback when quick view is requested */
  onQuickView?: (product: Product) => void;
  /** Callback to load more products */
  onLoadMore?: () => void;
  /** Custom empty state message */
  emptyMessage?: string;
  /** Custom empty state action */
  emptyAction?: React.ReactNode;
  /** Additional CSS classes */
  className?: string;
  /** Enable infinite scroll */
  infiniteScroll?: boolean;
  /** Threshold for infinite scroll trigger (in pixels from bottom) */
  scrollThreshold?: number;
}

/**
 * ProductGrid component provides a responsive grid layout for products
 * with advanced features like infinite scroll, loading states, and empty states
 * 
 * Features:
 * - Responsive grid layout (4/3/2/1 columns based on screen size)
 * - Infinite scroll or pagination support
 * - Loading skeleton states
 * - Empty state handling
 * - Product card interactions (add to cart, wishlist, quick view)
 * - Optimized performance with intersection observer
 * 
 * @example
 * ```tsx
 * <ProductGrid
 *   products={products}
 *   loading={loading}
 *   hasMore={hasMore}
 *   onLoadMore={loadMore}
 *   onAddToCart={handleAddToCart}
 *   onToggleWishlist={handleWishlist}
 *   infiniteScroll={true}
 * />
 * ```
 */
export const ProductGrid: FC<ProductGridProps> = ({
  products,
  loading,
  loadingMore = false,
  hasMore = false,
  viewMode = 'grid',
  skeletonCount = 12,
  onProductSelect,
  onAddToCart,
  onToggleWishlist,
  onQuickView,
  onLoadMore,
  emptyMessage = "No products found",
  emptyAction,
  className = '',
  infiniteScroll = true,
  scrollThreshold = 200
}) => {
  const loadMoreRef = useRef<HTMLDivElement>(null);
  const observerRef = useRef<IntersectionObserver | null>(null);
  const lastTriggerAtRef = useRef(0);

  const triggerLoadMore = useCallback(() => {
    if (!onLoadMore || !hasMore || loadingMore) return;
    const now = Date.now();
    if (now - lastTriggerAtRef.current < 400) return;
    lastTriggerAtRef.current = now;
    onLoadMore();
  }, [onLoadMore, hasMore, loadingMore]);

  // Set up intersection observer for infinite scroll
  useEffect(() => {
    if (!infiniteScroll || !onLoadMore || !hasMore || loadingMore) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries;
        if (entry.isIntersecting) {
          triggerLoadMore();
        }
      },
      {
        rootMargin: `0px 0px ${scrollThreshold}px 0px`,
        threshold: 0.1
      }
    );

    if (loadMoreRef.current) {
      observer.observe(loadMoreRef.current);
    }

    observerRef.current = observer;

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [infiniteScroll, hasMore, loadingMore, scrollThreshold, triggerLoadMore]);

  // Fallback/assistive scroll trigger so loading happens when the user hits the bottom.
  useEffect(() => {
    if (!infiniteScroll || !onLoadMore || !hasMore || loadingMore) return;

    const handleScroll = () => {
      const doc = document.documentElement;
      const scrollTop = window.scrollY || doc.scrollTop || 0;
      const viewportH = window.innerHeight || doc.clientHeight || 0;
      const pageH = doc.scrollHeight || document.body.scrollHeight || 0;
      const nearBottom = scrollTop + viewportH >= pageH - 80;
      if (nearBottom) {
        triggerLoadMore();
      }
    };

    window.addEventListener('scroll', handleScroll, { passive: true });
    return () => window.removeEventListener('scroll', handleScroll);
  }, [infiniteScroll, onLoadMore, hasMore, loadingMore, triggerLoadMore]);

  const handleAddToCart = useCallback((product: Product, selectedVariants?: ProductVariant[]) => {
    onAddToCart?.(product, selectedVariants);
  }, [onAddToCart]);

  const handleToggleWishlist = useCallback((productId: string) => {
    onToggleWishlist?.(productId);
  }, [onToggleWishlist]);

  const handleQuickView = useCallback((product: Product) => {
    onQuickView?.(product);
  }, [onQuickView]);

  // Render skeleton loading state
  const renderSkeletons = () => {
    return Array.from({ length: skeletonCount }, (_, index) => (
      <div key={`skeleton-${index}`} className="product-grid-skeleton">
        <Skeleton height="400px" className="skeleton-image" />
        <div className="skeleton-content">
          <Skeleton height="16px" width="60%" className="skeleton-brand" />
          <Skeleton height="20px" width="90%" className="skeleton-title" />
          <Skeleton height="16px" width="40%" className="skeleton-rating" />
          <Skeleton height="18px" width="50%" className="skeleton-price" />
        </div>
      </div>
    ));
  };

  // Render empty state
  const renderEmptyState = () => (
    <div className="product-grid-empty">
      <div className="empty-icon">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1">
          <circle cx="9" cy="21" r="1"></circle>
          <circle cx="20" cy="21" r="1"></circle>
          <path d="m1 1 4 4 2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
        </svg>
      </div>
      <h3 className="empty-title">{emptyMessage}</h3>
      <p className="empty-description">
        Try adjusting your search or filter criteria to find what you're looking for.
      </p>
      {emptyAction && (
        <div className="empty-action">
          {emptyAction}
        </div>
      )}
    </div>
  );

  // Render load more indicator
  const renderLoadMoreIndicator = () => {
    if (!infiniteScroll || !hasMore) return null;

    return (
      <div 
        ref={loadMoreRef} 
        className={`load-more-indicator ${loadingMore ? 'loading' : ''}`}
        aria-hidden="true"
      >
        {loadingMore && (
          <div className="load-more-spinner">
            <div className="spinner"></div>
            <span>Loading more products...</span>
          </div>
        )}
      </div>
    );
  };

  const gridClasses = [
    'product-grid',
    `product-grid-${viewMode}`,
    className
  ].filter(Boolean).join(' ');

  // Show initial loading state
  if (loading && products.length === 0) {
    return (
      <div className={gridClasses}>
        <Grid 
          columns={{ xs: 2, md: 3, xl: 3 }} 
          gap="md" 
          className="product-grid-container"
        >
          {renderSkeletons()}
        </Grid>
      </div>
    );
  }

  // Show empty state
  if (!loading && products.length === 0) {
    return (
      <div className={gridClasses}>
        {renderEmptyState()}
      </div>
    );
  }

  // Show products
  return (
    <div className={gridClasses}>
      <Grid 
        columns={{ xs: 2, md: 3, xl: 3 }} 
        gap="md" 
        className="product-grid-container"
        alignItems="stretch"
      >
        {products.map((product) => (
          <div key={product.sku} className="product-grid-item">
            <ProductCard
              product={product}
              onAddToCart={handleAddToCart}
              onToggleWishlist={handleToggleWishlist}
              onQuickView={handleQuickView}
              showQuickActions={false}
            />
          </div>
        ))}
        
        {/* Show loading skeletons for additional products */}
        {loadingMore && renderSkeletons().slice(0, 4)}
      </Grid>
      
      {/* Infinite scroll load more indicator */}
      {renderLoadMoreIndicator()}
    </div>
  );
};

ProductGrid.displayName = 'ProductGrid';

export default ProductGrid;
