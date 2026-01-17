-- Add missing streak columns (missed in previous migration)
ALTER TABLE users ADD COLUMN IF NOT EXISTS current_streak INTEGER DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_streak_update TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS points BIGINT DEFAULT 0;

-- Seed initial badges
-- Using ON CONFLICT logic if supported, or just INSERTs which Flyway manages via checksums/history.
-- Since 'name' is UNIQUE in V2 schema, we can use ON CONFLICT.

INSERT INTO badges (name, description, icon_url, criteria_json) VALUES
('First Swipe', 'Awarded for completing your first classification', 'badge_first_swipe.png', '{"type": "COUNT", "threshold": 1}'),
('10 Swipes', 'Awarded for completing 10 classifications', 'badge_10_swipes.png', '{"type": "COUNT", "threshold": 10}'),
('100 Swipes', 'Awarded for completing 100 classifications', 'badge_100_swipes.png', '{"type": "COUNT", "threshold": 100}'),
('3 Day Streak', 'Awarded for maintaining a 3-day streak', 'badge_streak_3.png', '{"type": "STREAK", "threshold": 3}'),
('7 Day Streak', 'Awarded for maintaining a 7-day streak', 'badge_streak_7.png', '{"type": "STREAK", "threshold": 7}'),
('1000 Points', 'Awarded for earning 1000 points', 'badge_points_1000.png', '{"type": "POINTS", "threshold": 1000}')
ON CONFLICT (name) DO NOTHING;
