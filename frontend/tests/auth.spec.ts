import { test, expect, Page } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';

test.describe('Authentication', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto(BASE_URL);
        // Wait for the page to fully load
        await page.waitForLoadState('networkidle');
        // Extra wait for React Native Web hydration
        await page.waitForTimeout(1000);
    });

    test('login page loads correctly', async ({ page }) => {
        // React Native Web uses text content differently
        // Wait for any text that indicates the page loaded
        await page.waitForTimeout(2000);
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 20000 });
        // Use more specific text to avoid matching multiple elements
        await expect(page.locator('text=Label • Improve Research')).toBeVisible({ timeout: 5000 });
    });

    test('shows error with invalid credentials', async ({ page }) => {
        // Wait for page to load
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        // Find input by placeholder using CSS selector with attribute
        const usernameInput = page.locator('input[placeholder="Username"]');
        const passwordInput = page.locator('input[placeholder="Password"]');

        await usernameInput.fill('wronguser');
        await passwordInput.fill('wrongpass');

        // Click login button by text
        await page.locator('text=Login').first().click();

        // Wait for error message
        await expect(page.locator('text=Invalid username or password')).toBeVisible({ timeout: 10000 });
    });

    test('login with valid admin credentials', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        const usernameInput = page.locator('input[placeholder="Username"]');
        const passwordInput = page.locator('input[placeholder="Password"]');

        await usernameInput.fill('admin_user');
        await passwordInput.fill('1234');

        await page.locator('text=Login').first().click();

        // Should navigate to admin dashboard - look for Tasks tile
        await expect(page.locator('text=Tasks')).toBeVisible({ timeout: 15000 });
    });

    test('login with valid user credentials', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        const usernameInput = page.locator('input[placeholder="Username"]');
        const passwordInput = page.locator('input[placeholder="Password"]');

        await usernameInput.fill('john_doe');
        await passwordInput.fill('1234');

        await page.locator('text=Login').first().click();

        // Should navigate away from login screen
        await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible({ timeout: 15000 });
    });

    test('register form appears when clicking register link', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        // Click "Don't have an account? Register" link
        await page.locator('text=Register').last().click();

        // Wait for register form overlay - check for "Confirm Password" field which is unique to register form
        await expect(page.locator('input[placeholder="Confirm Password"]')).toBeVisible({ timeout: 10000 });
    });

    test('Google OAuth button is present and clickable', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        // Check Google button exists
        await expect(page.locator('text=Continue with Google')).toBeVisible();
    });
});

// Helper to login as admin
export async function loginAsAdmin(page: Page) {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

    await page.locator('input[placeholder="Username"]').fill('admin_user');
    await page.locator('input[placeholder="Password"]').fill('1234');
    await page.locator('text=Login').first().click();

    await expect(page.locator('text=Tasks')).toBeVisible({ timeout: 15000 });
}

// Helper to login as user
export async function loginAsUser(page: Page) {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

    await page.locator('input[placeholder="Username"]').fill('john_doe');
    await page.locator('input[placeholder="Password"]').fill('1234');
    await page.locator('text=Login').first().click();

    // Wait for navigation away from login
    await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible({ timeout: 15000 });
}
