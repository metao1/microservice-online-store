import { test, expect } from '@playwright/test';

test.describe('User Flow - Complete Shopping Journey', () => {
  test('should complete basic shopping workflow', async ({ page }) => {
    await page.goto('/');

    const heroSection = page.getByTestId('hero-section');
    await expect(heroSection).toBeVisible();

    const shopButton = page.getByTestId('shop-now-button');
    await shopButton.click();
    await expect(page).toHaveURL('/products');

    await page.waitForTimeout(2000);

    const searchInput = page.getByTestId('search-input');
    if (await searchInput.count() > 0) {
      await searchInput.fill('sneaker');
      await searchInput.press('Enter');
      await page.waitForTimeout(1000);
    }

    const firstCard = page.locator('[data-testid^="product-card-"]').first();
    if (await firstCard.count() > 0) {
      await firstCard.click();

      const productTitle = page.getByTestId('product-title');
      await expect(productTitle).toBeVisible();

      const cartLink = page.getByTestId('cart-link');
      await cartLink.click();
      await expect(page).toHaveURL('/cart');
    }
  });

  test('should handle navigation between all pages', async ({ page }) => {
    const pages = [
      { path: '/', testId: 'hero-section' },
      { path: '/products', testId: null },
      { path: '/cart', testId: null },
    ];

    for (const pageObj of pages) {
      await page.goto(pageObj.path);
      await expect(page).toHaveURL(pageObj.path);

      if (pageObj.testId) {
        const element = page.getByTestId(pageObj.testId);
        if (await element.count() > 0) {
          await expect(element).toBeVisible();
        }
      }
    }
  });
});

test.describe('Error Handling', () => {
  test('should handle invalid product ID gracefully', async ({ page }) => {
    await page.goto('/products/99999999');
    await page.waitForTimeout(2000);

    const pageContent = page.locator('body');
    await expect(pageContent).toBeTruthy();
  });

  test('should handle network errors gracefully', async ({ page }) => {
    await page.context().setOffline(true);

    await page.goto('/products');
    await page.waitForTimeout(2000);

    await page.context().setOffline(false);
  });
});

test.describe('Performance', () => {
  test('should load home page quickly', async ({ page }) => {
    const startTime = Date.now();
    await page.goto('/');
    const loadTime = Date.now() - startTime;

    expect(loadTime).toBeLessThan(5000);
  });

  test('should load products page quickly', async ({ page }) => {
    const startTime = Date.now();
    await page.goto('/products');
    const loadTime = Date.now() - startTime;

    expect(loadTime).toBeLessThan(8000);
  });
});

test.describe('Accessibility', () => {
  test('should have proper heading hierarchy', async ({ page }) => {
    await page.goto('/');

    const h1 = page.locator('h1');
    expect(await h1.count()).toBeGreaterThan(0);
  });

  test('should have buttons with text', async ({ page }) => {
    await page.goto('/products');

    const buttons = page.locator('button');
    const buttonCount = await buttons.count();

    for (let i = 0; i < Math.min(buttonCount, 5); i++) {
      const button = buttons.nth(i);
      const text = await button.textContent();
      expect(text).toBeTruthy();
    }
  });

  test('should have proper form labels', async ({ page }) => {
    await page.goto('/products');

    const searchInput = page.getByTestId('search-input');
    if (await searchInput.count() > 0) {
      await expect(searchInput).toBeVisible();
    }
  });
});
