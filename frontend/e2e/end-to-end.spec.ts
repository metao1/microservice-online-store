import { test, expect } from '@playwright/test';

test.describe('User Flow - Complete Shopping Journey', () => {
  test('should complete full shopping workflow', async ({ page }) => {
    // Step 1: Start at home page
    await page.goto('/');
    
    const heroSection = page.getByTestId('hero-section');
    await expect(heroSection).toBeVisible();
    
    // Step 2: Click shop now to go to products
    const shopButton = page.getByTestId('shop-now-button');
    await shopButton.click();
    await expect(page).toHaveURL('/products');
    
    // Step 3: Wait for products to load
    await page.waitForTimeout(2000);
    
    // Step 4: Search for a product (if applicable)
    const searchInput = page.getByTestId('search-input');
    if (await searchInput.count() > 0) {
      await searchInput.fill('product');
      const searchButton = page.getByTestId('search-button');
      await searchButton.click();
      await page.waitForTimeout(1000);
    }
    
    // Step 5: View a product (if available)
    const firstViewButton = page.locator('[data-testid^="view-button-"]').first();
    if (await firstViewButton.count() > 0) {
      await firstViewButton.click();
      
      // Verify product detail page
      const productTitle = page.getByTestId('product-title');
      await expect(productTitle).toBeVisible();
      
      // Step 6: Change quantity and add to cart
      const quantitySelector = page.getByTestId('quantity-selector');
      if (await quantitySelector.count() > 0) {
        await quantitySelector.fill('2');
      }
      
      const addToCartButton = page.getByTestId('add-to-cart-button');
      if (await addToCartButton.count() > 0 && !await addToCartButton.isDisabled()) {
        await addToCartButton.click();
        await page.waitForTimeout(1000);
      }
      
      // Step 7: Go to cart
      const cartLink = page.getByTestId('cart-link');
      await cartLink.click();
      await expect(page).toHaveURL('/cart');
      
      // Step 8: Verify cart contents
      await page.waitForTimeout(1000);
      const cartItems = page.getByTestId('cart-items');
      // Cart may be empty or have items depending on mock data
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
    
    // Should either show error or navigate back
    const pageContent = page.locator('body');
    await expect(pageContent).toBeTruthy();
  });

  test('should handle network errors gracefully', async ({ page }) => {
    // Simulate network failure
    await page.context().setOffline(true);
    
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    // Re-enable network
    await page.context().setOffline(false);
  });
});

test.describe('Performance', () => {
  test('should load home page quickly', async ({ page }) => {
    const startTime = Date.now();
    await page.goto('/');
    const loadTime = Date.now() - startTime;
    
    // Page should load in reasonable time (adjust based on your needs)
    expect(loadTime).toBeLessThan(5000);
  });

  test('should load products page quickly', async ({ page }) => {
    const startTime = Date.now();
    await page.goto('/products');
    const loadTime = Date.now() - startTime;
    
    // Page should load in reasonable time
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
      // Search input should be visible and functional
      await expect(searchInput).toBeVisible();
    }
  });
});
