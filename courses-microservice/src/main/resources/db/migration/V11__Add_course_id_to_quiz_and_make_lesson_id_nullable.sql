ALTER TABLE quiz ADD COLUMN course_id UUID;

-- We need to populate the course_id for existing quizzes.
-- We can get the course_id from the lesson's chapter.
UPDATE quiz SET course_id = (
    SELECT c.id FROM course c
    JOIN chapter ch ON ch.course_id = c.id
    JOIN lesson l ON l.chapter_id = ch.id
    WHERE l.id = quiz.lesson_id
);

-- Now that existing data is populated, we can add the NOT NULL constraint.
ALTER TABLE quiz ALTER COLUMN course_id SET NOT NULL;

-- Add the foreign key constraint
ALTER TABLE quiz ADD CONSTRAINT fk_quiz_course FOREIGN KEY (course_id) REFERENCES course(id);

-- Make the lesson_id nullable
ALTER TABLE quiz ALTER COLUMN lesson_id DROP NOT NULL;
