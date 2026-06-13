import { test, expect } from '@playwright/test';
import * as fs from 'fs';

const BASE_URL = 'http://localhost:8081';
const STARDBI_USER = 'swipe_lab_test_user';
const STARDBI_PASS = 'password';

test.describe('[E2E] R7 Export Data', () => {
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

    test('should successfully select tasks and export classification data', async ({ page }) => {
        test.setTimeout(60000);
        await page.waitForTimeout(1000);

        // 1. Open Analytics
        await expect(page.locator('text=Analytics').last()).toBeVisible({ timeout: 15000 });
        await page.locator('text=Analytics').last().click();

        // Wait for AnalyticsScreen to render and check for "Export to CSV"
        await expect(page.locator('text=Export to CSV')).toBeVisible({ timeout: 15000 });

        // 2. Trigger export csv
        await page.locator('text=Export to CSV').click();

        // 3. Wait for modal to open
        await expect(page.locator('text=📥 Export Classifications to CSV')).toBeVisible({ timeout: 10000 });

        // 4. Select the task (e.g., E2E Identification Task)
        const taskOption = page.locator('text=E2E Identification Task').first();
        await expect(taskOption).toBeVisible({ timeout: 10000 });
        await taskOption.click();

        // Ensure at least 1 task is selected in the bottom bar
        await expect(page.locator('text=/1 task(.*) selected/')).toBeVisible({ timeout: 10000 });

        // 5. Trigger download
        const downloadPromise = page.waitForEvent('download', { timeout: 30000 });
        
        // Click the final "Export" button in the modal
        // Using exact text match to avoid matching the title
        await page.locator('text="Export"').click();
        
        const download = await downloadPromise;

        // 6. Assertions
        expect(download).toBeTruthy();
        
        // Validate filename matches expected pattern
        const suggestedFilename = download.suggestedFilename();
        expect(suggestedFilename).toContain('swipelab_classifications_export_');
        expect(suggestedFilename).toContain('.csv');
        
        // Save the file temporarily to inspect its contents
        const downloadPath = await download.path();
        expect(downloadPath).toBeTruthy();
        
        if (downloadPath) {
            // Check that the file was actually written to disk
            expect(fs.existsSync(downloadPath)).toBeTruthy();
            
            // Read file and validate it's not empty and contains CSV structure
            const fileContent = fs.readFileSync(downloadPath, 'utf8');
            expect(fileContent.length).toBeGreaterThan(0);
            
            // A basic CSV check: it should contain commas for columns
            expect(fileContent).toContain(',');
            // Should contain at least the header row
            expect(fileContent).toContain('\n');
        }
    });
});
