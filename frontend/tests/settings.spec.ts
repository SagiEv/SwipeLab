import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';

const USER_USER  = 'user_mock';
const ADMIN_USER = 'admin_mock';
const PASSWORD   = 'password';

async function loginAsUser(page: any) {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

    await page.locator('input[placeholder="Username"]').fill(USER_USER);
    await page.locator('input[placeholder="Password"]').fill(PASSWORD);
    await page.locator('text=Login').first().click();

    await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible({ timeout: 15000 });
    await page.waitForTimeout(1000);
}

async function loginAsAdmin(page: any) {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

    await page.locator('input[placeholder="Username"]').fill(ADMIN_USER);
    await page.locator('input[placeholder="Password"]').fill(PASSWORD);
    await page.locator('text=Login').first().click();

    await expect(page.locator('text=Tasks').first()).toBeVisible({ timeout: 15000 });
}

// ── User Settings ──────────────────────────────────────────────────────────────
test.describe('Settings Screen - User', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsUser(page);
    });

    test('can navigate to Settings via bottom tab', async ({ page }) => {
        await page.locator('text=Settings').first().click();
        await page.waitForTimeout(2000);

        // Settings screen header shows "Settings"
        await expect(page.locator('text=Settings').first()).toBeVisible();
    });

    test('settings page shows Profile, Notifications, Dark Mode options', async ({ page }) => {
        await page.locator('text=Settings').first().click();
        await page.waitForTimeout(2000);

        await expect(page.locator('text=Profile')).toBeVisible();
        await expect(page.locator('text=Notifications')).toBeVisible();
        await expect(page.locator('text=Dark Mode')).toBeVisible();
    });

    test('Log Out button exists on settings page', async ({ page }) => {
        await page.locator('text=Settings').first().click();
        await page.waitForTimeout(2000);

        // The logout button text in SettingsScreen is "Log Out" (two words)
        await expect(page.locator('text=Log Out')).toBeVisible({ timeout: 5000 });
    });

    test('clicking Log Out returns to login screen', async ({ page }) => {
        await page.locator('text=Settings').first().click();
        await page.waitForTimeout(2000);

        await page.locator('text=Log Out').click();
        await page.waitForTimeout(2000);

        // Should be back on the login screen
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 10000 });
    });

    test('user top bar Logout also works', async ({ page }) => {
        // UserTopBar has an inline "Logout" button (different from settings "Log Out")
        await expect(page.locator('text=Logout').first()).toBeVisible({ timeout: 5000 });
        await page.locator('text=Logout').first().click();
        await page.waitForTimeout(2000);

        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 10000 });
    });
});

// ── Admin Settings ─────────────────────────────────────────────────────────────
test.describe('Settings Screen - Admin', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsAdmin(page);
    });

    test('admin can navigate to Settings via bottom tab', async ({ page }) => {
        await page.locator('text=Settings').first().click();
        await page.waitForTimeout(2000);

        await expect(page.locator('text=Settings').first()).toBeVisible();
    });

    test('admin settings page shows Log Out button', async ({ page }) => {
        await page.locator('text=Settings').first().click();
        await page.waitForTimeout(2000);

        await expect(page.locator('text=Log Out')).toBeVisible({ timeout: 5000 });
    });
});
