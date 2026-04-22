-- Fix incorrect FK introduced in V4 where quiz_submission.quiz_id referenced lesson(id)
-- Expected model: quiz_submission.quiz_id -> quiz(id)

ALTER TABLE quiz_submission
DROP CONSTRAINT IF EXISTS quiz_submission_quiz_id_fkey;

-- 1) Migrate legacy rows that stored lesson IDs into quiz IDs.
--    This updates rows only when quiz_id is not already a valid quiz.id.
UPDATE quiz_submission qs
SET quiz_id = q.id
FROM quiz q
WHERE q.lesson_id = qs.quiz_id
  AND NOT EXISTS (
    SELECT 1
    FROM quiz q2
    WHERE q2.id = qs.quiz_id
  );

-- 2) Remove orphan rows that still do not map to an existing quiz.
--    These rows cannot be kept once the correct FK is applied.
DELETE FROM quiz_submission qs
WHERE NOT EXISTS (
  SELECT 1
  FROM quiz q
  WHERE q.id = qs.quiz_id
);

-- 3) Replace wrong FK with the correct one.

ALTER TABLE quiz_submission
ADD CONSTRAINT quiz_submission_quiz_id_fkey
FOREIGN KEY (quiz_id)
REFERENCES quiz(id)
ON DELETE CASCADE;
