import { render } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { createMoney, Product } from '@types';
import ProductGrid from './ProductGrid';

vi.mock('./ProductCard', () => ({
  default: ({ product }: { product: Product }) => <article>{product.title}</article>,
}));

const product: Product = {
  sku: 'book-1',
  title: 'Book one',
  price: createMoney(10, 'EUR'),
  imageUrl: '/book.png',
  description: 'Book one',
  inStock: true,
};

describe('ProductGrid infinite scrolling', () => {
  let observerOptions: IntersectionObserverInit | undefined;

  beforeEach(() => {
    observerOptions = undefined;
    vi.stubGlobal('IntersectionObserver', class {
      constructor(_callback: IntersectionObserverCallback, options?: IntersectionObserverInit) {
        observerOptions = options;
      }

      observe() {}
      disconnect() {}
    });
  });

  afterEach(() => vi.unstubAllGlobals());

  it('starts prefetching 800 pixels before the load-more sentinel enters view', () => {
    render(
      <ProductGrid
        products={[product]}
        loading={false}
        hasMore
        onLoadMore={vi.fn()}
      />,
    );

    expect(observerOptions?.rootMargin).toBe('0px 0px 800px 0px');
    const grid = document.querySelector('.product-grid-container');
    expect(grid).toHaveClass('grid-cols-xs-2', 'grid-cols-md-4', 'grid-cols-xl-5');
  });
});
