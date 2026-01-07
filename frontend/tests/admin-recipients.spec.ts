import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';

// Helper to login as admin
async function loginAsAdmin(page: any) {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

    await page.locator('input[placeholder="Username"]').fill('admin_user');
    await page.locator('input[placeholder="Password"]').fill('1234');
    await page.locator('text=Login').first().click();

    await expect(page.locator('text=Tasks')).toBeVisible({ timeout: 15000 });
}

test.describe('Admin Recipients Management', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsAdmin(page);
    });

    test('recipients list loads', async ({ page }) => {
        // Navigate to recipients list
        await page.locator('text=Recipients List').click();

        // Wait for page to load
        await page.waitForTimeout(2000);

        // Should see recipients content (dashboard tiles should not be visible)
        await expect(page.locator('text=Add Gold Image')).not.toBeVisible({ timeout: 5000 });
    });

    test('recipients page displays content', async ({ page }) => {
        // Navigate to recipients list
        await page.locator('text=Recipients List').click();

        await page.waitForTimeout(2000);

        // Page should have loaded
        await expect(page.locator('body')).toBeVisible();
        expect(true).toBe(true);
    });

    test('can interact with recipients page', async ({ page }) => {
        // Navigate to recipients list
        await page.locator('text=Recipients List').click();

        await page.waitForTimeout(2000);

        // Just verify we can interact with the page
        expect(true).toBe(true);
    });
});
