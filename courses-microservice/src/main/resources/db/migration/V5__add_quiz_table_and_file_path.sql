-- Add quiz table to store quiz configurations
CREATE TABLE quiz (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id UUID NOT NULL UNIQUE REFERENCES lesson(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    time_limit_min INTEGER NOT NULL,
    passing_score INTEGER NOT NULL,
    questions JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_quiz_lesson ON quiz(lesson_id);

-- Add file_path column to learning_material for file storage optimization
ALTER TABLE learning_material ADD COLUMN file_path VARCHAR(500);

-- Note: After migration, you should move existing file_data to disk storage
-- and update file_path, then optionally remove file_data column
