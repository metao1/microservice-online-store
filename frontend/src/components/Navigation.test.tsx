/**
 * Navigation Component Tests
 * Updated to match Zalando-like navigation layout.
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import Navigation from './Navigation';
import { CartProvider } from '../context/CartContext';
import { AuthProvider } from '../context/AuthContext';
import { User } from '../types';

const mockUser: User = {
  id: 'test-user',
  name: 'Test User',
  email: 'test@example.com',
  role: 'USER'
};

const TestWrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <BrowserRouter>
    <AuthProvider initialUser={mockUser}>
      <CartProvider userId={mockUser.id}>
        {children}
      </CartProvider>
    </AuthProvider>
  </BrowserRouter>
);

describe('Navigation Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Basic Rendering', () => {
    it('renders the navigation component', () => {
      render(
        <TestWrapper>
          <Navigation />
        </TestWrapper>
      );

      expect(screen.getByTestId('navbar')).toBeInTheDocument();
    });

    it('displays the brand logo', () => {
      render(
        <TestWrapper>
          <Navigation />
        </TestWrapper>
      );

      const brandLogo = screen.getByTestId('nav-brand-logo');
      expect(brandLogo).toBeInTheDocument();
      expect(brandLogo).toHaveTextContent('ModernStore');
    });

    it('displays the utility bar with promotional message', () => {
      render(
        <TestWrapper>
          <Navigation />
        </TestWrapper>
      );

      expect(screen.getByText(/Free standard delivery over €29,90 & free returns/)).toBeInTheDocument();
    });
  });

  describe('Navigation Links', () => {
    it('renders all primary navigation links', () => {
      render(
        <TestWrapper>
          <Navigation />
        </TestWrapper>
      );

      expect(screen.getByTestId('category-women')).toBeInTheDocument();
      expect(screen.getByTestId('category-men')).toBeInTheDocument();
      expect(screen.getByTestId('category-kids')).toBeInTheDocument();
    });

    it('calls onCategorySelect when category is clicked', () => {
      const mockOnCategorySelect = vi.fn();

      render(
        <TestWrapper>
          <Navigation onCategorySelect={mockOnCategorySelect} />
        </TestWrapper>
      );

      fireEvent.click(screen.getByTestId('category-women'));
      expect(mockOnCategorySelect).toHaveBeenCalledWith('women');
    });
  });

  describe('Search Functionality', () => {
    it('renders the search input', () => {
      render(
        <TestWrapper>
          <Navigation />
        </TestWrapper>
      );

      const searchInput = screen.getByTestId('search-input');
      expect(searchInput).toBeInTheDocument();
      expect(searchInput).toHaveAttribute('placeholder', 'Search');
    });

    it('shows search suggestions container when typing', async () => {
      render(
        <TestWrapper>
          <Navigation />
        </TestWrapper>
      );

      const searchInput = screen.getByTestId('search-input');

      fireEvent.change(searchInput, { target: { value: 'Nike' } });
      fireEvent.focus(searchInput);

      await waitFor(() => {
        expect(screen.getByTestId('search-suggestions')).toBeInTheDocument();
      });
    });

    it('calls onSearch when search is submitted', () => {
      const mockOnSearch = vi.fn();

      render(
        <TestWrapper>
          <Navigation onSearch={mockOnSearch} />
        </TestWrapper>
      );

      const searchInput = screen.getByTestId('search-input');

      fireEvent.change(searchInput, { target: { value: 'test query' } });
      fireEvent.submit(searchInput.closest('form')!);

      expect(mockOnSearch).toHaveBeenCalledWith('test query');
    });
  });

  describe('Cart Functionality', () => {
    it('renders the cart link', () => {
      render(
        <TestWrapper>
          <Navigation />
        </TestWrapper>
      );

      expect(screen.getByTestId('cart-link')).toBeInTheDocument();
    });
  });

  describe('Mobile Navigation', () => {
    it('renders mobile menu toggle button', () => {
      render(
        <TestWrapper>
          <Navigation />
        </TestWrapper>
      );

      const mobileToggle = screen.getByTestId('mobile-menu-toggle');
      expect(mobileToggle).toBeInTheDocument();
      expect(mobileToggle).toHaveAttribute('aria-label', 'Toggle mobile menu');
    });

    it('toggles mobile menu when hamburger button is clicked', () => {
      render(
        <TestWrapper>
          <Navigation />
        </TestWrapper>
      );

      const mobileToggle = screen.getByTestId('mobile-menu-toggle');
      expect(screen.queryByTestId('mobile-menu')).not.toBeInTheDocument();

      fireEvent.click(mobileToggle);
      expect(screen.getByTestId('mobile-menu')).toBeInTheDocument();

      fireEvent.click(mobileToggle);
      expect(screen.queryByTestId('mobile-menu')).not.toBeInTheDocument();
    });

    it('closes mobile menu when backdrop is clicked', () => {
      render(
        <TestWrapper>
          <Navigation />
        </TestWrapper>
      );

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
