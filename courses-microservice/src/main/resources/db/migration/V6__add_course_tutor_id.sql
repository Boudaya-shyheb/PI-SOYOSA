ALTER TABLE course ADD COLUMN tutor_id VARCHAR(100);

UPDATE course SET tutor_id = 'legacy-tutor' WHERE tutor_id IS NULL;

ALTER TABLE course ALTER COLUMN tutor_id SET NOT NULL;

CREATE INDEX idx_course_tutor_id ON course(tutor_id);
