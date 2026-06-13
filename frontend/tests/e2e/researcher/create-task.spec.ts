import { test, expect } from '@playwright/test';
import path from 'path';

const BASE_URL = 'http://localhost:8081';
const STARDBI_USER = 'swipe_lab_test_user';
const STARDBI_PASS = 'password';
const E2E_IMAGE_PATH = path.join(__dirname, '../../../../backend/src/main/resources/e2e-crops/1_201_102.jpg');

test.describe('[E2E] R2 Create Task from Experiment', () => {
    test.describe.configure({ timeout: 120000 });

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
        await expect(page.locator('text=Tasks').first()).toBeVisible({ timeout: 15000 });
    });

    test('creates a task from a StarDBI experiment successfully', async ({ page }) => {
        test.setTimeout(60000);
        await page.waitForTimeout(1000); // wait for navigation/animation to settle
        // Open Add Task screen
        await page.locator('text=Add Task').first().click();
        await expect(page.locator('text=What\'s the name of your task?')).toBeVisible();

        // Step 1: Name the task
        const taskName = `E2E Test Task ${Date.now()}`;
        await page.getByPlaceholder('e.g., Pollinator Identification Survey').fill(taskName);
        await page.locator('text=Next →').click();

        // Step 2: Add Description
        await expect(page.locator('text=Describe the task')).toBeVisible();
        await page.getByPlaceholder('e.g., Identify whether each image contains the target pollinator species...').fill('This is a test description for E2E task creation flow.');
        await page.locator('text=Next →').click();

        // Step 3: Load available StarDBI experiments & select experiment
        await expect(page.locator('text=Select Experiments')).toBeVisible();
        await page.getByPlaceholder('Search experiments...').fill('Mock Experiment');
        await expect(page.locator('text="Mock Experiment 1"').first()).toBeVisible();
        await page.keyboard.press('Escape');
        await page.waitForTimeout(500);
        await page.locator('text="Mock Experiment 1"').first().click();
        await page.locator('text=Next →').click();

        // Step 4: Select species
        await expect(page.locator('text=Choose species to label')).toBeVisible();
        await page.getByPlaceholder('Search species...').fill('peregrinus');
        await expect(page.locator('text="thaumastocoris peregrinus"').first()).toBeVisible({ timeout: 10000 });
        await page.keyboard.press('Escape');
        await page.waitForTimeout(500);
        await page.locator('text="thaumastocoris peregrinus"').first().click();

        await expect(page.getByText('REFERENCE IMAGES', { exact: true })).toBeVisible({ timeout: 10000 });

        // Step 5: Upload reference image per species selected
        // Click the 'Upload' or 'From pool / Upload' button
        await page.getByText('Upload', { exact: false }).first().click();
        
        // The modal should open
        await expect(page.locator('text=UPLOAD NEW')).toBeVisible();

        // Trigger file chooser and upload
        const fileChooserPromise = page.waitForEvent('filechooser');
        await page.getByText('from device', { exact: false }).first().click();
        const fileChooser = await fileChooserPromise;
        await fileChooser.setFiles(E2E_IMAGE_PATH);

        // Close modal
        await page.locator('text=Done').click();
        await expect(page.locator('text=UPLOAD NEW')).not.toBeVisible();

        await page.locator('text=Next →').click();

        // Step 6: Configure task distribution (Public/Restricted)
        await expect(page.locator('text=Who should see this task?')).toBeVisible();
        // Keep Restricted (default), but explicitly click it just in case
        await page.locator('text=Restricted').click();

        // Step 7: Select co-managers
        await page.getByPlaceholder('Search researchers...').fill('swipe_lab');
        await page.waitForTimeout(500);
        await expect(page.locator('text=swipe_lab').first()).toBeVisible();
        await page.keyboard.press('Escape');
        await page.waitForTimeout(500);
        await page.locator('text=swipe_lab').first().click();

        await page.locator('text=Next →').click();

        // Step 8: Confirm and Create Task
        await expect(page.locator('text=Review & Confirm')).toBeVisible();
        await expect(page.locator(`text=${taskName}`)).toBeVisible();
        await page.locator('text=✓ Create Task').click();

        // Assertions: Task creation succeeds
        await expect(page.locator('text=Task Created!')).toBeVisible({ timeout: 15000 });

        // Assertions: New task appears in dashboard
        await page.locator('text=Go to Tasks').click();
        await expect(page.locator(`text=${taskName}`).first()).toBeVisible({ timeout: 15000 });
    });
});
