# E2E User-Story Tests

End-to-end tests (one per user story) that drive the real app: browser → Expo web
frontend → Spring backend → Postgres. They run **only against the `e2e` profile**.

```
tests/e2e/
  helpers.ts                 # shared login / navigation helpers (text-only selectors)
  user/                      # regular-user user stories
  admin/                     # super-admin user stories
  researcher/                # researcher user stories
```

## Prerequisites (start these first)

1. **Postgres** running locally with a `swipelab_e2e` database
   (`CREATE DATABASE swipelab_e2e;` once).
2. **Backend** in the `e2e` profile, on `:8080` — the operator starts this:
   ```
   cd backend
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=e2e
   ```
   Wait for `✅ E2E Data Seeding complete!`. This seeds users/tasks and mocks StarDBI.

The **frontend** is started automatically by Playwright's `webServer` (`npm run start:e2e`,
which points `EXPO_PUBLIC_API_URL` at the `:8080` e2e backend).

## Running

```
npm run test:e2e:stories          # headless (normal)
npm run test:e2e:stories:watch    # headed + slowMo, watch a real browser
npx playwright test --config=playwright.e2e.config.ts tests/e2e/user/<file>.spec.ts  # one file
npx playwright show-report        # open the last HTML report
```

## ⚠️ Test state & isolation — READ THIS

These tests hit a **real backend + real DB**, so writes persist. There is **no per-test
teardown** (the app exposes no "unassign task" / "delete user" endpoints, and we do not
modify system code). The intended reset mechanism is the e2e profile's
`ddl-auto: create-drop`: **restarting the backend wipes and re-seeds the database.**

**The suite runs serially** (`playwright.e2e.config.ts` sets `workers: 1`,
`fullyParallel: false`). These tests drive ONE shared backend and mutate shared state, so
parallel workers would stomp on each other (e.g. one test bans a user while another is
mid-flow). Do not re-enable parallelism for this config.

Consequence: **restart the backend before a full-suite run** (`npm run test:e2e:stories`).
Some stories mutate shared seeded data that others depend on. Concretely:

- **U3 Join a Task** assigns the seeded public task *"Explore E2E Task"* to `e2e_user`,
  removing it from the Explore list. Specs run alphabetically, so **U2 Browse** (browse-tasks)
  runs before **U3** (join-task) and still sees it. On a re-run without a backend restart,
  U2 fails — restart to reset.

- **U4 Swipe / Classify Images** submits many fast classifications as `e2e_user`; repeated
  runs can lock `e2e_user` via fraud detection. Restart the backend for a fresh, unlocked user.

- **A3 Notifications** does NOT use `e2e_user` for its fraud trigger — it registers a fresh
  throwaway user each run and trips fraud on them, so it leaves `e2e_user` usable for the
  user specs. (Earlier it banned `e2e_user`, which poisoned U4/U7/U9; that's fixed.)

- **R8 Pause / Archive Task** (a researcher spec from `main`, under `tests/e2e/researcher/`)
  **archives the seeded "E2E Identification Task" (task 1)**. Because specs run
  alphabetically, `researcher/` runs before `user/`, so by the time **U4 Swipe** and
  **U9 Species Reference Images** run, that task is archived and no longer appears in
  `e2e_user`'s Quick Start — they then time out looking for it. This is cross-suite
  coupling with main's tests, not a bug in U4/U9 (they pass in isolation on a fresh
  backend). To run U4/U9 cleanly, run the user folder on a fresh backend, e.g.:
  `npx playwright test --config=playwright.e2e.config.ts tests/e2e/user`
  (or run only your specs: `... tests/e2e/user tests/e2e/admin`).

## Super-admin (e2e)

Under the e2e profile, the super-admin is **`admin_e2e` / `superpassword123`**
(`application-e2e.yml` sets `app.security.super-admin.username=admin_e2e`). So `admin_e2e`
is both a RESEARCHER and `isSuperAdmin=true`. A1/A3 log in as `admin_e2e` for the
super-admin-gated screens (Users management, notifications bell).

Individual specs are **idempotent where possible** (e.g. U3 skips the join and verifies the
joined end-state if already assigned). But cross-test coupling like U2↔U3 is resolved by a
fresh backend, not cleanup code.

**Rule of thumb:** one clean full run = restart backend → `npm run test:e2e:stories`.
Note: this config's `testDir` is `./tests/e2e`, so `npm run test:e2e:stories` now also runs
the researcher specs that live under `tests/e2e/researcher/` (added on main).

## Skipped stories

- **A2 Suspicious Activity Review** — skipped. The backend exposes the endpoints
  (`/api/admin/suspicious-activity`, `.../reset`), but there is **no SuspiciousActivityScreen
  in the frontend** (no nav tab / dashboard tile / component), and no flagged users are
  seeded (flags only arise after fraud detection triggers). It can't be tested through the
  UI without building the screen, which is out of scope (no system-code changes). Revisit if
  the UI is implemented.

