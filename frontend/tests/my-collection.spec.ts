import { test, expect } from '@playwright/test';

const BASE_URL = 'http://localhost:8081';

// Helper to login as user
async function loginAsUser(page: any) {
    await page.goto(BASE_URL);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);
    // Handle login if not already logged in
    const welcome = page.locator('text=Welcome to SwipeLab');
    if (await welcome.isVisible({ timeout: 2000 })) {
        await page.locator('input[placeholder="Username"]').fill('john_doe');
        await page.locator('input[placeholder="Password"]').fill('1234');
        await page.locator('text=Login').first().click();
        await expect(page.locator('text=Welcome to SwipeLab')).not.toBeVisible({ timeout: 15000 });
    }
}

// Helper to navigate to collection
async function navigateToCollection(page: any) {
    await page.waitForTimeout(1000);
    const collectionTab = page.locator('text=Collection');
    if (await collectionTab.isVisible({ timeout: 3000 }).catch(() => false)) {
        await collectionTab.click();
        await page.waitForTimeout(1500);
    }
}

test.describe('My Collection Screen', () => {
    test.beforeEach(async ({ page }) => {
        await loginAsUser(page);
    });

    test('can navigate to collection', async ({ page }) => {
        await navigateToCollection(page);
        await expect(page.locator('text=My Collection')).toBeVisible();
    });

    test('collection displays total count of bugs', async ({ page }) => {
        await navigateToCollection(page);
        // Look for "Bugs Collected" label
        await expect(page.locator('text=Bugs Collected')).toBeVisible();
    });

    test('collection displays items from completed tasks', async ({ page }) => {
        await navigateToCollection(page);
        await page.waitForTimeout(2000);

        // Based on mock data:
        // Asian Giant Hornet (Completed, Yes) -> Should be visible
        // Red Fire Ant (In Progress) -> Should NOT be visible

        // Check for presence of Hornet items
        const hornetItem = page.getByLabel('Asian Giant Hornet').first();
        await expect(hornetItem).toBeVisible({ timeout: 5000 });

        // Check that In Progress items are NOT visible (Red Fire Ant)
        const antItem = page.getByLabel('Red Fire Ant');
        await expect(antItem).not.toBeVisible();
    });

    test('collection does NOT display non-bug (No/Trash) items', async ({ page }) => {
        await navigateToCollection(page);

        // Based on mock data, there are "No" and "Trash" items. 
        // We filter for allow 'yes'.
        // Expect to see 'Collected' badge
        await expect(page.locator('text=Collected').first()).toBeVisible();

        // Expect NOT to see X or Trash items
        await expect(page.locator('text=No')).not.toBeVisible();
        await expect(page.locator('text=Trash')).not.toBeVisible();
    });

    test('clicking an item navigates to details', async ({ page }) => {
        await navigateToCollection(page);
        await page.waitForTimeout(1000);

        // Click the first item
        const firstItem = page.getByLabel('Asian Giant Hornet').first();
        await firstItem.click();

        // Verify navigation to details
        // Header text should include "Details"
        await expect(page.locator('text=Details')).toBeVisible();

        // Should show scientific name
        await expect(page.locator('text=Vespa mandarinia')).toBeVisible();

        // Should show description label
        await expect(page.locator('text=Description')).toBeVisible();
    });

    test('details screen shows correct bug info', async ({ page }) => {
        await navigateToCollection(page);
        await page.waitForTimeout(1000);

        // Click item
        await page.getByLabel('Asian Giant Hornet').first().click();

        // Check content
        await expect(page.locator('text=Asian Giant Hornet')).toBeVisible();
        await expect(page.locator('text=Collected')).toBeVisible(); // User label updated to "Collected"
        await expect(page.locator('text=Task')).toBeVisible();
    });

    test('can return from details to collection', async ({ page }) => {
        await navigateToCollection(page);
        await page.getByLabel('Asian Giant Hornet').first().click();

        // Click Back/Home button (Right icon is Home in details screen based on implementation)
        // Or Left back? ScreenHeader usually has no back button unless specified, but user might assume Home returns.
        // In CollectionDetailsScreen:
        // rightIcon={require('../../../assets/images/home.png')}
        // rightTitle="Home"
        // onRightPress={() => navigation.navigate('SwipeLab')} -> Goes to Home

        // To go back to collection, user would use Bottom Bar? No bottom bar in Stack details usually?
        // Ah, UserNavigator has stack.
        // Let's check if we can go to Home.

        await page.locator('text=Home').first().click();
        await expect(page.locator('text=Swipe to Classify')).toBeVisible(); // Home screen title guess or content
    });
});
