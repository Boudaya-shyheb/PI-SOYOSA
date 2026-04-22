-- Add missing columns to products table
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS average_rating DECIMAL(3, 2) DEFAULT 0,
ADD COLUMN IF NOT EXISTS review_count INT DEFAULT 0;

-- Add missing columns to products table for image_url if missing
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS image_url TEXT;

-- Verify columns exist
DESC products;
