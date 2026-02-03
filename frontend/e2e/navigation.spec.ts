import { test, expect } from '@playwright/test';

test.describe('Navigation', () => {
  test('should display navigation bar on all pages', async ({ page }) => {
    const pages = ['/', '/products', '/cart'];

    for (const path of pages) {
      await page.goto(path);
      const navbar = page.getByTestId('navbar');
      await expect(navbar).toBeVisible();
    }
  });

  test('should show primary navigation links and search', async ({ page }) => {
    await page.goto('/');

    await expect(page.getByTestId('category-women')).toBeVisible();
    await expect(page.getByTestId('category-men')).toBeVisible();
    await expect(page.getByTestId('category-kids')).toBeVisible();
    await expect(page.getByTestId('search-input')).toBeVisible();
  });

  test('should navigate to home via brand logo', async ({ page }) => {
    await page.goto('/products');

    const brandLogo = page.getByTestId('nav-brand-logo');
    await expect(brandLogo).toBeVisible();
    await brandLogo.click();
    await expect(page).toHaveURL('/');
  });

  test('should navigate to cart page', async ({ page }) => {
    await page.goto('/');

    const cartLink = page.getByTestId('cart-link');
    await cartLink.click();
    await expect(page).toHaveURL('/cart');
  });
});

test.describe('Responsive Navigation', () => {
  test('should show mobile menu toggle on small screens', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/');

    const mobileToggle = page.getByTestId('mobile-menu-toggle');
    await expect(mobileToggle).toBeVisible();
  });

  test('should show navigation on desktop', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('/');

    await expect(page.getByTestId('navbar')).toBeVisible();
    await expect(page.getByTestId('cart-link')).toBeVisible();
  });
});
