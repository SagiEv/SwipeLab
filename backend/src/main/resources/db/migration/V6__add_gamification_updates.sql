-- Add gamification cache columns to users table.
-- score/badges/rank_level are denormalized from the gamification table
-- for fast profile reads without a join.
ALTER TABLE users ADD COLUMN IF NOT EXISTS score      BIGINT      NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS badges     TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS rank_level VARCHAR(50) NOT NULL DEFAULT 'UNRANKED';

-- Seed initial badges (legacy badge system from V2)
INSERT INTO badges (name, description, icon_url, criteria_json) VALUES
('First Swipe',  'Awarded for completing your first classification', 'badge_first_swipe.png', '{"type": "COUNT", "threshold": 1}'),
('10 Swipes',    'Awarded for completing 10 classifications',        'badge_10_swipes.png',   '{"type": "COUNT", "threshold": 10}'),
('100 Swipes',   'Awarded for completing 100 classifications',       'badge_100_swipes.png',  '{"type": "COUNT", "threshold": 100}'),
('3 Day Streak', 'Awarded for maintaining a 3-day streak',          'badge_streak_3.png',    '{"type": "STREAK", "threshold": 3}'),
('7 Day Streak', 'Awarded for maintaining a 7-day streak',          'badge_streak_7.png',    '{"type": "STREAK", "threshold": 7}'),
('1000 Points',  'Awarded for earning 1000 points',                 'badge_points_1000.png', '{"type": "POINTS", "threshold": 1000}')
ON CONFLICT (name) DO NOTHING;
