import { test, expect, Page } from '@playwright/test';
import { loginAsUser } from '../helpers';

/**
 * [E2E] U6 — View Statistics
 *
 * User story:
 *   As a regular user, I want to view my statistics, so that I can monitor my
 *   performance.
 *
 * Mechanics (verified in StatsScreen.tsx + E2eDataSeeder analytics seed):
 *   - Reached via the bottom-bar "Stats" tab → StatsScreen (header "Stats").
 *   - useAllStatistics() loads /statistics/me, /vs-experts, /vs-users, /breakdown,
 *     and /gamification/user-info. The seeder pre-populates analytics for e2e_user
 *     (rankings, daily stats, species stats), so the dashboard has data to show.
 *   - Sections rendered:
 *       • Summary grid — incl. an "Accuracy" card (value like "95.8%", subtext "Overall").
 *       • "⚖️ Comparisons" → "Vs Experts" (Your Accuracy / Expert Benchmark progress
 *         bars) and "Vs Community" (Average User progress bar).
 *       • "📝 Species Breakdown" — per-category accuracy rows.
 *   - There are no canvas charts; the "charts" are ProgressBar components. So
 *     "charts render" == the comparison progress bars (labels + % values) are visible.
 */

test.describe('[E2E] U6 View Statistics', () => {
  test('shows accuracy, breakdown, vs-experts and vs-users with rendered charts', async ({ page }) => {
    // 1. Login as a regular user.
    await loginAsUser(page);

    // 2. Navigate to Statistics via the bottom-bar "Stats" tab.
    await page.locator('text=Stats').first().click();

    // 3. Load statistics dashboard — wait for the Comparisons section to render
    //    (it depends on the vs-experts/vs-users fetches completing).
    await expect(page.getByText('Comparisons').locator('visible=true').first()).toBeVisible({ timeout: 20000 });

    // Accuracy metric is displayed (the "Accuracy" summary card, value as a percentage).
    await expect(page.getByText('Accuracy', { exact: true }).locator('visible=true').first()).toBeVisible({ timeout: 10000 });
    await expect.poll(() => percentValuesCount(page), { timeout: 10000 }).toBeGreaterThan(0);

    // Vs-experts statistics are displayed.
    await expect(page.getByText('Vs Experts').locator('visible=true').first()).toBeVisible({ timeout: 10000 });
    await expect(page.getByText('Your Accuracy').locator('visible=true').first()).toBeVisible({ timeout: 10000 });
    await expect(page.getByText('Expert Benchmark').locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // Vs-users (community) statistics are displayed.
    await expect(page.getByText('Vs Community').locator('visible=true').first()).toBeVisible({ timeout: 10000 });
    await expect(page.getByText('Average User').locator('visible=true').first()).toBeVisible({ timeout: 10000 });
    await expect(page.getByText(/top \d+% of contributors/).locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // Classification breakdown is displayed (the Species Breakdown section).
    await expect(page.getByText('Species Breakdown').locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // Charts render successfully: the comparison ProgressBars are present, each showing
    // a "NN.N%" value next to its label. We require several percentage readouts (the
    // summary Accuracy card + the comparison bars).
    await expect.poll(() => percentValuesCount(page), { timeout: 10000 }).toBeGreaterThanOrEqual(3);

    // The dashboard never errored out.
    await expect(page.locator('text=Something went wrong')).toHaveCount(0);
  });
});

/** Counts visible elements whose text is a percentage value like "95.8%" or "100%". */
async function percentValuesCount(page: Page): Promise<number> {
  return page.evaluate(() => {
    return Array.from(document.querySelectorAll('div')).filter(
      (d) => /^\d+(\.\d+)?%$/.test((d.textContent || '').trim()) && (d as HTMLElement).offsetParent !== null,
    ).length;
  });
}
