import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';

const ADMIN_USER = 'admin_mock';
const PASSWORD   = 'password';

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

test.describe('Admin Recipients Management', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsAdmin(page);
    });

    test('recipients list tile exists on dashboard', async ({ page }) => {
        await expect(page.locator('text=Recipients List')).toBeVisible();
    });

    test('clicking Recipients List navigates away from dashboard', async ({ page }) => {
        await page.locator('text=Recipients List').click();
        await page.waitForTimeout(2000);

        // Dashboard-specific tiles should no longer be visible
        await expect(page.locator('text=Add Gold Image')).not.toBeVisible({ timeout: 5000 });
    });

    test('recipients page body is visible', async ({ page }) => {
        await page.locator('text=Recipients List').click();
        await page.waitForTimeout(2000);

        await expect(page.locator('body')).toBeVisible();
        // Recipients page should show some content
        await expect(page.locator('body')).not.toContainText('Fatal error');
    });

    test('can navigate back to dashboard from recipients via Home tab', async ({ page }) => {
        await page.locator('text=Recipients List').click();
        await page.waitForTimeout(2000);

        await page.locator('text=Home').first().click();
        await page.waitForTimeout(1500);

        // Dashboard is back — all tiles visible
        await expect(page.locator('text=Add Task').first()).toBeVisible({ timeout: 5000 });
        await expect(page.locator('text=Add Gold Image').first()).toBeVisible({ timeout: 5000 });
    });
});
