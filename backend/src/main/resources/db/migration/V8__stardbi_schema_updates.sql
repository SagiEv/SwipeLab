-- V2__stardbi_schema_updates.sql
-- Formalizes the schema changes required for the STARDBI data ingestion pipeline so that
-- production environments (like the VM) remain strictly synced without relying on Hibernate's ddl-auto.

-- 1. Support Tasks having both a technical and display name to resolve DDL conflicts
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS title VARCHAR(255) DEFAULT 'Untitled' NOT NULL;

-- 2. Support Image Hierarchy
ALTER TABLE images ADD COLUMN IF NOT EXISTS parent_image_id BIGINT;

-- 3. Support mapping to external Stardbi source natively
ALTER TABLE images ADD COLUMN IF NOT EXISTS external_box_id BIGINT UNIQUE;

-- 4. Support inserting Base64 directly into the image record safely
-- First ensure the column exists (in case ddl-auto didn't run)
ALTER TABLE images ADD COLUMN IF NOT EXISTS src_path TEXT;
-- Force the column type to TEXT so it doesn't crash on standard 255 length constraints
ALTER TABLE images ALTER COLUMN src_path TYPE TEXT;

-- 5. Remove the original restrictive NOT NULL constraint on image_url, since internal crops
-- rely on src_path base64 encoding instead of external URLs.
ALTER TABLE images ALTER COLUMN image_url DROP NOT NULL;
