-- Add configurable attempt policy for quizzes
ALTER TABLE quiz
ADD COLUMN IF NOT EXISTS max_attempts INTEGER NOT NULL DEFAULT 1,
ADD COLUMN IF NOT EXISTS cooldown_min INTEGER NOT NULL DEFAULT 0;
