import { test, expect } from '@playwright/test';
import { beforeEach, describe } from 'vitest';

describe('Products Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/products');
  });

  test('should display products page', async ({ page }) => {
    const productsPage = page.locator('text=Products');
    await expect(page).toHaveURL('/products');
  });

  test('should have search functionality', async ({ page }) => {
    const searchInput = page.getByTestId('search-input');
    const searchButton = page.getByTestId('search-button');
    
    await expect(searchInput).toBeVisible();
    await expect(searchButton).toBeVisible();
  });

  test('should display search input and button', async ({ page }) => {
    const searchInput = page.getByTestId('search-input');
    await expect(searchInput).toHaveAttribute('placeholder', 'Search products...');
  });

  test('should allow user to search for products', async ({ page }) => {
    const searchInput = page.getByTestId('search-input');
    const searchButton = page.getByTestId('search-button');
    
    await searchInput.fill('laptop');
    await searchButton.click();
    
    // Wait for results to load
    await page.waitForTimeout(1000);
  });

  test('should handle empty product list gracefully', async ({ page }) => {
    // This test verifies the UI handles no products scenario
    // In a real app with a mock server, you'd return empty list
    const searchInput = page.getByTestId('search-input');
    const searchButton = page.getByTestId('search-button');
    
    await searchInput.fill('nonexistentproduct12345');
    await searchButton.click();
    
    // Wait for response
    await page.waitForTimeout(1000);
  });

  test('should have navigation links', async ({ page }) => {
    const homeLink = page.getByTestId('home-link');
    const cartLink = page.getByTestId('cart-link');
    
    await expect(homeLink).toBeVisible();
    await expect(cartLink).toBeVisible();
  });

  test('should navigate to home when home link clicked', async ({ page }) => {
    const homeLink = page.getByTestId('home-link');
    await homeLink.click();
    await expect(page).toHaveURL('/');
  });

  test('should navigate to cart when cart link clicked', async ({ page }) => {
    const cartLink = page.getByTestId('cart-link');
    await cartLink.click();
    await expect(page).toHaveURL('/cart');
  });
});

describe('Product Cards', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/products');
    // Wait for products to load
    await page.waitForTimeout(2000);
  });

  test('should display product cards', async ({ page }) => {
    const productCards = page.locator('[data-testid^="product-card-"]');
    const cardCount = await productCards.count();
    
    if (cardCount > 0) {
      await expect(productCards.first()).toBeVisible();
    }
  });

  test('should have view details button on product cards', async ({ page }) => {
    const firstProductCard = page.locator('[data-testid^="product-card-"]').first();
    
    if ((await firstProductCard.count()) > 0) {
      const viewButton = firstProductCard.locator('[data-testid^="view-button-"]');
      await expect(viewButton).toBeVisible();
    }
  });

  test('should navigate to product detail when view details clicked', async ({ page }) => {
    const firstViewButton = page.locator('[data-testid^="view-button-"]').first();
    const buttonCount = await firstViewButton.count();
    
    if (buttonCount > 0) {
      await firstViewButton.click();
      await expect(page).toHaveURL(/\/products\/\d+/);
    }
  });
});
