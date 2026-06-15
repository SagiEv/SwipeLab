import { test, expect, Page } from '@playwright/test';
import { loginAsUser, gotoChallenges } from '../helpers';

/**
 * [E2E] U8 — Gamification
 *
 * User story:
 *   As a regular user, I want to view my gamification progress, so that I can track
 *   achievements and rankings.
 *
 * The story's assertions span more than one screen. What each is backed by:
 *   - XP            → the always-visible UserTopBar "Score: N" chip (score == XP).
 *   - Streak        → the UserTopBar "N days streak" line.
 *   - Leaderboard   → LeaderboardScreen: "🏆 Greatest Of All Time" + "🔥 Greatest Of
 *                     The Month" tables (seeded entries: e2e_user, admin_e2e).
 *   - User ranking  → header "Your Spot: #N" + the RankBadge tier pill ("Your Rank").
 *   - Earned badges → the LeaderboardScreen shows the user's rank tier (the achievement
 *                     indicator). There is no list-of-badges screen in the user app, so
 *                     the tier pill is the closest faithful "achievement" surface.
 *   - Challenge progress → lives on the ChallengesScreen (cross-screen step below):
 *                     each challenge card shows "Progression: X%" / "Completed: n/target".
 *
 * Verified in LeaderboardScreen.tsx, UserTopBar.tsx, ChallengesScreen.tsx + the
 * E2eDataSeeder gamification/leaderboard seed.
 */

test.describe('[E2E] U8 Gamification', () => {
  test('shows XP, streak, leaderboard, ranking, and challenge progress', async ({ page }) => {
    // 1. Login as a regular user (TopBar with XP + streak is now visible).
    await loginAsUser(page);

    // XP is displayed (TopBar "Score: N").
    await expect(page.getByText(/^Score:\s*[\d,]+/).locator('visible=true').first()).toBeVisible({ timeout: 20000 });
    // Streak information is displayed (TopBar "N days streak").
    await expect(page.getByText(/\d+\s*days streak/).locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // 2. Navigate to Leaderboard (bottom-bar tab).
    await page.locator('text=Leaderboard').first().click();

    // Leaderboard loads successfully: both seeded tables render.
    await expect(page.getByText('Greatest Of All Time').locator('visible=true').first()).toBeVisible({ timeout: 20000 });
    await expect(page.getByText('Greatest Of The Month').locator('visible=true').first()).toBeVisible({ timeout: 10000 });
    // Table column headers + at least one ranked row.
    await expect(page.getByText('Score', { exact: true }).locator('visible=true').first()).toBeVisible({ timeout: 10000 });
    await expect.poll(() => leaderboardRowCount(page), { timeout: 10000 }).toBeGreaterThan(0);

    // User ranking is visible: "Your Spot: #N" header + the RankBadge tier ("Your Rank").
    await expect(page.getByText(/Your Spot:\s*#\d+/).locator('visible=true').first()).toBeVisible({ timeout: 10000 });
    await expect(page.getByText('Your Rank').locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // Earned achievement indicator: the rank tier pill (UNRANKED/BEGINNER/EXPERT/...).
    await expect(page.getByText(/^(UNRANKED|BEGINNER|EXPERT|PRO|LEGEND)$/).locator('visible=true').first())
      .toBeVisible({ timeout: 10000 });

    // 3 + cross-screen: challenge progress is shown on the Challenges screen.
    await gotoChallenges(page);
    await expect.poll(() => challengeCardCount(page), { timeout: 20000 }).toBeGreaterThan(0);
    await expect(page.getByText(/Progression:\s*\d+%/).locator('visible=true').first()).toBeVisible({ timeout: 10000 });
    await expect(page.getByText(/Completed:\s*\d+\/\d+/).locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // Nothing errored across the flow.
    await expect(page.locator('text=Something went wrong')).toHaveCount(0);
  });
});

/** Counts visible leaderboard data rows by their score cells (a number per row). */
async function leaderboardRowCount(page: Page): Promise<number> {
  // The user column shows seeded usernames; count visible rows by the seeded users.
  return page.evaluate(() => {
    const names = ['e2e_user', 'admin_e2e', 'reviewer_e2e', 'You'];
    return Array.from(document.querySelectorAll('div')).filter((d) => {
      const t = (d.textContent || '').trim();
      return names.includes(t) && (d as HTMLElement).offsetParent !== null;
    }).length;
  });
}

/** Counts visible challenge cards by their unique "Progression: X%" line. */
async function challengeCardCount(page: Page): Promise<number> {
  return page.evaluate(() => {
    return Array.from(document.querySelectorAll('div')).filter(
      (d) => /^Progression:\s*\d+%$/.test((d.textContent || '').trim()) && (d as HTMLElement).offsetParent !== null,
    ).length;
  });
}
