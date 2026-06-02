-- ============================================================
-- V15 – Task assignment schema: is_public flag + assigned users table
-- Issue: [Frontend] tasks assign not updating because cached myTasksScreen
-- ============================================================

-- ── 1. Public task flag ───────────────────────────────────────
-- Allows a task to appear in the public "Explore Tasks" list,
-- where any authenticated user can self-assign.
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS is_public BOOLEAN NOT NULL DEFAULT FALSE;

-- ── 2. Task → assigned users join table ──────────────────────
-- Tracks which usernames have self-assigned a public task via
-- POST /api/v1/tasks/{id}/assign.
CREATE TABLE IF NOT EXISTS task_assigned_users (
    task_id  BIGINT       NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    username VARCHAR(255) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
    PRIMARY KEY (task_id, username)
);

CREATE INDEX IF NOT EXISTS idx_tau_task_id  ON task_assigned_users(task_id);
CREATE INDEX IF NOT EXISTS idx_tau_username ON task_assigned_users(username);

-- ── 3. Task extra columns used by Task entity ─────────────────
-- These columns exist in the JPA entity but were never formally
-- migrated; add them safely with IF NOT EXISTS semantics.
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS query_species   VARCHAR(255);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS question        VARCHAR(500);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS source_system   VARCHAR(50)  NOT NULL DEFAULT 'INTERNAL';
