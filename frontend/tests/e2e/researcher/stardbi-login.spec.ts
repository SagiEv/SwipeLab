import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';
const STARDBI_USER = 'swipe_lab_test_user';
const STARDBI_PASS = 'password';

test.describe('[E2E] R1 StarDBI Login', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto(BASE_URL);
        await page.waitForLoadState('networkidle');
        await page.waitForTimeout(1500);
    });

    test('researcher can authenticate using StarDBI and access dashboard', async ({ page }) => {
        // Open Login Screen & Verify it's loaded
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });

        // Authenticate through mocked StarDBI flow
        await page.locator('input[placeholder="Enter your username"]').fill(STARDBI_USER);
        await page.locator('input[placeholder="Enter your password"]').fill(STARDBI_PASS);
        
        // Select Researcher Login
        await page.locator('text=Login as Researcher').click();

        // Assertions:
        // 1. Mock StarDBI authentication succeeds and we leave the login screen
        await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible({ timeout: 15000 });
        
        // 2. User is auto-provisioned as RESEARCHER and Dashboard becomes accessible
        await expect(page.locator('text=Tasks').first()).toBeVisible({ timeout: 15000 });
        await expect(page.locator('text=Add Task').first()).toBeVisible({ timeout: 15000 });
        await expect(page.locator('text=Taxonomy').first()).toBeVisible({ timeout: 15000 });
    });
});
