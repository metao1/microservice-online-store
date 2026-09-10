import { act, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, useNavigate } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createMoney, Product } from '@types';
import ProductsPage from './ProductsPage';

type Deferred<T> = {
  promise: Promise<T>;
  resolve: (value: T) => void;
};

const api = vi.hoisted(() => ({
  getCategories: vi.fn(),
  getProducts: vi.fn(),
}));

vi.mock('../services/api', () => ({ apiClient: api }));

vi.mock('../components/ProductGrid', () => ({
  default: ({
    products,
    hasMore,
    onLoadMore,
  }: {
    products: Product[];
    hasMore: boolean;
    onLoadMore: () => void;
  }) => (
    <div data-testid="product-list">
      {products.map((product) => <span key={product.sku}>{product.title}</span>)}
      {hasMore && <button type="button" onClick={onLoadMore}>Load more</button>}
    </div>
  ),
}));

const deferred = <T,>(): Deferred<T> => {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((complete) => {
    resolve = complete;
  });
  return { promise, resolve };
};

const product = (sku: string, title: string): Product => ({
  sku,
  title,
  price: createMoney(10, 'EUR'),
  imageUrl: '/product.png',
  description: title,
  inStock: true,
});

function ProductsRoute() {
  const navigate = useNavigate();

  return (
    <>
      <button
        type="button"
        aria-label="Navigate to electronics"
        onClick={() => navigate('/products?category=electronics')}
      >
        Electronics
      </button>
      <ProductsPage />
    </>
  );
}

describe('ProductsPage pagination requests', () => {
  beforeEach(() => {
    api.getCategories.mockReset().mockResolvedValue([]);
    api.getProducts.mockReset();
    window.sessionStorage.clear();
  });

  it('keeps the latest category visible when an older request completes last', async () => {
    const books = deferred<Product[]>();
    const electronics = deferred<Product[]>();

    api.getProducts.mockImplementation((category: string) => (
      category === 'electronics' ? electronics.promise : books.promise
    ));

    render(
      <MemoryRouter initialEntries={['/products?category=books']}>
        <ProductsRoute />
      </MemoryRouter>,
    );

    await waitFor(() => expect(api.getProducts).toHaveBeenCalledWith('books', 16, 0));
    fireEvent.click(screen.getByRole('button', { name: 'Navigate to electronics' }));
    await waitFor(() => expect(api.getProducts).toHaveBeenCalledWith('electronics', 16, 0));

    await act(async () => electronics.resolve([product('electronics-1', 'Latest product')]));
    expect(await screen.findByText('Latest product')).toBeInTheDocument();

    await act(async () => books.resolve([product('books-1', 'Stale product')]));

    expect(screen.getByText('Latest product')).toBeInTheDocument();
    expect(screen.queryByText('Stale product')).not.toBeInTheDocument();
  });

  it('appends later pages without moving products already on screen', async () => {
    const nextPage = deferred<Product[]>();
    const firstPage = Array.from({ length: 16 }, (_, index) => (
      product(`history-${index}`, `History volume ${index}`)
    ));
    window.sessionStorage.setItem('products-filters:/products', JSON.stringify({
      sortBy: 'name',
      sortOrder: 'asc',
    }));

    api.getProducts.mockImplementation((_category: string, _limit: number, offset: number) => (
      offset === 0 ? Promise.resolve(firstPage) : nextPage.promise
    ));

    render(
      <MemoryRouter initialEntries={['/products?category=books']}>
        <ProductsPage />
      </MemoryRouter>,
    );

    expect(await screen.findByText('History volume 0')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Load more' }));
    await waitFor(() => expect(api.getProducts).toHaveBeenCalledWith('books', 16, 16));

    await act(async () => nextPage.resolve([product('software-1', 'A Software architecture')]));

    expect(screen.getByText('History volume 0')).toBeInTheDocument();
    expect(screen.getByText('A Software architecture')).toBeInTheDocument();
    expect(screen.getByTestId('product-list').querySelector('span')).toHaveTextContent('History volume 0');
  });
});
