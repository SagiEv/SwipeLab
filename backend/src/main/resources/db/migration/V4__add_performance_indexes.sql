-- Add Performance Indexes for Foreign Keys
-- This migration adds indexes on frequently queried foreign key columns
-- to improve JOIN performance as the database grows

-- Add index on classifications.user_id for faster user lookup
CREATE INDEX IF NOT EXISTS idx_classifications_user_id ON classifications(user_id);

-- Add index on classifications.image_id for faster image lookup
CREATE INDEX IF NOT EXISTS idx_classifications_image_id ON classifications(image_id);

-- Add index on classifications.label_id for faster label lookup  
CREATE INDEX IF NOT EXISTS idx_classifications_label_id ON classifications(label_id);

-- Add composite index for common query pattern (finding user's classification for specific image)
CREATE INDEX IF NOT EXISTS idx_classifications_user_image ON classifications(user_id, image_id);

-- Add index on images.task_id for faster task-based image queries
CREATE INDEX IF NOT EXISTS idx_images_task_id ON images(task_id);

-- Add index on tasks.created_by for faster creator lookup
CREATE INDEX IF NOT EXISTS idx_tasks_created_by ON tasks(created_by);

-- Add index on tasks.status for filtering active/completed tasks
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
