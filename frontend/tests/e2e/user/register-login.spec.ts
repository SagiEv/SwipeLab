import { test, expect } from '@playwright/test';
import {
  gotoLogin,
  login,
  logout,
  assertOnUserHome,
  getStoredToken,
} from '../helpers';

/**
 * [E2E] U1 — Register & Login
 *
 * User story:
 *   As a regular user, I want to register and log in, so that I can access the platform.
 *
 * Runs only under the `e2e` profile (backend on :8080 with auto-verify-emails=true,
 * frontend via `npm run start:e2e`). Under that profile the register endpoint returns
 * a JWT immediately (the account is auto-approved/ACTIVE), so RegisterForm logs the
 * user straight into the home screen — there is no "Check Your Email" step.
 *
 * Note: the e2e backend uses a fresh `create-drop` DB on each startup, so the
 * generated handle below is unique-per-run only within a single backend lifetime.
 * Restart the backend between full re-runs to avoid "Username already taken".
 */

// Unique-ish handle for this run. Lowercase because the backend lowercases usernames.
const UNIQUE = `${Date.now()}`.slice(-9);
const USERNAME = `u1_${UNIQUE}`;
const EMAIL = `u1_${UNIQUE}@e2e.test`;
const DISPLAY_NAME = `U1 Tester ${UNIQUE}`;
// Satisfies the RegisterForm regex: upper, lower, digit, special, >=8, no spaces.
const PASSWORD = 'Passw0rd!';

test.describe('[E2E] U1 Register & Login', () => {
  test('registers a new user (auto-approved) and logs in to reach the home screen', async ({ page }) => {
    // 1. Navigate to the login screen.
    await gotoLogin(page);

    // 2. Open the Register form via the login-screen link (match its full text so we
    //    don't collide with the form's "Register" title/button).
    await page.locator("text=Don't have an account? Register").click();
    await expect(page.locator('input[placeholder="Confirm your password"]')).toBeVisible({ timeout: 10000 });

    // 3. Create a new account.
    await page.locator('input[placeholder="Username (e.g., jdoe23)"]').fill(USERNAME);
    await page.locator('input[placeholder="Email address"]').fill(EMAIL);
    await page.locator('input[placeholder="Display Name (e.g., John Doe)"]').fill(DISPLAY_NAME);
    await page.locator('input[placeholder="Create a strong password"]').fill(PASSWORD);
    await page.locator('input[placeholder="Confirm your password"]').fill(PASSWORD);
    // "Register" appears 3x: the login link, the form title, and the submit button.
    // The submit button is the last one and is the clickable (pointer) element.
    await page.locator('text=Register').last().click();

    // 4. Account is auto-approved → app auto-logs in and lands on the user home.
    //    Asserts: account created + login succeeded + home displayed.
    await assertOnUserHome(page);

    // ...and the JWT/session is stored.
    expect(await getStoredToken(page)).toBeTruthy();

    // 5. Prove an explicit login with the created credentials works.
    await logout(page);
    expect(await getStoredToken(page)).toBeNull();

    await login(page, USERNAME, PASSWORD);

    // 6. Login succeeds → home screen displayed again, session stored again.
    await assertOnUserHome(page);
    expect(await getStoredToken(page)).toBeTruthy();
  });
});
