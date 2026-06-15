import { test, expect } from '@playwright/test';
import { loginAsUser, gotoMyTasks } from '../helpers';

/**
 * [E2E] U2 — Browse Available Tasks
 *
 * User story:
 *   As a regular user, I want to browse available classification tasks, so that I
 *   can decide which tasks to participate in.
 *
 * Runs only under the `e2e` profile. The seeded public task is `explore_e2e_task`
 * ("Explore E2E Task", Birds, isPublic=true) — it is NOT assigned to e2e_user, so it
 * appears under the "Explore Tasks" section of UserMyTasksScreen (the assigned-task
 * filter keeps it there). See backend E2eDataSeeder.
 *
 * Screen anchors (verified, text-only selectors — no testIDs / no app changes):
 *   - Bottom bar "My Tasks" → UserMyTasksScreen.
 *   - "Explore Tasks" section header lists public TaskCards; each card's title is the
 *     task `name` (here: explore_e2e_task) with its description ("Identify birds...").
 *   - Tapping a card → TaskDetailsScreen, which has a "Task Details" header,
 *     "Classification Progress", and a "Back" button (navigation.goBack()).
 */

// The card/details surface the task's display `title`, not its internal `name`
// (seeder sets title="Explore E2E Task", name="explore_e2e_task").
const PUBLIC_TASK = 'Explore E2E Task';
const PUBLIC_TASK_DESCRIPTION = 'Identify birds in these e2e images';

test.describe('[E2E] U2 Browse Available Tasks', () => {
  test('lists public tasks, opens task details, and returns to the list', async ({ page }) => {
    // 1. Login as a regular user.
    await loginAsUser(page);

    // 2. Navigate to the Explore Tasks list (My Tasks screen).
    await gotoMyTasks(page);

    // 3. Load available public tasks → assert the seeded public task is displayed
    //    with its metadata (title + description).
    // NOTE: react-native-web keeps prior screens mounted but hidden, so several nodes
    // can match the title text. Target only the VISIBLE one (otherwise .first() may
    // resolve to a hidden leftover node and fail the visibility check).
    const taskCard = page.getByText(PUBLIC_TASK, { exact: true }).locator('visible=true').first();
    await expect(taskCard).toBeVisible({ timeout: 20000 });
    await expect(page.getByText(PUBLIC_TASK_DESCRIPTION).locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // 4. Open task details → assert the details page loads successfully.
    await taskCard.click();
    // "Task Details" header + "Classification Progress" are unique to the details
    // screen, so they confirm it loaded. (The task title also appears here, but the
    // now-hidden My Tasks screen stays mounted in the DOM, so asserting on the title
    // would ambiguously match the hidden card — these two markers are unambiguous.)
    await expect(page.locator('text=Task Details')).toBeVisible({ timeout: 20000 });
    await expect(page.locator('text=Classification Progress')).toBeVisible({ timeout: 10000 });

    // 5. Return to the task list → assert navigation works (back on the Explore list).
    // "Explore Tasks" can match more than one node, so scope to the first visible one.
    await page.locator('text=Back').first().click();
    await expect(page.getByText('Explore Tasks').locator('visible=true').first()).toBeVisible({ timeout: 20000 });
    await expect(page.getByText(PUBLIC_TASK, { exact: true }).locator('visible=true').first()).toBeVisible({ timeout: 10000 });
  });
});
