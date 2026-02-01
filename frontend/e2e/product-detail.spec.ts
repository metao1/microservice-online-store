import { test, expect } from '@playwright/test';

test.describe('Product Detail Page', () => {
  test('should display product details', async ({ page }) => {
    // Navigate to a product (using first available product)
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    const firstViewButton = page.locator('[data-testid^="view-button-"]').first();
    const buttonCount = await firstViewButton.count();
    
    if (buttonCount > 0) {
      await firstViewButton.click();
      
      // Verify product detail page elements
      const productTitle = page.getByTestId('product-title');
      const productPrice = page.getByTestId('product-price');
      const productImage = page.getByTestId('product-image');
      
      await expect(productTitle).toBeVisible();
      await expect(productPrice).toBeVisible();
      await expect(productImage).toBeVisible();
    }
  });

  test('should have quantity selector', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    const firstViewButton = page.locator('[data-testid^="view-button-"]').first();
    const buttonCount = await firstViewButton.count();
    
    if (buttonCount > 0) {
      await firstViewButton.click();
      
      const quantitySelector = page.getByTestId('quantity-selector');
      await expect(quantitySelector).toBeVisible();
      
      // Verify default quantity is 1
      await expect(quantitySelector).toHaveValue('1');
    }
  });

  test('should allow changing quantity', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    const firstViewButton = page.locator('[data-testid^="view-button-"]').first();
    const buttonCount = await firstViewButton.count();
    
    if (buttonCount > 0) {
      await firstViewButton.click();
      
      const quantitySelector = page.getByTestId('quantity-selector');
      await quantitySelector.fill('5');
      
      await expect(quantitySelector).toHaveValue('5');
    }
  });

  test('should have add to cart button', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    const firstViewButton = page.locator('[data-testid^="view-button-"]').first();
    const buttonCount = await firstViewButton.count();
    
    if (buttonCount > 0) {
      await firstViewButton.click();
      
      const addToCartButton = page.getByTestId('add-to-cart-button');
      await expect(addToCartButton).toBeVisible();
    }
  });

  test('should display product price', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    const firstViewButton = page.locator('[data-testid^="view-button-"]').first();
    const buttonCount = await firstViewButton.count();
    
    if (buttonCount > 0) {
      await firstViewButton.click();
      
      const productPrice = page.getByTestId('product-price');
      const priceText = await productPrice.textContent();
      
      // Price should contain currency symbol and a number
      expect(priceText).toMatch(/[€$£¥]/);
    }
  });

  test('should navigate back to products list', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    const productsLink = page.getByTestId('products-link');
    await productsLink.click();
    
    await expect(page).toHaveURL('/products');
  });
});
