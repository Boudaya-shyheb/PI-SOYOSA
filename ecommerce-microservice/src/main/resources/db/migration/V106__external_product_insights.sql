-- External product insights table
CREATE TABLE IF NOT EXISTS external_product_insights (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    source VARCHAR(50) NOT NULL,
    source_id VARCHAR(120),
    isbn VARCHAR(32),
    title VARCHAR(500),
    authors TEXT,
    publisher VARCHAR(255),
    published_date VARCHAR(32),
    page_count INT,
    language VARCHAR(10),
    categories TEXT,
    average_rating DECIMAL(3, 2),
    ratings_count INT,
    thumbnail_url TEXT,
    info_link TEXT,
    raw_payload TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_external_insights_product
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT uq_external_insights_product_source UNIQUE (product_id, source)
);

CREATE INDEX IF NOT EXISTS idx_external_insights_product ON external_product_insights(product_id);
CREATE INDEX IF NOT EXISTS idx_external_insights_isbn ON external_product_insights(isbn);
CREATE INDEX IF NOT EXISTS idx_external_insights_source_id ON external_product_insights(source, source_id);
