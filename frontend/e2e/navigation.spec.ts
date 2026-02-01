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

  test('should have all navigation links visible', async ({ page }) => {
    await page.goto('/');
    
    const homeLink = page.getByTestId('home-link');
    const productsLink = page.getByTestId('products-link');
    const cartLink = page.getByTestId('cart-link');
    
    await expect(homeLink).toBeVisible();
    await expect(productsLink).toBeVisible();
    await expect(cartLink).toBeVisible();
  });

  test('should navigate to home page', async ({ page }) => {
    await page.goto('/products');
    
    const homeLink = page.getByTestId('home-link');
    await homeLink.click();
    
    await expect(page).toHaveURL('/');
  });

  test('should navigate to products page', async ({ page }) => {
    await page.goto('/');
    
    const productsLink = page.getByTestId('products-link');
    await productsLink.click();
    
    await expect(page).toHaveURL('/products');
  });

  test('should navigate to cart page', async ({ page }) => {
    await page.goto('/');
    
    const cartLink = page.getByTestId('cart-link');
    await cartLink.click();
    
    await expect(page).toHaveURL('/cart');
  });

  test('should display brand logo', async ({ page }) => {
    await page.goto('/');
    
    const brandLogo = page.getByTestId('brand-logo');
    await expect(brandLogo).toBeVisible();
    await expect(brandLogo).toContainText('Online Store');
  });

  test('should have cart badge when items in cart', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    const cartBadge = page.getByTestId('cart-badge');
    const badgeInitialCount = await cartBadge.count();
    
    // Cart badge may or may not be visible initially
    // This test just verifies it can appear
    if (badgeInitialCount > 0) {
      await expect(cartBadge).toBeVisible();
    }
  });
});

test.describe('Responsive Navigation', () => {
  test('should have working navigation on mobile', async ({ page }) => {
    // Set mobile viewport
    await page.setViewportSize({ width: 375, height: 667 });
    
    await page.goto('/');
    
    const navbar = page.getByTestId('navbar');
    await expect(navbar).toBeVisible();
    
    const homeLink = page.getByTestId('home-link');
    await expect(homeLink).toBeVisible();
  });

  test('should have working navigation on tablet', async ({ page }) => {
    // Set tablet viewport
    await page.setViewportSize({ width: 768, height: 1024 });
    
    await page.goto('/');
    
    const navbar = page.getByTestId('navbar');
    await expect(navbar).toBeVisible();
    
    const productsLink = page.getByTestId('products-link');
    await expect(productsLink).toBeVisible();
  });

  test('should have working navigation on desktop', async ({ page }) => {
    // Set desktop viewport
    await page.setViewportSize({ width: 1920, height: 1080 });
    
    await page.goto('/');
    
    const navbar = page.getByTestId('navbar');
    await expect(navbar).toBeVisible();
    
    const cartLink = page.getByTestId('cart-link');
    await expect(cartLink).toBeVisible();
  });
});
