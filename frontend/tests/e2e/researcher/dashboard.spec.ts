import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';
const STARDBI_USER = 'swipe_lab_test_user';
const STARDBI_PASS = 'password';

test.describe('[E2E] R3 View Dashboard', () => {
    test.beforeEach(async ({ page }) => {
        test.setTimeout(120000);
        await page.goto(BASE_URL);
        await page.waitForLoadState('networkidle');
        await page.waitForTimeout(1500);
        
        // Login as researcher
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });
        await page.locator('input[placeholder="Enter your username"]').fill(STARDBI_USER);
        await page.locator('input[placeholder="Enter your password"]').fill(STARDBI_PASS);
        await page.locator('text=Login as Researcher').click();
        await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible({ timeout: 15000 });
    });

    test('views task dashboard and task details successfully', async ({ page }) => {
        test.setTimeout(60000);
        await page.waitForTimeout(1000);

        // From ResearcherDashboard (Home), navigate to Tasks
        await expect(page.locator('text=Tasks').first()).toBeVisible({ timeout: 15000 });
        await page.locator('text=Tasks').first().click(); // Click the 'Tasks' tile or tab

        // 1. Task list loads successfully
        await expect(page.getByPlaceholder('Search tasks…')).toBeVisible({ timeout: 15000 });

        // Wait for tasks to load. We look for 'Progress:' which is displayed on task cards
        await expect(page.locator('text=Progress:').first()).toBeVisible({ timeout: 15000 });

        // 2. Select a task
        await page.locator('text=Progress:').first().click();

        // 3. View task details
        await expect(page.locator('text=Back to Tasks')).toBeVisible({ timeout: 15000 });
        
        // 4. Assertions on details page
        await expect(page.locator('text=Classification Progress')).toBeVisible();
        await expect(page.locator('text=Min. Classifications')).toBeVisible();
        await expect(page.locator('text=Consensus')).toBeVisible();
        await expect(page.getByText('Species', { exact: true })).toBeVisible();
        
        // "images classified" is part of the string: "X of Y images classified"
        await expect(page.locator('text=images classified')).toBeVisible();
    });
});
