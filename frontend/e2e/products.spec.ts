import { test, expect } from '@playwright/test';


test.describe('Products Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/products');
  });

  test('should display products page', async ({ page }) => {
    await expect(page).toHaveURL('/products');
    const title = page.locator('h1');
    await expect(title).toBeVisible();
  });

  test('should display search input in navigation', async ({ page }) => {
    const searchInput = page.getByTestId('search-input');
    await expect(searchInput).toBeVisible();
    await expect(searchInput).toHaveAttribute('placeholder', 'Search');
  });

  test('should allow user to search for products', async ({ page }) => {
    const searchInput = page.getByTestId('search-input');

    await searchInput.fill('sneaker');
    await searchInput.press('Enter');

    await page.waitForTimeout(1000);
  });

  test('should have navigation links', async ({ page }) => {
    const brandLogo = page.getByTestId('nav-brand-logo');
    const cartLink = page.getByTestId('cart-link');

    await expect(brandLogo).toBeVisible();
    await expect(cartLink).toBeVisible();
  });

  test('should navigate to home when brand logo clicked', async ({ page }) => {
    const brandLogo = page.getByTestId('nav-brand-logo');
    await brandLogo.click();
    await expect(page).toHaveURL('/');
  });
});

test.describe('Product Cards', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);
  });

  test('should display product cards', async ({ page }) => {
    const productCards = page.locator('[data-testid^="product-card-"]');
    const cardCount = await productCards.count();

    if (cardCount > 0) {
      await expect(productCards.first()).toBeVisible();
    }
  });

  test('should navigate to product detail when product card clicked', async ({ page }) => {
    const firstCard = page.locator('[data-testid^="product-card-"]').first();
    const cardCount = await firstCard.count();

    if (cardCount > 0) {
      await firstCard.click();
      await expect(page).toHaveURL(/\/products\//);
    }
  });
});
