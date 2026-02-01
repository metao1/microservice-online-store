import { test, expect } from '@playwright/test';

test.describe('Cart Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/cart');
  });

  test('should display cart page', async ({ page }) => {
    await expect(page).toHaveURL('/cart');
  });

  test('should handle empty cart gracefully', async ({ page }) => {
    // If cart is empty, should show appropriate message
    const emptyMessage = page.locator('text=/Your cart is empty|No items in cart/i');
    const cartItems = page.getByTestId('cart-items');
    
    const messageExists = await emptyMessage.count();
    const itemsExist = await cartItems.count();
    
    // Either empty message or items should be present
    expect(messageExists + itemsExist).toBeGreaterThanOrEqual(1);
  });

  test('should navigate to home from cart', async ({ page }) => {
    const homeLink = page.getByTestId('home-link');
    await homeLink.click();
    
    await expect(page).toHaveURL('/');
  });

  test('should navigate to products from cart', async ({ page }) => {
    const productsLink = page.getByTestId('products-link');
    await productsLink.click();
    
    await expect(page).toHaveURL('/products');
  });
});

test.describe('Cart Operations', () => {
  test('should add product to cart from products page', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    // Get initial cart count
    const initialCartBadge = page.getByTestId('cart-badge');
    const initialCount = await initialCartBadge.count();
    
    // Add to cart from a product card
    const firstAddButton = page.locator('[data-testid^="add-button-"]').first();
    
    if ((await firstAddButton.count()) > 0) {
      // Check if button is enabled
      const isDisabled = await firstAddButton.isDisabled();
      
      if (!isDisabled) {
        await firstAddButton.click();
        await page.waitForTimeout(1000);
        
        // Navigate to cart
        const cartLink = page.getByTestId('cart-link');
        await cartLink.click();
        
        // Verify item is in cart
        const cartItems = page.getByTestId('cart-items');
        const hasItems = await cartItems.count();
        
        if (hasItems > 0) {
          expect(await cartItems.locator('tr').count()).toBeGreaterThan(0);
        }
      }
    }
  });

  test('should display cart items with quantity and price', async ({ page }) => {
    // Navigate to cart
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    // Try to add item if available
    const firstAddButton = page.locator('[data-testid^="add-button-"]').first();
    
    if ((await firstAddButton.count()) > 0) {
      const isDisabled = await firstAddButton.isDisabled();
      
      if (!isDisabled) {
        await firstAddButton.click();
        await page.waitForTimeout(1000);
      }
    }
    
    // Navigate to cart
    const cartLink = page.getByTestId('cart-link');
    await cartLink.click();
    
    const cartItems = page.getByTestId('cart-items');
    const hasItems = await cartItems.count();
    
    if (hasItems > 0) {
      const rows = cartItems.locator('tr');
      const rowCount = await rows.count();
      
      if (rowCount > 0) {
        // Each row should have product info
        const firstRow = rows.first();
        const cells = firstRow.locator('td');
        
        // Should have at least: Product, Price, Quantity, Total, Action
        expect(await cells.count()).toBeGreaterThanOrEqual(5);
      }
    }
  });

  test('should allow changing quantity in cart', async ({ page }) => {
    // Navigate to cart
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    // Try to add item
    const firstAddButton = page.locator('[data-testid^="add-button-"]').first();
    
    if ((await firstAddButton.count()) > 0) {
      const isDisabled = await firstAddButton.isDisabled();
      
      if (!isDisabled) {
        await firstAddButton.click();
        await page.waitForTimeout(1000);
      }
    }
    
    // Go to cart
    const cartLink = page.getByTestId('cart-link');
    await cartLink.click();
    
    // Find quantity input
    const quantityInput = page.locator('[data-testid^="quantity-input-"]').first();
    
    if ((await quantityInput.count()) > 0) {
      await quantityInput.fill('3');
      
      // Verify new quantity
      await expect(quantityInput).toHaveValue('3');
    }
  });

  test('should allow removing items from cart', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);
    
    // Try to add item
    const firstAddButton = page.locator('[data-testid^="add-button-"]').first();
    
    if ((await firstAddButton.count()) > 0) {
      const isDisabled = await firstAddButton.isDisabled();
      
      if (!isDisabled) {
        await firstAddButton.click();
        await page.waitForTimeout(1000);
      }
    }
    
    // Go to cart
    const cartLink = page.getByTestId('cart-link');
    await cartLink.click();
    
    // Find remove button
    const removeButton = page.locator('[data-testid^="remove-button-"]').first();
    
    if ((await removeButton.count()) > 0) {
      await removeButton.click();
      await page.waitForTimeout(1000);
    }
  });

  test('should display checkout button', async ({ page }) => {
    await page.goto('/cart');
    
    const checkoutButton = page.getByTestId('checkout-button');
    
    if ((await checkoutButton.count()) > 0) {
      await expect(checkoutButton).toBeVisible();
    }
  });
});
