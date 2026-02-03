import { test, expect } from '@playwright/test';

test.describe('Cart Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/cart');
  });

  test('should display cart page', async ({ page }) => {
    await expect(page).toHaveURL('/cart');
  });

  test('should handle empty cart gracefully', async ({ page }) => {
    const emptyMessage = page.locator('text=/Your cart is empty|No items in cart/i');
    const cartItems = page.getByTestId('cart-items');

    const messageExists = await emptyMessage.count();
    const itemsExist = await cartItems.count();

    expect(messageExists + itemsExist).toBeGreaterThanOrEqual(1);
  });

  test('should navigate to home from cart using brand logo', async ({ page }) => {
    const brandLogo = page.getByTestId('nav-brand-logo');
    await brandLogo.click();

    await expect(page).toHaveURL('/');
  });
});
