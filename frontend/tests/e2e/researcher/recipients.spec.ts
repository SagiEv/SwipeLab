import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';
const STARDBI_USER = 'swipe_lab_test_user';
const STARDBI_PASS = 'password';

test.describe('[E2E] R6 Manage Recipients', () => {
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

    test('creates a recipient group, adds users, and assigns it to a task', async ({ page }) => {
        test.setTimeout(120000);
        await page.waitForTimeout(1000);

        // 2. Open Recipients Management
        await page.locator('text=Recipients List').first().click();
        await expect(page.locator('text=New List')).toBeVisible({ timeout: 15000 });

        // 3. Create recipient group
        await page.locator('text=New List').click();
        await expect(page.locator('text=Create New List')).toBeVisible();

        const groupName = `E2E Group ${Date.now()}`;
        await page.getByPlaceholder('Enter a new group name').fill(groupName);

        // 4. Add users to group
        await page.getByPlaceholder('Search...').fill('e2e_user');
        
        // Wait for e2e_user to appear and select it
        await expect(page.locator('text=e2e_user').first()).toBeVisible();
        await page.locator('text=e2e_user').first().click();

        // Click create button
        await page.locator('text=Create (Selected)').click();
        await expect(page.locator('text=Create New List')).not.toBeVisible();

        // Assert Group appears in list
        await expect(page.locator(`text=${groupName}`).first()).toBeVisible({ timeout: 10000 });

        // Wait for modal to fully close
        await page.waitForTimeout(1000);

        // 5. Assign group to task
        // Go to Tasks Management
        // Navigate to Tasks to create a new task
        await page.locator('text=Tasks').last().click();
        // Click the first task card (handles clean DB and dirty DB from previous test runs)
        const taskCard = page.locator('div[tabindex="0"]:visible').filter({ hasText: /E2E (Identification|Updated) Task/ }).first();
        await expect(taskCard).toBeVisible({ timeout: 10000 });
        await taskCard.click();

        // Handle window.alert for success/error
        page.on('dialog', dialog => dialog.accept());

        // Wait for TaskDetailsScreen and click Edit
        const editBtn1 = page.locator('text="Edit"').last();
        await expect(editBtn1).toBeVisible({ timeout: 10000 });
        await editBtn1.click();

        // Wait for EditTaskScreen to load
        await expect(page.locator('text=Task Visibility')).toBeVisible({ timeout: 10000 });
        
        // Wait for task details to populate from API
        await page.waitForTimeout(2000);
        
        // Search and assign the newly created group
        const groupLabel = `(Group) ${groupName}`;
        await page.getByPlaceholder('Search users or groups...').fill(groupName);
        await expect(page.locator(`text=${groupLabel}`).first()).toBeVisible({ timeout: 10000 });
        
        // Dismiss keyboard to ensure the list is not covered
        await page.keyboard.press('Escape');
        await page.waitForTimeout(500);
        
        // Select the group from the dropdown
        await page.locator(`text=${groupLabel}`).first().click();

        // Assert the group is selected in the MultiSelect view
        await expect(page.locator(`text=${groupLabel}`).first()).toBeVisible();

        // Save changes - Ensure we scroll to it
        await page.locator('text=Update Task').scrollIntoViewIfNeeded();
        await page.locator('text=Update Task').click();

        // Assert redirect to Tasks Management by checking for a task card
        const updatedTaskCard = page.locator('div[tabindex="0"]:visible').filter({ hasText: /E2E (Identification|Updated) Task/ }).first();
        await expect(updatedTaskCard).toBeVisible({ timeout: 15000 });

        // Reload the page to ensure fresh state and clear navigation ghosts
        await page.reload();
        await expect(page.locator('text=Tasks').last()).toBeVisible({ timeout: 10000 });
        await page.locator('text=Tasks').last().click();

        // Navigate back to edit screen
        const finalTaskCard = page.locator('div[tabindex="0"]:visible').filter({ hasText: /E2E (Identification|Updated) Task/ }).first();
        await expect(finalTaskCard).toBeVisible({ timeout: 10000 });
        await finalTaskCard.click();

        const editBtn2 = page.locator('text="Edit"').last();
        await expect(editBtn2).toBeVisible({ timeout: 10000 });
        await editBtn2.click();
        
        await expect(page.locator('text=Task Visibility')).toBeVisible({ timeout: 10000 });
        await expect(page.locator(`text=${groupLabel}`).first()).toBeVisible({ timeout: 10000 });
    });
});
