import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright config for the E2E user-story suite.
 *
 * These tests run ONLY against the `e2e` profile:
 *   - Backend: started by the operator with the Spring `e2e` profile on :8080
 *     (`./mvnw spring-boot:run -Dspring-boot.run.profiles=e2e`) — see E2E_GUIDE.md.
 *     It seeds deterministic data and mocks StarDBI, against the isolated
 *     `swipelab_e2e` database.
 *   - Frontend: this config's webServer runs `npm run start:e2e`, which sets
 *     EXPO_PUBLIC_API_URL=http://localhost:8080 so the app talks to the e2e backend.
 *
 * It is intentionally separate from `playwright.config.ts` (which runs plain
 * `npm start`) so the user-story specs under tests/e2e stay isolated from the
 * legacy specs in tests/*.spec.ts.
 *
 * Run with: npm run test:e2e:stories
 */
export default defineConfig({
  testDir: './tests/e2e',
  // These tests drive ONE shared backend and mutate shared state (ban/unban, fraud
  // strikes, task assignment, classifications). They MUST run serially — parallel
  // workers stomp on each other's state (e.g. one test bans e2e_user while another
  // is mid-flow). Single worker, no intra-file parallelism.
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  reporter: 'html',
  use: {
    baseURL: 'http://localhost:8081',
    trace: 'on-first-retry',
  },
  projects: [
    {
      name: 'chromium',
      use: {
        ...devices['Desktop Chrome'],
        // PWSLOWMO (ms) adds a visible pause before each action so a headed run
        // can be followed by eye. Unset (0) for normal/headless runs. Used by the
        // `test:e2e:stories:watch` script.
        launchOptions: { slowMo: Number(process.env.PWSLOWMO) || 0 },
      },
    },
  ],
  webServer: {
    // start:e2e points the frontend at the e2e backend (localhost:8080).
    command: 'npm run start:e2e',
    url: 'http://localhost:8081',
    reuseExistingServer: !process.env.CI,
    timeout: 120 * 1000,
  },
});
