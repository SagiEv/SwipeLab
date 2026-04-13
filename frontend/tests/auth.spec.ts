import { test, expect, Page } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';

// ── Credentials ────────────────────────────────────────────────────────────────
const ADMIN_USER = 'admin_mock';
const USER_USER  = 'user_mock';
const PASSWORD   = 'password';

test.describe('Authentication', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto(BASE_URL);
        await page.waitForLoadState('networkidle');
        await page.waitForTimeout(1500);
    });

    test('login page loads correctly', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 20000 });
        // Subtitle contains "Label • Improve Research" (partial match)
        await expect(page.locator('text=Improve Research')).toBeVisible({ timeout: 5000 });
    });

    test('shows error with invalid credentials', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        await page.locator('input[placeholder="Username"]').fill('wrong_user');
        await page.locator('input[placeholder="Password"]').fill('wrongpass');
        await page.locator('text=Login').first().click();

        await expect(page.locator('text=Invalid username or password')).toBeVisible({ timeout: 10000 });
    });

    test('login with valid admin credentials', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        await page.locator('input[placeholder="Username"]').fill(ADMIN_USER);
        await page.locator('input[placeholder="Password"]').fill(PASSWORD);
        await page.locator('text=Login').first().click();

        // Admin dashboard shows "Tasks" tile after login
        await expect(page.locator('text=Tasks').first()).toBeVisible({ timeout: 15000 });
    });

    test('login with valid user credentials', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        await page.locator('input[placeholder="Username"]').fill(USER_USER);
        await page.locator('input[placeholder="Password"]').fill(PASSWORD);
        await page.locator('text=Login').first().click();

        // Should navigate away from login page
        await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible({ timeout: 15000 });
    });

    test('login also works by pressing Enter', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        await page.locator('input[placeholder="Username"]').fill(USER_USER);
        const passwordInput = page.locator('input[placeholder="Password"]');
        await passwordInput.fill(PASSWORD);
        await passwordInput.press('Enter');

        await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible({ timeout: 15000 });
    });

    test('register form appears when clicking register link', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        // Click the Register link at the bottom of the login screen
        await page.locator('text=Register').last().click();

        // Register overlay shows a "Confirm Password" field unique to that form
        await expect(page.locator('input[placeholder="Confirm Password"]')).toBeVisible({ timeout: 10000 });
    });

    test('Google OAuth button is present', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });
        await expect(page.locator('text=Continue with Google')).toBeVisible();
    });

    test('Login as Researcher button is present', async ({ page }) => {
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });
        await expect(page.locator('text=Login as Researcher')).toBeVisible();
    });
});

// ── Shared Login Helpers ───────────────────────────────────────────────────────

export async function loginAsAdmin(page: Page) {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

    await page.locator('input[placeholder="Username"]').fill(ADMIN_USER);
    await page.locator('input[placeholder="Password"]').fill(PASSWORD);
    await page.locator('text=Login').first().click();

    // Wait for admin dashboard (Tasks tile appears)
    await expect(page.locator('text=Tasks').first()).toBeVisible({ timeout: 15000 });
}

export async function loginAsUser(page: Page) {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

    await page.locator('input[placeholder="Username"]').fill(USER_USER);
    await page.locator('input[placeholder="Password"]').fill(PASSWORD);
    await page.locator('text=Login').first().click();

    // Wait for navigation away from login
    await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible({ timeout: 15000 });
    await page.waitForTimeout(1000);
}
