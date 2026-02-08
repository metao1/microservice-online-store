/**
 * Navigation Component Tests
 * Updated to match navigation layout.
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import Navigation from './Navigation';
import { CartProvider } from '../context';
import { AuthProvider } from '../context';
import { User } from '../types';
import { apiClient } from '../services/api';

vi.mock('../services/api', () => ({
  apiClient: {
    getCategories: vi.fn(),
    searchProducts: vi.fn(),
  },
}));

const mockUser: User = {
  id: 'test-user',
  name: 'Test User',
  email: 'test@example.com',
  role: 'USER'
};

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
    <AuthProvider initialUser={mockUser}>
      <CartProvider userId={mockUser.id}>
        {children}
      </CartProvider>
    </AuthProvider>
  </BrowserRouter>
);

const renderNavigation = async (props: React.ComponentProps<typeof Navigation> = {}) => {
  render(
    <TestWrapper>
      <Navigation {...props} />
    </TestWrapper>
  );

  await waitFor(() => {
    expect(apiClient.getCategories).toHaveBeenCalled();
  });
};

describe('Navigation Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (apiClient.getCategories as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([
      { category: 'Computers' },
      { category: 'Accessories & Supplies' },
      { category: 'Audio & Video Accessories' },
      { category: 'Car Electronics' },
    ]);
    (apiClient.searchProducts as unknown as ReturnType<typeof vi.fn>).mockResolvedValue([]);
  });

  describe('Basic Rendering', () => {
    it('renders the navigation component', async () => {
      await renderNavigation();

      expect(screen.getByTestId('navbar')).toBeInTheDocument();
    });

    it('displays the brand logo', async () => {
      await renderNavigation();

      const brandLogo = screen.getByTestId('nav-brand-logo');
      expect(brandLogo).toBeInTheDocument();
      expect(brandLogo).toHaveTextContent('ModernStore');
    });

    it('displays the utility bar with promotional message', async () => {
      await renderNavigation();

      expect(screen.getByText(/Free standard delivery over €29,90 & free returns/)).toBeInTheDocument();
    });
  });

  describe('Navigation Links', () => {
    it('renders secondary navigation links', async () => {
      await renderNavigation();

      // Main categories are rendered in the secondary row
      expect(await screen.findByText('Computers & Accessories')).toBeInTheDocument();
      expect(screen.getByText('Audio & Video')).toBeInTheDocument();
      expect(screen.getByText('Car Electronics')).toBeInTheDocument();
      expect(screen.getByText('GPS & Navigation')).toBeInTheDocument();
    });

    it('calls onCategorySelect when secondary category is clicked', async () => {
      const mockOnCategorySelect = vi.fn();

      await renderNavigation({ onCategorySelect: mockOnCategorySelect });

      // Hover a main category and click a subcategory from the mega panel
      const mainCategory = await screen.findByText('Computers & Accessories');
      fireEvent.mouseEnter(mainCategory);
      const subcategory = await screen.findByText('Computers');
      fireEvent.click(subcategory);
      expect(mockOnCategorySelect).toHaveBeenCalledWith('Computers');
    });
  });

  describe('Search Functionality', () => {
    it('renders the search input', async () => {
      await renderNavigation();

      const searchInput = screen.getByTestId('search-input');
      expect(searchInput).toBeInTheDocument();
      expect(searchInput).toHaveAttribute('placeholder', 'Search');
    });

    it('shows search suggestions container when typing', async () => {
      await renderNavigation();

      const searchInput = screen.getByTestId('search-input');

      fireEvent.change(searchInput, { target: { value: 'Nike' } });
      fireEvent.focus(searchInput);

      await waitFor(() => {
        expect(screen.getByTestId('search-suggestions')).toBeInTheDocument();
      });
    });

    it('calls onSearch when search is submitted', async () => {
      const mockOnSearch = vi.fn();

      await renderNavigation({ onSearch: mockOnSearch });

      const searchInput = screen.getByTestId('search-input');

      fireEvent.change(searchInput, { target: { value: 'test query' } });
      fireEvent.submit(searchInput.closest('form')!);

      await waitFor(() => {
        expect(mockOnSearch).toHaveBeenCalledWith('test query');
      });
    });
  });

  describe('Cart Functionality', () => {
    it('renders the cart link', async () => {
      await renderNavigation();

      expect(screen.getByTestId('cart-link')).toBeInTheDocument();
    });
  });

  describe('Mobile Navigation', () => {
    it('renders mobile menu toggle button', async () => {
      await renderNavigation();

      const mobileToggle = screen.getByTestId('mobile-menu-toggle');
      expect(mobileToggle).toBeInTheDocument();
      expect(mobileToggle).toHaveAttribute('aria-label', 'Toggle mobile menu');
    });

    it('toggles mobile menu when hamburger button is clicked', async () => {
      await renderNavigation();

      const mobileToggle = screen.getByTestId('mobile-menu-toggle');
      expect(screen.queryByTestId('mobile-menu')).not.toBeInTheDocument();

      fireEvent.click(mobileToggle);
      expect(screen.getByTestId('mobile-menu')).toBeInTheDocument();

      fireEvent.click(mobileToggle);
      expect(screen.queryByTestId('mobile-menu')).not.toBeInTheDocument();
    });

    it('closes mobile menu when backdrop is clicked', async () => {
      await renderNavigation();

      const mobileToggle = screen.getByTestId('mobile-menu-toggle');
      fireEvent.click(mobileToggle);
      expect(screen.getByTestId('mobile-menu')).toBeInTheDocument();

      const backdrop = document.querySelector('.mobile-menu-backdrop');
      expect(backdrop).toBeInTheDocument();
      fireEvent.click(backdrop!);
      expect(screen.queryByTestId('mobile-menu')).not.toBeInTheDocument();
    });
  });
});
