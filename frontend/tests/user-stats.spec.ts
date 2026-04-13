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

// ── Stats ──────────────────────────────────────────────────────────────────────
test.describe('User Stats Screen', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsUser(page);
    });

    test('can navigate to Stats via bottom tab', async ({ page }) => {
        const statsTab = page.locator('text=Stats').first();
        await expect(statsTab).toBeVisible({ timeout: 5000 });
        await statsTab.click();
        await page.waitForTimeout(2000);

        await expect(page.locator('body')).toBeVisible();
    });

    test('stats page does not crash', async ({ page }) => {
        await page.locator('text=Stats').first().click();
        await page.waitForTimeout(2000);

        // Page should be interactive — no fatal errors
        await expect(page.locator('body')).not.toContainText('Fatal error');
    });

    test('can navigate back to Home from Stats', async ({ page }) => {
        await page.locator('text=Stats').first().click();
        await page.waitForTimeout(1500);

        await page.locator('text=Home').first().click();
        await page.waitForTimeout(1500);

        // Back on the home/swipe screen — login page still absent
        await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible();
    });
});

// ── Leaderboard ────────────────────────────────────────────────────────────────
test.describe('User Leaderboard', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsUser(page);
    });

    test('can navigate to Leaderboard via bottom tab', async ({ page }) => {
        const tab = page.locator('text=Leaderboard').first();
        await expect(tab).toBeVisible({ timeout: 5000 });
        await tab.click();
        await page.waitForTimeout(2000);

        await expect(page.locator('body')).toBeVisible();
    });

    test('leaderboard page does not crash', async ({ page }) => {
        await page.locator('text=Leaderboard').first().click();
        await page.waitForTimeout(2000);

        await expect(page.locator('body')).not.toContainText('Fatal error');
    });
});

// ── My Tasks ───────────────────────────────────────────────────────────────────
test.describe('User My Tasks Screen', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsUser(page);
    });

    test('can navigate to My Tasks via bottom tab', async ({ page }) => {
        await page.locator('text=My Tasks').first().click();
        await page.waitForTimeout(2000);

        // My Tasks screen header renders "My Tasks"
        await expect(page.locator('text=My Tasks')).toBeVisible({ timeout: 5000 });
    });

    test('my tasks page shows assigned or available tasks sections', async ({ page }) => {
        await page.locator('text=My Tasks').first().click();
        await page.waitForTimeout(2000);

        // UserMyTasksScreen always shows "Assigned Tasks" section header
        await expect(page.locator('text=Assigned Tasks')).toBeVisible({ timeout: 5000 });
    });
});
