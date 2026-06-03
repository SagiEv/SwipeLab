-- V14: Update credibility_score defaults and ranges
--
-- Context: The credibility system has been upgraded to use a 0-100 normalized
-- composite score. New users now start at 50.0 (neutral) instead of 0.0.
-- This migration:
--   1. Resets the default column value to 50.0
--   2. Updates existing users who still sit at 0.0 with 0 total classifications
--      (they have no signal — reset to neutral 50.0)
--   3. Leaves users who have an actual non-zero score untouched
--      (their score will be recomputed organically as new classifications arrive)

-- 1. Update the column default for future INSERT statements
ALTER TABLE users
    ALTER COLUMN credibility_score SET DEFAULT 50.0;

-- 2. Reset only genuine "never-computed" users (score=0.0, no classifications at all)
--    to the new neutral starting point of 50.0.
--    Users who earned a 0.0 through bad behaviour keep their score.
UPDATE users u
SET credibility_score = 50.0
WHERE u.credibility_score = 0.0
  AND u.total_classifications = 0;
