import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';
const STARDBI_USER = 'swipe_lab_test_user';
const STARDBI_PASS = 'password';

test.describe('[E2E] R5 View Analytics', () => {
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

    test('views overview tab and navigates to tasks tab to review analytics', async ({ page }) => {
        test.setTimeout(60000);
        await page.waitForTimeout(1000);

        // 2. Open Analytics
        // In the BottomBar, there is a label 'Analytics'
        await expect(page.locator('text=Analytics').last()).toBeVisible({ timeout: 15000 });
        await page.locator('text=Analytics').last().click();

        // Wait for AnalyticsScreen to render. Check for "Export to CSV"
        await expect(page.locator('text=Export to CSV')).toBeVisible({ timeout: 15000 });

        // 3. View Overview tab (default)
        // Assert Overview metrics load successfully
        await expect(page.locator('text=📊 Activity')).toBeVisible();
        await expect(page.locator('text=🔢 Platform Totals')).toBeVisible();
        
        // Confidence trends are displayed
        await expect(page.locator('text=📈 Credibility Trend')).toBeVisible();
        
        // Label Distribution
        await expect(page.locator('text=🏷️ Label Distribution (30d)')).toBeVisible();

        // 4. Switch to Tasks tab
        // The tab text is '📋 Tasks'
        await page.locator('text=📋 Tasks').click();
        
        // Assert Tasks tab loaded
        await expect(page.locator('text=Select a task to view its analytics')).toBeVisible({ timeout: 15000 });

        // 5. Review analytics visualizations (Per-task analytics)
        // Find the first task row that displays the progress string
        const firstTask = page.locator('text=/\\d+\\/\\d+ images/').first();
        await expect(firstTask).toBeVisible({ timeout: 15000 });
        
        // Click the task to expand its analytics
        await firstTask.click();
        
        // Wait for the Task Analytics Detail to load
        await expect(page.locator('text=📊 Progress').first()).toBeVisible({ timeout: 15000 });
        await expect(page.locator('text=✨ Data Quality').first()).toBeVisible();
        await expect(page.locator('text=👥 Participation').first()).toBeVisible();
    });
});
