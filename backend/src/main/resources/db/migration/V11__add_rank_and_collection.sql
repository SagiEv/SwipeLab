-- V11__add_rank_and_collection.sql
-- Adds YES-tag counter to gamification for rank computation,
-- and creates the user collection table (one row per YES-tagged image).

-- 1. Track how many YES tags the user has submitted (drives rank tier).
ALTER TABLE gamification ADD COLUMN IF NOT EXISTS yes_tag_count INTEGER NOT NULL DEFAULT 0;

-- 2. User collection — one row per YES-tagged image; intentionally no UNIQUE constraint
--    because two separate YES swipes on the same image are two valid entries.
CREATE TABLE IF NOT EXISTS user_collection (
    id          BIGSERIAL    PRIMARY KEY,
    username    VARCHAR(255) NOT NULL,
    image_id    BIGINT       NOT NULL,
    species     VARCHAR(255),
    image_url   TEXT,
    task_id     BIGINT,
    tagged_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_collection_username ON user_collection(username);
CREATE INDEX IF NOT EXISTS idx_user_collection_image    ON user_collection(image_id);
