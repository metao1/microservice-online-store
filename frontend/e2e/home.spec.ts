import { test, expect } from '@playwright/test';

test.describe('Home Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should display hero section', async ({ page }) => {
    const heroSection = page.getByTestId('hero-section');
    await expect(heroSection).toBeVisible();
  });

  test('should have shop collection button', async ({ page }) => {
    const shopButton = page.getByTestId('shop-now-button');
    await expect(shopButton).toBeVisible();
    await expect(shopButton).toContainText('Shop Collection');
  });

  test('should have navigation bar', async ({ page }) => {
    const navbar = page.getByTestId('navbar');
    await expect(navbar).toBeVisible();
  });

  test('should navigate to products page when shop button is clicked', async ({ page }) => {
    const shopButton = page.getByTestId('shop-now-button');
    await shopButton.click();
    await expect(page).toHaveURL('/products');
  });

  test('should have footer with company info', async ({ page }) => {
    const footer = page.locator('footer');
    await expect(footer).toBeVisible();
  });
});
