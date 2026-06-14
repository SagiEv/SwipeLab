import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';
const ADMIN_USER = 'admin_e2e'; // The superadmin seeded in E2E
const PASSWORD = 'superpassword123';

test.describe('[E2E] A4 Malicious Labeling Config', () => {
    test.beforeEach(async ({ page }) => {
        test.setTimeout(120000);
        await page.goto(BASE_URL);
        await page.waitForLoadState('networkidle');
        await page.waitForTimeout(1500);

        // Login as Superadmin (using regular login for superadmin, or researcher login?)
        // The E2E seeder says: admin_e2e / superpassword123 / RESEARCHER
        page.on('console', msg => console.log('PAGE LOG:', msg.text()));
        page.on('response', async response => {
            if (response.status() === 500) {
                const body = await response.text();
                console.log('500 ERROR BODY:', body);
            }
        });
        
        await expect(page.locator('text=Welcome to SwipeLab')).toBeVisible({ timeout: 15000 });
        await page.locator('input[placeholder="Enter your username"]').fill(ADMIN_USER);
        await page.locator('input[placeholder="Enter your password"]').fill(PASSWORD);
        await page.locator('text=Login').first().click();

        // Check for error text if it appears
        page.locator('.error').textContent().then(text => console.log('ERROR ON PAGE:', text)).catch(() => {});

        await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible({ timeout: 15000 });

        // Wait for dashboard to load
        await expect(page.locator('text=Tasks').first()).toBeVisible({ timeout: 15000 });

        // Navigate to Settings
        await page.locator('text=Settings').first().click();
        await page.waitForTimeout(1000);

        // Click Malicious Labeling Config row
        await expect(page.locator('text=Malicious Labeling Config').first()).toBeVisible();
        await page.locator('text=Malicious Labeling Config').first().click();
        await page.waitForTimeout(1000);
    });

    test('views config and updates threshold', async ({ page }) => {
        test.setTimeout(60000);

        // Ensure we are on the config screen
        await expect(page.getByText('Malicious Threshold', { exact: true }).first()).toBeVisible();

        // Check if current value is visible. E2E defaults threshold to 15.0
        const thresholdInput = page.getByText('Malicious Threshold').locator('../..').locator('input');
        await expect(thresholdInput).toBeVisible();

        // Ensure we enter a completely different value from current so Save button enables
        const newValue = (Math.floor(Math.random() * 50) + 10) + '.5';
        console.log('Using new threshold value:', newValue);

        // Fill a new value
        await thresholdInput.click();
        await thresholdInput.fill(newValue); // Playwright fill works on RN Web directly

        // Save
        const saveButton = page.locator('text=Save Changes');
        await expect(saveButton).toBeEnabled();
        await saveButton.click();

        // Verify success toast/message
        await expect(page.locator('text=Configuration saved successfully.')).toBeVisible({ timeout: 10000 });

        // Verify audit log has new entry
        await expect(page.locator('text=credibility.malicious_threshold')).toBeVisible();
        await expect(page.locator('text=admin_e2e').first()).toBeVisible();
        await expect(page.locator(`text=${newValue}`).first()).toBeVisible();
    });

    test('toggles auto-ban and updates', async ({ page }) => {
        test.setTimeout(60000);

        // Find the auto-ban switch
        const autoBanSwitch = page.locator('role=switch').first();
        await expect(autoBanSwitch).toBeVisible();

        // Click it to toggle
        await autoBanSwitch.click();

        // Save
        const saveButton = page.locator('text=Save Changes');
        await expect(saveButton).toBeEnabled();
        await saveButton.click();

        // Verify success
        await expect(page.locator('text=Configuration saved successfully.')).toBeVisible({ timeout: 10000 });

        // Verify audit log
        await expect(page.locator('text=fraud.auto_ban_enabled')).toBeVisible();
    });
});
