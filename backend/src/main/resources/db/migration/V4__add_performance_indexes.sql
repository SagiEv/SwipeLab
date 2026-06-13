-- Add Performance Indexes for Foreign Keys
-- This migration adds indexes on frequently queried columns
-- to improve JOIN and filter performance as the database grows.

-- ─── CLASSIFICATIONS ──────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_classifications_username   ON classifications(username);
CREATE INDEX IF NOT EXISTS idx_classifications_image_id   ON classifications(image_id);
CREATE INDEX IF NOT EXISTS idx_classifications_task_id    ON classifications(task_id);
-- Composite: most common query pattern (has user classified this image+species?)
CREATE INDEX IF NOT EXISTS idx_classifications_user_image ON classifications(username, image_id);

-- ─── IMAGES ───────────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_images_task_id ON images(task_id);

-- ─── TASKS ────────────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_tasks_created_by ON tasks(created_by);
CREATE INDEX IF NOT EXISTS idx_tasks_status     ON tasks(status);
