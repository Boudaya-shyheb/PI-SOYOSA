CREATE TABLE quiz_submission (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    quiz_id UUID NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    student_id VARCHAR(100) NOT NULL,
    answers JSONB NOT NULL,
    score INTEGER,
    submitted_at TIMESTAMPTZ NOT NULL,
    graded_at TIMESTAMPTZ,
    graded_by VARCHAR(100),
    UNIQUE (quiz_id, student_id)
);

CREATE INDEX idx_quiz_submission_course ON quiz_submission(course_id);
CREATE INDEX idx_quiz_submission_student ON quiz_submission(student_id);
CREATE INDEX idx_quiz_submission_quiz ON quiz_submission(quiz_id);
