CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE course (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    level VARCHAR(5) NOT NULL,
    capacity INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE chapter (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (course_id, order_index)
);

CREATE TABLE lesson (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chapter_id UUID NOT NULL REFERENCES chapter(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    order_index INTEGER NOT NULL,
    xp_reward INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (chapter_id, order_index)
);

CREATE TABLE learning_material (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lesson_id UUID NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    url VARCHAR(500),
    content VARCHAR(4000),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE enrollment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    user_id VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    progress_percent INTEGER NOT NULL,
    xp_earned INTEGER NOT NULL,
    last_milestone INTEGER,
    enrolled_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    completion_badge VARCHAR(50),
    UNIQUE (course_id, user_id)
);

CREATE TABLE lesson_completion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id UUID NOT NULL REFERENCES enrollment(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lesson(id) ON DELETE CASCADE,
    xp_reward INTEGER NOT NULL,
    completed_at TIMESTAMPTZ NOT NULL,
    UNIQUE (enrollment_id, lesson_id)
);

CREATE INDEX idx_chapter_course ON chapter(course_id);
CREATE INDEX idx_lesson_chapter ON lesson(chapter_id);
CREATE INDEX idx_material_lesson ON learning_material(lesson_id);
CREATE INDEX idx_enrollment_course ON enrollment(course_id);
CREATE INDEX idx_completion_enrollment ON lesson_completion(enrollment_id);
