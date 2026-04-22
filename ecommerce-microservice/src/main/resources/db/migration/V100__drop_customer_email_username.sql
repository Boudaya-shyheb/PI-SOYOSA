-- Drop customer_email and customer_username columns from reviews table
-- These were leftover from User Service integration attempts and are no longer needed

ALTER TABLE reviews DROP COLUMN IF EXISTS customer_email;
ALTER TABLE reviews DROP COLUMN IF EXISTS customer_username;
