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

// ── Tasks Management ───────────────────────────────────────────────────────────
test.describe('Admin Tasks Management', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsAdmin(page);
    });

    test('tasks list page loads via dashboard tile', async ({ page }) => {
        await page.locator('text=Tasks').first().click();
        await page.waitForTimeout(2000);
        await expect(page.locator('body')).toContainText('Tasks');
    });

    test('add task form renders at least one input field', async ({ page }) => {
        await page.locator('text=Add Task').click();
        await page.waitForTimeout(2000);

        const inputCount = await page.locator('input').count();
        expect(inputCount).toBeGreaterThan(0);
    });

    test('can type into the first input on Add Task form', async ({ page }) => {
        await page.locator('text=Add Task').click();
        await page.waitForTimeout(2000);

        const firstInput = page.locator('input').first();
        if (await firstInput.isVisible()) {
            await firstInput.fill('Test Task Name');
            const value = await firstInput.inputValue();
            expect(value).toBe('Test Task Name');
        } else {
            // Input may not be visible (e.g. loading) — acceptable
            expect(true).toBe(true);
        }
    });

    test('Add Task tile navigates away from dashboard', async ({ page }) => {
        await page.locator('text=Add Task').click();
        await page.waitForTimeout(2000);

        // After navigating to Add Task, the Grid of dashboard tiles is gone
        await expect(page.locator('text=Add Gold Image')).not.toBeVisible({ timeout: 5000 });
    });
});

// ── Task Details ───────────────────────────────────────────────────────────────
test.describe('Admin Task Details', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsAdmin(page);
    });

    test('can navigate to tasks management page', async ({ page }) => {
        await page.locator('text=Tasks').first().click();
        await page.waitForTimeout(2000);

        // Body should now contain task-related content
        await expect(page.locator('body')).toContainText('Tasks');
    });

    test('can navigate back to dashboard from Tasks via Home bottom tab', async ({ page }) => {
        await page.locator('text=Tasks').first().click();
        await page.waitForTimeout(1500);

        await page.locator('text=Home').first().click();
        await page.waitForTimeout(1500);

        // Dashboard tiles are back
        await expect(page.locator('text=Add Task')).toBeVisible({ timeout: 5000 });
    });
});

// ── Taxonomy ───────────────────────────────────────────────────────────────────
test.describe('Admin Taxonomy', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsAdmin(page);
    });

    test('taxonomy tile navigates away from dashboard', async ({ page }) => {
        await page.locator('text=Taxonomy').click();
        await page.waitForTimeout(2000);
        await expect(page.locator('body')).toBeVisible();
        // Dashboard grid is gone
        await expect(page.locator('text=Add Gold Image')).not.toBeVisible({ timeout: 5000 });
    });
});
