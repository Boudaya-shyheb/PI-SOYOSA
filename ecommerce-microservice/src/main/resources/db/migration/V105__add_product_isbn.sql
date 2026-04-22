-- Add ISBN column to products table
ALTER TABLE products
ADD COLUMN IF NOT EXISTS isbn VARCHAR(32);
