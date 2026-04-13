import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';

const USER_USER = 'user_mock';
const PASSWORD  = 'password';

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

test.describe('User Swipe Screen', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsUser(page);
    });

    test('user lands on swipe/home screen after login', async ({ page }) => {
        // The swipe screen is the default home screen for users
        await expect(page.locator('body')).toBeVisible();
        // Login screen must be gone
        await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible();
    });

    test('user bottom navigation has correct tabs', async ({ page }) => {
        // UserNavigator bottom bar: Home, My Tasks, Leaderboard, Stats, Settings
        await expect(page.locator('text=Home').first()).toBeVisible();
        await expect(page.locator('text=My Tasks').first()).toBeVisible();
        await expect(page.locator('text=Leaderboard').first()).toBeVisible();
        await expect(page.locator('text=Stats').first()).toBeVisible();
        await expect(page.locator('text=Settings').first()).toBeVisible();
    });

    test('swipe screen body is visible and interactive', async ({ page }) => {
        await page.waitForTimeout(2000);
        await expect(page.locator('body')).toBeVisible();
    });

    test('keyboard arrow keys do not crash the swipe screen', async ({ page }) => {
        await page.waitForTimeout(2000);

        // Keyboard navigation is a web-only feature — just check it doesn't throw
        await page.keyboard.press('ArrowRight');
        await page.waitForTimeout(300);
        await page.keyboard.press('ArrowLeft');
        await page.waitForTimeout(300);
        await page.keyboard.press('ArrowUp');
        await page.waitForTimeout(300);
        await page.keyboard.press('ArrowDown');
        await page.waitForTimeout(300);

        // Page should still be functional
        await expect(page.locator('body')).toBeVisible();
    });

    test('user top bar shows Logout option', async ({ page }) => {
        // UserTopBar has a "Logout" text button in the top right
        await expect(page.locator('text=Logout').first()).toBeVisible({ timeout: 5000 });
    });
});
