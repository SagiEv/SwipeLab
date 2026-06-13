import { test, expect } from '@playwright/test';
import path from 'path';

const BASE_URL = 'http://localhost:8081';
const STARDBI_USER = 'swipe_lab_test_user';
const STARDBI_PASS = 'password';

const IMAGES = [
    {
        filename: 'bee.png',
        isRequestedSpecies: false,
        difficulty: 'Easy',
        speciesSearch: 'peregrinus',
        speciesName: 'thaumastocoris peregrinus',
    },
    {
        filename: 'trash.png',
        isRequestedSpecies: false,
        difficulty: 'Easy',
        speciesSearch: 'peregrinus',
        speciesName: 'thaumastocoris peregrinus',
    },
    {
        filename: 'perengrinus.jpg',
        isRequestedSpecies: true,
        difficulty: 'Easy',
        speciesSearch: 'peregrinus',
        speciesName: 'thaumastocoris peregrinus',
    }
];

test.describe('[E2E] R4 Manage Gold Images', () => {
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
        await expect(page.locator('text=Gold Images').first()).toBeVisible({ timeout: 15000 });
    });

    test('uploads and manages gold images successfully', async ({ page }) => {
        test.setTimeout(90000);
        await page.waitForTimeout(1000);

        // Open Gold Images Management
        await page.locator('text=Gold Images').first().click();
        await expect(page.locator('text=Gold Image Guidelines')).toBeVisible();

        for (const image of IMAGES) {
            console.log('Uploading image:', image.filename);
            
            // Click Add Image from Gold Images Management Screen
            await page.getByText('Add Image', { exact: true }).click();
            await expect(page.locator('text=How would you like to add the image?')).toBeVisible();

            // Upload Image
            const imagePath = path.join(__dirname, 'mock_images', image.filename);
            const fileChooserPromise = page.waitForEvent('filechooser');
            await page.locator('text=Browse & Select Image').click();
            const fileChooser = await fileChooserPromise;
            await fileChooser.setFiles(imagePath);

            await expect(page.locator('text=✓ Ready')).toBeVisible();

            // Select Species
            await page.getByPlaceholder('Search for species…').fill(image.speciesSearch);
            await expect(page.locator(`text="${image.speciesName}"`).first()).toBeVisible({ timeout: 10000 });
            await page.keyboard.press('Escape');
            await page.waitForTimeout(500);
            await page.locator(`text="${image.speciesName}"`).first().click();

            // Select Correct Answer
            if (image.isRequestedSpecies) {
                await page.getByText('Yes', { exact: true }).click();
            } else {
                await page.getByText('No', { exact: true }).click();
            }

            // Select Difficulty
            await page.getByText(image.difficulty, { exact: true }).click();

            // Save Image
            await page.locator('text=➔').click();

            // Assert success
            await expect(page.locator('text=Gold image created successfully! Redirecting…')).toBeVisible({ timeout: 15000 });

            // Assert success
            await expect(page.locator('text=Gold image created successfully! Redirecting…')).toBeVisible({ timeout: 15000 });

            // Navigate back to home and open Gold Images to verify state cleanly
            await page.goto(BASE_URL);
            await expect(page.locator('text=Gold Images').first()).toBeVisible({ timeout: 15000 });
            await page.locator('text=Gold Images').first().click();
            await expect(page.locator('text=Gold Image Guidelines').first()).toBeVisible({ timeout: 15000 });
        }

        // Final assertion: images should appear in the management list
        await expect(page.getByText('Answer: YES').first()).toBeVisible();
        await expect(page.getByText('Answer: NO').first()).toBeVisible();
    });
});
