-- Remove single-attempt DB constraint to allow configurable retries
ALTER TABLE quiz_submission
DROP CONSTRAINT IF EXISTS quiz_submission_quiz_id_student_id_key;

-- Keep lookup performance for quiz/student attempt history
CREATE INDEX IF NOT EXISTS idx_quiz_submission_quiz_student
ON quiz_submission(quiz_id, student_id, submitted_at DESC);
