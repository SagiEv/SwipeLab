import { test, expect, Page } from '@playwright/test';
import { loginAsUser, gotoChallenges } from '../helpers';

/**
 * [E2E] U5 — View Challenges
 *
 * User story:
 *   As a regular user, I want to view and complete challenges, so that I can earn
 *   rewards and badges.
 *
 * Mechanics (verified in ChallengesScreen.tsx + E2eDataSeeder):
 *   - Challenges are reached from the UserTopBar "Score:" stats block (not the bottom
 *     bar). The screen shows a "Challenges" header and two sections: "In Progress" and
 *     "Completed" (both always rendered).
 *   - Each ChallengeCard shows: name, description, "Progression: X%",
 *     "Completed: progress/target", a progress bar, and a badge icon (or 🏆 fallback).
 *   - The seeder defines challenges: "Classify 1 image", "Classify 20 images today",
 *     "Reach 500 total classifications" (returned by GET /gamification/challenges).
 *   - "Refresh" is a pull-to-refresh (RefreshControl → refetch). Pull gestures aren't
 *     available in web Playwright, so we refresh by re-navigating to the screen, which
 *     re-runs the challenges query — and assert the data still renders correctly.
 *
 * Note on "complete challenge / rewards / XP": the screen has no in-page action to
 * "complete" a challenge (completion is a backend side-effect of classifying), and it
 * shows no XP counter. So this test asserts what the screen actually provides:
 * challenges are displayed with progress indicators, the Completed section exists, and
 * (if any completed challenge is present) it renders with its progress/badge.
 */

test.describe('[E2E] U5 View Challenges', () => {
  test('displays active challenges with progress indicators and refreshes', async ({ page }) => {
    // 1. Login as a regular user.
    await loginAsUser(page);

    // 2. Navigate to Challenges.
    await gotoChallenges(page);

    // 3. View active challenges → assert the screen and both sections are displayed.
    await expect(page.getByText('In Progress').locator('visible=true').first()).toBeVisible({ timeout: 20000 });
    await expect(page.getByText('Completed', { exact: true }).locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // At least one seeded challenge card is shown with its name.
    await expect.poll(() => challengeCardCount(page), { timeout: 20000 }).toBeGreaterThan(0);

    // Progress indicators are visible: each card shows "Progression: X%" and a
    // "Completed: n/target" line.
    await expect(page.getByText(/Progression:\s*\d+%/).locator('visible=true').first()).toBeVisible({ timeout: 10000 });
    await expect(page.getByText(/Completed:\s*\d+\/\d+/).locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    const countBefore = await challengeCardCount(page);

    // 4 + 5. Refresh challenge status: re-navigate so the challenges query re-runs,
    // then assert the challenges still render (status reloaded successfully).
    await page.locator('text=My Tasks').first().click(); // leave the screen
    await expect(page.getByText('Explore Tasks').locator('visible=true').first()).toBeVisible({ timeout: 20000 });
    await gotoChallenges(page); // return → triggers a fresh challenges fetch

    await expect.poll(() => challengeCardCount(page), { timeout: 20000 }).toBe(countBefore);
    await expect(page.getByText(/Progression:\s*\d+%/).locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // Rewards/badges: if any challenge is in the Completed section, it renders as a
    // card too (with its progress reading 100% / target met). This is "if applicable"
    // in the story; we assert the Completed section is present (done above) and that
    // the page never errored.
    await expect(page.locator('text=Something went wrong')).toHaveCount(0);
  });
});

/**
 * Counts visible challenge cards by their unique "Progression: X%" line (one per card).
 */
async function challengeCardCount(page: Page): Promise<number> {
  return page.evaluate(() => {
    return Array.from(document.querySelectorAll('div')).filter(
      (d) => /^Progression:\s*\d+%$/.test((d.textContent || '').trim()) && (d as HTMLElement).offsetParent !== null,
    ).length;
  });
}
