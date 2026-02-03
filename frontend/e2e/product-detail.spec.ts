import { test, expect } from '@playwright/test';

test.describe('Product Detail Page', () => {
  test('should display product details', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);

    const firstCard = page.locator('[data-testid^="product-card-"]').first();
    if (await firstCard.count() > 0) {
      await firstCard.click();

      const productTitle = page.getByTestId('product-title');
      const productPrice = page.getByTestId('product-price');
      const productImage = page.getByTestId('product-image');

      await expect(productTitle).toBeVisible();
      await expect(productPrice).toBeVisible();
      await expect(productImage).toBeVisible();
    }
  });

  test('should have add to cart button', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);

    const firstCard = page.locator('[data-testid^="product-card-"]').first();
    if (await firstCard.count() > 0) {
      await firstCard.click();

      const addToCartButton = page.getByTestId('add-to-cart-button');
      await expect(addToCartButton).toBeVisible();
    }
  });

  test('should display product price', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);

    const firstCard = page.locator('[data-testid^="product-card-"]').first();
    if (await firstCard.count() > 0) {
      await firstCard.click();

      const productPrice = page.getByTestId('product-price');
      const priceText = await productPrice.textContent();

      expect(priceText).toMatch(/[€$£¥]/);
    }
  });

  test('should navigate back to products list via breadcrumb', async ({ page }) => {
    await page.goto('/products');
    await page.waitForTimeout(2000);

    const firstCard = page.locator('[data-testid^="product-card-"]').first();
    if (await firstCard.count() > 0) {
      await firstCard.click();

      const productsLink = page.getByRole('link', { name: 'Products' });
      await productsLink.click();
      await expect(page).toHaveURL('/products');
    }
  });
});
