import { test, expect, Page } from '@playwright/test';
import { loginAsUser, gotoMyTasks } from '../helpers';

/**
 * [E2E] U3 — Join a Task
 *
 * User story:
 *   As a regular user, I want to join a public task, so that I can start contributing
 *   classifications.
 *
 * Mechanics (verified in UserMyTasksScreen.tsx + useAssignTask in queries.ts):
 *   - Public task cards live under the "Explore Tasks" section with an icon-only
 *     "add" (add-circle) button. Tapping it POSTs /tasks/{id}/assign.
 *   - On success there is NO toast; the feedback is that the task MOVES from
 *     "Explore Tasks" into "Assigned Tasks" (caches refetch, and Explore filters out
 *     assigned tasks) and the "Assigned" counter increments. (ErrorToast shows only
 *     on failure.) So we assert success via that observable outcome.
 *
 * State note: joining permanently assigns the seeded public task ("Explore E2E Task")
 * to e2e_user. The e2e DB is create-drop, so a fresh backend restart resets it. To
 * stay re-runnable on the SAME backend, the test skips the click if the task is
 * already assigned and just verifies the joined end-state.
 */

const PUBLIC_TASK = 'Explore E2E Task';

test.describe('[E2E] U3 Join a Task', () => {
  test('joins a public task so it moves into My Tasks (assigned)', async ({ page }) => {
    // 1. Login as a regular user. 2. Open Explore Tasks (the My Tasks screen).
    await loginAsUser(page);
    await gotoMyTasks(page);

    // Wait for the task to be rendered somewhere visible (Explore or Assigned).
    // NOTE: getByText(...).first() is unreliable here — react-native-web keeps prior
    // screens mounted but hidden, so .first() can resolve to a hidden node. We check
    // for a VISIBLE occurrence (offsetParent !== null) instead.
    await expect.poll(() => isVisibleSomewhere(page, PUBLIC_TASK), { timeout: 20000 }).toBe(true);

    const assignedCountBefore = await assignedCount(page);
    const inExploreBefore = await isInExplore(page, PUBLIC_TASK);

    if (inExploreBefore) {
      // 3. Select + 4. join the public task via its add-circle button.
      // The card and its inner controls are react-native-web TouchableOpacity nodes
      // rendered as <div role="button"> with no text on the icon button. Walk from the
      // title to its header-row sibling (the add-circle) and click that element — NOT
      // the card body, which would navigate to Task Details instead of joining.
      await clickJoinButton(page, PUBLIC_TASK);

      // 5. The task is refetched into "Assigned Tasks".
      // Success: it leaves Explore and the Assigned counter increments by one.
      await expect.poll(() => isInExplore(page, PUBLIC_TASK), { timeout: 20000 }).toBe(false);
      await expect.poll(() => assignedCount(page), { timeout: 20000 }).toBe(assignedCountBefore + 1);
    }

    // Assertions (hold whether we just joined or it was already assigned):
    // the user is assigned to the task → it appears under "Assigned Tasks" and is no
    // longer offered under "Explore Tasks".
    await expect.poll(() => isUnderAssigned(page, PUBLIC_TASK), { timeout: 20000 }).toBe(true);
    await expect.poll(() => isInExplore(page, PUBLIC_TASK), { timeout: 20000 }).toBe(false);
  });
});

/** True if any visible element's exact text equals `title`. */
async function isVisibleSomewhere(page: Page, title: string): Promise<boolean> {
  return page.evaluate((title) => {
    return Array.from(document.querySelectorAll('div')).some(
      (d) => (d.textContent || '').trim() === title && (d as HTMLElement).offsetParent !== null,
    );
  }, title);
}

/**
 * Click the icon-only "add" (join) button on the public task card.
 *
 * The card is the clickable ancestor that contains the title text; its header row
 * holds the title plus the add-circle icon button (a sibling, with no text). We tag
 * that icon element in the DOM, then issue a real Playwright click so it runs through
 * react-native-web's touch handlers (rather than a synthetic evaluate() dispatch).
 */
async function clickJoinButton(page: Page, title: string): Promise<void> {
  const TAG = '__e2e_join_btn__';
  const tagged = await page.evaluate(
    ({ title, TAG }) => {
      // Find the visible title element.
      const titleEl = Array.from(document.querySelectorAll('div')).find(
        (d) => (d.textContent || '').trim() === title && (d as HTMLElement).offsetParent !== null,
      ) as HTMLElement | undefined;
      if (!titleEl) return false;
      // Header row = title's parent; the add-circle button is the title's sibling.
      const headerRow = titleEl.parentElement;
      if (!headerRow) return false;
      const siblings = Array.from(headerRow.children) as HTMLElement[];
      const iconBtn = siblings.find((el) => el !== titleEl);
      if (!iconBtn) return false;
      iconBtn.setAttribute('data-e2e', TAG);
      return true;
    },
    { title, TAG },
  );
  if (!tagged) throw new Error(`Could not locate the join button for "${title}"`);
  await page.locator(`[data-e2e="${TAG}"]`).click();
}

/**
 * True if `title` currently sits under the "Assigned Tasks" section — i.e. between the
 * "Assigned Tasks" header and the "Explore Tasks" header in the shared scroll view.
 */
async function isUnderAssigned(page: Page, title: string): Promise<boolean> {
  return page.evaluate((title) => {
    const visibleTop = (label: string): number | null => {
      const el = Array.from(document.querySelectorAll('div')).find(
        (d) => (d.textContent || '').trim() === label && (d as HTMLElement).offsetParent !== null,
      ) as HTMLElement | undefined;
      return el ? el.getBoundingClientRect().top : null;
    };
    const assignedTop = visibleTop('Assigned Tasks');
    const exploreTop = visibleTop('Explore Tasks');
    if (assignedTop == null) return false;
    const upperBound = exploreTop == null ? Number.POSITIVE_INFINITY : exploreTop;
    const titleEls = Array.from(document.querySelectorAll('div')).filter(
      (d) => (d.textContent || '').trim() === title && (d as HTMLElement).offsetParent !== null,
    ) as HTMLElement[];
    return titleEls.some((el) => {
      const t = el.getBoundingClientRect().top;
      return t > assignedTop && t < upperBound;
    });
  }, title);
}

/** Read the numeric value of the "Assigned" summary stat chip. */
async function assignedCount(page: Page): Promise<number> {
  return page.evaluate(() => {
    const labels = Array.from(document.querySelectorAll('div'));
    const labelEl = labels.find((d) => (d.textContent || '').trim() === 'Assigned');
    // The StatChip renders value then label as siblings; the value is the previous
    // sibling text node. Walk up to the chip and find a numeric text inside it.
    const chip = labelEl?.parentElement;
    if (!chip) return -1;
    const nums = Array.from(chip.querySelectorAll('div'))
      .map((d) => (d.textContent || '').trim())
      .filter((t) => /^\d+$/.test(t));
    return nums.length ? Number(nums[0]) : -1;
  });
}

/**
 * True if `title` currently sits under the "Explore Tasks" section (i.e. is still a
 * joinable public task), by comparing vertical positions of the section header and
 * the visible task title within the shared scroll view.
 */
async function isInExplore(page: Page, title: string): Promise<boolean> {
  return page.evaluate((title) => {
    const visibleTop = (label: string): number | null => {
      const el = Array.from(document.querySelectorAll('div')).find(
        (d) => (d.textContent || '').trim() === label && (d as HTMLElement).offsetParent !== null,
      ) as HTMLElement | undefined;
      return el ? el.getBoundingClientRect().top : null;
    };
    const exploreTop = visibleTop('Explore Tasks');
    if (exploreTop == null) return false;
    // Title element(s) that exactly match and are visible.
    const titleEls = Array.from(document.querySelectorAll('div')).filter(
      (d) => (d.textContent || '').trim() === title && (d as HTMLElement).offsetParent !== null,
    ) as HTMLElement[];
    // Any matching title positioned BELOW the "Explore Tasks" header → it's in Explore.
    return titleEls.some((el) => el.getBoundingClientRect().top > exploreTop);
  }, title);
}
