CREATE TABLE course_review (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    user_id VARCHAR(100) NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(2000) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (course_id, user_id)
);

CREATE INDEX idx_course_review_course ON course_review(course_id);
CREATE INDEX idx_course_review_course_updated ON course_review(course_id, updated_at DESC);
