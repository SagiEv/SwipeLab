-- ─── GOLD IMAGES TABLE ───────────────────────────────────────────────────────
-- Stores metadata for gold-standard images used in credibility evaluation.
-- species + correct_answer drive the credibility pipeline.
-- difficulty_level and explanation are retained for researcher notes.
CREATE TABLE gold_images (
    id BIGSERIAL PRIMARY KEY,
    image_id BIGINT NOT NULL UNIQUE,
    species VARCHAR(255),                   -- species being queried (matches query_species)
    correct_answer VARCHAR(50),             -- YES | NO | DONT_KNOW | TRASH
    difficulty_level VARCHAR(50) DEFAULT 'MEDIUM',  -- EASY | MEDIUM | HARD (researcher notes)
    explanation TEXT,                       -- expert notes on why this is the correct answer
    active BOOLEAN NOT NULL DEFAULT TRUE,   -- soft-delete: never physically remove rows
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_gold_image_main FOREIGN KEY (image_id) REFERENCES images(id)
);

-- ─── CREDIBILITY RECORDS TABLE ───────────────────────────────────────────────
-- Immutable audit log: one row per user classification on a gold image.
-- Used to compute per-user gold accuracy for the credibility score.
CREATE TABLE credibility_records (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    task_id BIGINT NOT NULL,
    image_id BIGINT NOT NULL,              -- FK to gold_images.id
    query_species VARCHAR(255),
    user_response VARCHAR(50) NOT NULL,    -- what the user answered
    correct_answer VARCHAR(50) NOT NULL,   -- what the correct answer was at the time
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_cr_gold_image FOREIGN KEY (image_id) REFERENCES gold_images(id)
);

CREATE INDEX idx_cr_username ON credibility_records(username);
CREATE INDEX idx_cr_image_id ON credibility_records(image_id);
