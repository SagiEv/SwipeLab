import { test, expect, Page } from '@playwright/test';
import { loginAsUser } from '../helpers';

/**
 * [E2E] U9 — Species Reference Images
 *
 * User story:
 *   As a regular user, I want to view species reference images while classifying, so
 *   that I can make more accurate decisions.
 *
 * Mechanics (verified in SwipeScreen.tsx + SwipeButtons.tsx + ReferenceGallery.tsx):
 *   - Start a session like U4: Home Quick Start → tap the assigned "E2E Identification
 *     Task" → a swipe card appears (question "Is this a Cat?").
 *   - The swipe controls include a "Show References" button. Clicking it swaps the
 *     buttons for the ReferenceGallery panel (the story's "species information modal").
 *   - The gallery shows a "Hide References" button and a horizontal strip of reference
 *     images. SwipeScreen always passes at least a placeholder image when a swipe item
 *     has no real reference images, so an <img> always renders here.
 *   - "Hide References" closes the gallery and restores the swipe buttons, after which
 *     the user can keep classifying.
 *
 * Notes on the story's assertions:
 *   - "Species metadata is displayed" → the species question ("Is this a Cat?") on the
 *     SwipeCard is the species context shown; the gallery itself renders no separate
 *     metadata text, so we assert the question stays visible.
 *   - The icon-only swipe buttons are clicked by their background colour (see U4).
 */

const ASSIGNED_TASK = 'E2E Identification Task';
const QUESTION = 'Is this a Cat?';

test.describe('[E2E] U9 Species Reference Images', () => {
  test('opens references while classifying, views images, closes, and continues', async ({ page }) => {
    // 1. Login. 2. Open the active task. 3. Load an image for classification.
    await loginAsUser(page);
    await page.getByText(ASSIGNED_TASK, { exact: true }).locator('visible=true').first().click();
    await expect(page.getByText(QUESTION).locator('visible=true').first()).toBeVisible({ timeout: 20000 });
    await expect(page.getByText('Show References').locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // 4. Open the species information modal (the reference gallery).
    await page.getByText('Show References').locator('visible=true').first().click();

    // Information modal opens successfully: the gallery's "Hide References" control shows.
    await expect(page.getByText('Hide References').locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // 5. Reference images load: at least one image is rendered in the gallery, and it
    //    is NOT the "no reference images" empty text.
    await expect.poll(() => visibleImageCount(page), { timeout: 10000 }).toBeGreaterThan(0);
    await expect(page.locator('text=No reference images available')).toHaveCount(0);

    // Species metadata is displayed: the species question stays visible alongside it.
    await expect(page.getByText(QUESTION).locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // Modal can be closed: "Hide References" closes the gallery, swipe buttons return.
    await page.getByText('Hide References').locator('visible=true').first().click();
    await expect(page.getByText('Show References').locator('visible=true').first()).toBeVisible({ timeout: 10000 });

    // User can continue classification: one swipe must advance to the next image.
    const before = await currentImageSrc(page);
    await clickSwipeButton(page, 'YES');
    await expect
      .poll(() => currentImageSrc(page), { timeout: 20000, message: 'image did not advance after continuing' })
      .not.toBe(before);

    await expect(page.locator('text=Something went wrong')).toHaveCount(0);
  });
});

/** Counts visible <img> elements (reference thumbnails / card image). */
async function visibleImageCount(page: Page): Promise<number> {
  return page.evaluate(() => {
    return Array.from(document.querySelectorAll('img')).filter(
      (img) => (img as HTMLElement).offsetParent !== null && img.getAttribute('src'),
    ).length;
  });
}

/** Src of the largest visible image (the swipe card image), to detect advancement. */
async function currentImageSrc(page: Page): Promise<string | null> {
  return page.evaluate(() => {
    const imgs = Array.from(document.querySelectorAll('img')).filter(
      (img) => (img as HTMLElement).offsetParent !== null && img.getAttribute('src'),
    );
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

/**
 * Click a SwipeButton by its distinct background colour (icon-only buttons).
 * YES=#10B981, NO=#EF4444, DONT_KNOW=#FBBF24, TRASH=#9CA3AF. (Same approach as U4.)
 */
async function clickSwipeButton(page: Page, action: 'YES' | 'NO' | 'DONT_KNOW' | 'TRASH'): Promise<void> {
  const COLORS = {
    YES: 'rgb(16, 185, 129)',
    NO: 'rgb(239, 68, 68)',
    DONT_KNOW: 'rgb(251, 191, 36)',
    TRASH: 'rgb(156, 163, 175)',
  } as const;
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
  await page.evaluate((TAG) => {
    document.querySelector(`[data-e2e="${TAG}"]`)?.removeAttribute('data-e2e');
  }, TAG);
}
