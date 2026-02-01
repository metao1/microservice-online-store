import { test, expect } from '@playwright/test';

test.describe('Home Page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should display hero section', async ({ page }) => {
    const heroSection = page.getByTestId('hero-section');
    await expect(heroSection).toBeVisible();
  });

  test('should have shop now button', async ({ page }) => {
    const shopButton = page.getByTestId('shop-now-button');
    await expect(shopButton).toBeVisible();
    await expect(shopButton).toContainText('Shop Now');
  });

  test('should have navigation bar', async ({ page }) => {
    const navbar = page.getByTestId('navbar');
    await expect(navbar).toBeVisible();
  });

  test('should have home link in navigation', async ({ page }) => {
    const homeLink = page.getByTestId('home-link');
    await expect(homeLink).toBeVisible();
  });

  test('should navigate to products page when shop now is clicked', async ({ page }) => {
    const shopButton = page.getByTestId('shop-now-button');
    await shopButton.click();
    await expect(page).toHaveURL('/products');
  });

  test('should navigate to products page via products link', async ({ page }) => {
    const productsLink = page.getByTestId('products-link');
    await productsLink.click();
    await expect(page).toHaveURL('/products');
  });

  test('should display browse products button', async ({ page }) => {
    const browseButton = page.getByTestId('browse-products-button');
    await expect(browseButton).toBeVisible();
    await expect(browseButton).toContainText('Browse Products');
  });

  test('should have footer with company info', async ({ page }) => {
    const footer = page.locator('footer');
    await expect(footer).toBeVisible();
  });
});
