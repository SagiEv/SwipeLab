import { test, expect, Page } from '@playwright/test';
import { loginAsUser } from '../helpers';

/**
 * [E2E] U4 — Swipe / Classify Images
 *
 * User story:
 *   As a regular user, I want to classify images, so that I can contribute data to a
 *   task.
 *
 * Mechanics (verified in SwipeScreen.tsx / SwipeCard.tsx / SwipeButtons.tsx):
 *   - The seeded assigned task "E2E Identification Task" (id 1) has a swipeable image
 *     batch (question "Is this a Cat?"). The Home tab (SwipeLab) shows a "Quick Start"
 *     list of assigned tasks; tapping one starts the classification session.
 *   - Each swipe submits ONE classification immediately (POST /classifications/submit)
 *     — there is no separate "Submit" button — then advances to the next image.
 *   - The four actions are the SwipeButtons (icon-only). We click them directly via
 *     their structural position in the SwipeButtons layout (don't-know alone on top,
 *     No + Yes in the middle row, trash alone on the bottom). Keyboard arrows also
 *     work for left/right, but ArrowUp/ArrowDown are unreliable in-browser (scroll),
 *     so clicking the buttons exercises all four actions consistently.
 *   - The card re-renders per image (key={imageId}); each seeded image is distinct
 *     base64, so a changed <img> src is a reliable "advanced to next image" signal.
 *
 * Assertions: batch loads, each classification action is accepted (card advances =
 * submit succeeded), images advance, and no error state appears.
 */

const ASSIGNED_TASK = 'E2E Identification Task';
const QUESTION = 'Is this a Cat?';

// The four classification actions, in order, across successive cards.
const ACTIONS = ['YES', 'NO', 'DONT_KNOW', 'TRASH'] as const;
type Action = (typeof ACTIONS)[number];

test.describe('[E2E] U4 Swipe / Classify Images', () => {
  test('classifies images with YES/NO/DONT_KNOW/TRASH and advances each time', async ({ page }) => {
    // 1. Login as a regular user (lands on Home / SwipeLab).
    await loginAsUser(page);

    // 2. Open an assigned task from Quick Start → 3. start the classification session.
    await page.getByText(ASSIGNED_TASK, { exact: true }).locator('visible=true').first().click();

    // Image batch loads successfully: the swipe card (question + image) appears.
    await expect(page.getByText(QUESTION).locator('visible=true').first()).toBeVisible({ timeout: 20000 });
    await expect.poll(() => currentImageSrc(page), { timeout: 20000 }).not.toBeNull();

    // 4 + 5. Swipe each action; after each, the image must advance (submit accepted).
    for (const action of ACTIONS) {
      const before = await currentImageSrc(page);
      expect(before, `expected an image before swiping ${action}`).not.toBeNull();

      // Click the action, then wait for the card to advance. The SwipeCard plays a
      // ~250ms animation before submitting, and an occasional first click can be
      // dropped while the card is settling — so retry the click a few times until the
      // image changes. Advancement = the classification was submitted and accepted.
      await advanceWithAction(page, action, before);

      // We never landed on the error state.
      await expect(page.locator('text=Something went wrong')).toHaveCount(0);
    }

    // Submission succeeded throughout: still in a valid swipe/batch state (a card is
    // shown, or the batch completed cleanly) — not an error.
    await expect(page.locator('text=Something went wrong')).toHaveCount(0);
  });
});

/**
 * Click `action` and wait until the swipe card advances to a different image. Retries
 * the click a few times to absorb a dropped first press while the card is animating.
 */
async function advanceWithAction(page: Page, action: Action, before: string | null): Promise<void> {
  const MAX_ATTEMPTS = 4;
  for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
    await clickSwipeButton(page, action);
    try {
      await expect
        .poll(() => currentImageSrc(page), { timeout: 6000, intervals: [200, 400, 800] })
        .not.toBe(before);
      return; // advanced
    } catch {
      if (attempt === MAX_ATTEMPTS) {
        throw new Error(`image did not advance after ${action} (${MAX_ATTEMPTS} attempts)`);
      }
      // else: re-click on the next loop iteration
    }
  }
}

/**
 * Click one of the four SwipeButtons. They are icon-only TouchableOpacity nodes, but
 * each has a distinct background colour (see SwipeButtons.tsx styles), which we use to
 * find and tag the right one, then issue a real Playwright click.
 *   YES = #10B981 (green), NO = #EF4444 (red), DONT_KNOW = #FBBF24 (amber),
 *   TRASH = #9CA3AF (grey).
 */
async function clickSwipeButton(page: Page, action: Action): Promise<void> {
  const COLORS: Record<Action, string> = {
    YES: 'rgb(16, 185, 129)',
    NO: 'rgb(239, 68, 68)',
    DONT_KNOW: 'rgb(251, 191, 36)',
    TRASH: 'rgb(156, 163, 175)',
  };
  const TAG = '__e2e_swipe_btn__';
  const target = COLORS[action];
  const tagged = await page.evaluate(
    ({ target, TAG }) => {
      const el = Array.from(document.querySelectorAll('div')).find((d) => {
        const bg = getComputedStyle(d as HTMLElement).backgroundColor;
        return bg === target && (d as HTMLElement).offsetParent !== null;
      }) as HTMLElement | undefined;
      if (!el) return false;
      el.setAttribute('data-e2e', TAG);
      return true;
    },
    { target, TAG },
  );
  if (!tagged) throw new Error(`Could not find the ${action} swipe button`);
  await page.locator(`[data-e2e="${TAG}"]`).click();
  // Remove the tag so the next action re-resolves against the fresh card.
  await page.evaluate((TAG) => {
    document.querySelector(`[data-e2e="${TAG}"]`)?.removeAttribute('data-e2e');
  }, TAG);
}

/**
 * Returns the src of the currently displayed swipe image (the visible <img> inside the
 * card), or null if none is shown yet. Used to detect card advancement.
 */
async function currentImageSrc(page: Page): Promise<string | null> {
  return page.evaluate(() => {
    const imgs = Array.from(document.querySelectorAll('img')).filter(
      (img) => (img as HTMLElement).offsetParent !== null && img.getAttribute('src'),
    );
    // The swipe card image is a large, visible data/URL image. Prefer the biggest one.
    if (imgs.length === 0) return null;
    let best: HTMLImageElement | null = null;
    let bestArea = -1;
    for (const img of imgs) {
      const r = img.getBoundingClientRect();
      const area = r.width * r.height;
      if (area > bestArea) {
        bestArea = area;
        best = img;
      }
    }
    return best ? best.getAttribute('src') : null;
  });
}
