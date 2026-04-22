ALTER TABLE orders
    ADD COLUMN delivery_street VARCHAR(255) NULL,
    ADD COLUMN delivery_city VARCHAR(120) NULL,
    ADD COLUMN delivery_postal_code VARCHAR(40) NULL,
    ADD COLUMN delivery_country VARCHAR(120) NULL;
