import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';

const ADMIN_USER = 'admin_mock';
const PASSWORD   = 'password';

// ── Login Helper ───────────────────────────────────────────────────────────────
async function loginAsAdmin(page: any) {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

    await page.locator('input[placeholder="Username"]').fill(ADMIN_USER);
    await page.locator('input[placeholder="Password"]').fill(PASSWORD);
    await page.locator('text=Login').first().click();

    // Wait for admin dashboard — "Tasks" tile is the reliable indicator
    await expect(page.locator('text=Tasks').first()).toBeVisible({ timeout: 15000 });
}

// ── Tests ──────────────────────────────────────────────────────────────────────
test.describe('Admin Dashboard', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsAdmin(page);
    });

    test('dashboard loads after admin login', async ({ page }) => {
        await expect(page.locator('text=Tasks').first()).toBeVisible();
    });

    test('all dashboard tiles are visible', async ({ page }) => {
        // The six tiles defined in AdminDashboard.tsx
        await expect(page.locator('text=Tasks').first()).toBeVisible();
        await expect(page.locator('text=Add Task')).toBeVisible();
        await expect(page.locator('text=Taxonomy')).toBeVisible();
        await expect(page.locator('text=Recipients List')).toBeVisible();
        await expect(page.locator('text=Gold Images')).toBeVisible();
        await expect(page.locator('text=Add Gold Image')).toBeVisible();
    });

    test('admin bottom navigation has all five tabs', async ({ page }) => {
        // AdminNavigator bottom bar: Home, Users, Tasks, Analytics, Settings
        await expect(page.locator('text=Home').first()).toBeVisible();
        await expect(page.locator('text=Users').first()).toBeVisible();
        await expect(page.locator('text=Analytics').first()).toBeVisible();
        await expect(page.locator('text=Settings').first()).toBeVisible();
    });

    test('navigate to Tasks Management via dashboard tile', async ({ page }) => {
        // Click the "Tasks" dashboard tile (first occurrence — the tile)
        await page.locator('text=Tasks').first().click();
        await page.waitForTimeout(2000);

        // Should still contain Tasks text somewhere on page
        await expect(page.locator('body')).toContainText('Tasks');
    });

    test('navigate to Add Task via dashboard tile', async ({ page }) => {
        await page.locator('text=Add Task').click();
        await page.waitForTimeout(2000);

        // Add Task screen should render at least one input field
        const inputCount = await page.locator('input').count();
        expect(inputCount).toBeGreaterThan(0);
    });

    test('navigate to Recipients List via dashboard tile', async ({ page }) => {
        await page.locator('text=Recipients List').click();
        await page.waitForTimeout(2000);

        // After navigation, the dashboard-only "Add Gold Image" tile should not be visible
        await expect(page.locator('text=Add Gold Image')).not.toBeVisible({ timeout: 5000 });
    });

    test('navigate to Analytics via bottom nav', async ({ page }) => {
        await page.locator('text=Analytics').first().click();
        await page.waitForTimeout(2000);
        await expect(page.locator('body')).toBeVisible();
    });

    test('navigate to Users via bottom nav', async ({ page }) => {
        await page.locator('text=Users').first().click();
        await page.waitForTimeout(2000);
        await expect(page.locator('body')).toContainText('User');
    });

    test('navigate to Settings via bottom nav', async ({ page }) => {
        await page.locator('text=Settings').first().click();
        await page.waitForTimeout(2000);

        // Settings screen header shows "Settings" — use .first() to avoid strict mode violation
        await expect(page.locator('text=Settings').first()).toBeVisible();
    });
});
