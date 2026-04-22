-- Complete Database Initialization Script
-- Clear existing bad data and insert proper sample data

-- 1. CLEAR EXISTING DATA
DELETE FROM review_attachments;
DELETE FROM review_replies;
DELETE FROM reviews;
DELETE FROM bundle_items;
DELETE FROM bundles;
DELETE FROM cart_items;
DELETE FROM carts;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM products;
DELETE FROM categories;

-- 2. INSERT CATEGORIES
INSERT INTO categories (name, description, created_at, updated_at) VALUES
('Stationery', 'Writing instruments, notebooks, and paper products', NOW(), NOW()),
('Study Tools', 'Learning aids and study materials', NOW(), NOW()),
('Digital', 'Digital downloads and online resources', NOW(), NOW()),
('Accessories', 'Bags, organizers, and desk accessories', NOW(), NOW()),
('Books', 'Educational and reference books', NOW(), NOW());

-- 3. INSERT PRODUCTS (25 products)
INSERT INTO products (name, description, price, quantity_available, category_id, image_url, average_rating, review_count, created_at, updated_at) VALUES

-- Stationery (cat_id = 1)
('Premium Notebook Set', 'High-quality A4 lined notebooks with soft covers, pack of 3', 24.99, 150, 1, 'https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=400', 4.50, 2, NOW(), NOW()),
('Mechanical Pencil Set', 'Professional grade mechanical pencils with 5 different lead sizes', 18.50, 200, 1, 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=400', 5.00, 1, NOW(), NOW()),
('Highlighter Pack', 'Assorted neon highlighters, pack of 12 with comfortable grip', 12.99, 300, 1, 'https://images.unsplash.com/photo-1578993949214-f37c15e42db8?w=400', 4.00, 1, NOW(), NOW()),
('Pen Collection', 'Set of 24 premium ballpoint pens in various colors', 16.75, 180, 1, 'https://images.unsplash.com/photo-1605857260722-84b80b56fcc8?w=400', 5.00, 1, NOW(), NOW()),
('Sticky Notes Bulk', 'Assorted colors and sizes, pack of 50', 14.30, 250, 1, 'https://images.unsplash.com/photo-1587825140708-dfaf72ae4b04?w=400', 4.00, 1, NOW(), NOW()),

-- Study Tools (cat_id = 2)
('Vocabulary Flashcards', 'English-Arabic bilingual flashcards for language learning', 22.00, 100, 2, 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', 5.00, 1, NOW(), NOW()),
('Study Timer - Pomodoro', 'Digital timer for focused study sessions with alarm', 45.99, 75, 2, 'https://images.unsplash.com/photo-1611039513987-6e52baed2d63?w=400', 5.00, 1, NOW(), NOW()),
('Language Learning App Subscription', '6-month access to premium language app', 89.99, 500, 2, 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400', 4.00, 1, NOW(), NOW()),
('Math Formula Sheet', 'Laminated printable math formulas for quick reference', 8.50, 400, 2, 'https://images.unsplash.com/photo-1509909756405-2fd17c5567a8?w=400', 4.00, 1, NOW(), NOW()),
('Grammar Guide Handbook', 'Comprehensive English grammar reference book', 19.99, 120, 2, 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', 5.00, 1, NOW(), NOW()),

-- Digital (cat_id = 3)
('Digital Study Templates Bundle', 'Downloadable Notion templates for organization', 12.99, 1000, 3, 'https://images.unsplash.com/photo-1540245647168-0ccde64c3f58?w=400', 0.00, 0, NOW(), NOW()),
('Exam Prep Guide PDF', '150-page comprehensive exam preparation guide', 9.99, 2000, 3, 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', 0.00, 0, NOW(), NOW()),
('Note-taking Strategies eBook', 'Digital guide to effective note-taking methods', 7.50, 1500, 3, 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', 0.00, 0, NOW(), NOW()),
('Study Schedule Planner', 'Editable digital calendar for semester planning', 5.99, 1200, 3, 'https://images.unsplash.com/photo-1540245647168-0ccde64c3f58?w=400', 0.00, 0, NOW(), NOW()),
('Research Tips eBook', 'Guide to conducting academic research effectively', 11.99, 800, 3, 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', 0.00, 0, NOW(), NOW()),

-- Accessories (cat_id = 4)
('School Backpack - Blue', 'Durable polyester backpack with multiple compartments and laptop sleeve', 89.99, 45, 4, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400', 5.00, 1, NOW(), NOW()),
('Desk Organizer Set', 'Wooden desk organizer with 3 compartments and pen holder', 34.50, 90, 4, 'https://images.unsplash.com/photo-1610701596007-11502861dcfa?w=400', 4.00, 1, NOW(), NOW()),
('Desk Lamp - LED', 'Adjustable LED desk lamp with USB charging port', 42.99, 60, 4, 'https://images.unsplash.com/photo-1565636192335-14c46fa1120d?w=400', 0.00, 0, NOW(), NOW()),
('Monitor Stand with Storage', 'Adjustable monitor stand with 2 storage drawers', 65.75, 55, 4, 'https://images.unsplash.com/photo-1531415074968-36987b292e3a?w=400', 0.00, 0, NOW(), NOW()),
('Keyboard and Mouse Combo', 'Wireless ergonomic keyboard and mouse set', 55.30, 80, 4, 'https://images.unsplash.com/photo-1587289810167-8f9ce4614270?w=400', 0.00, 0, NOW(), NOW()),

-- Books (cat_id = 5)
('English Grammar in Use', 'Essential grammar reference for intermediate learners', 28.99, 85, 5, 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', 0.00, 0, NOW(), NOW()),
('TOEFL Preparation Guide', 'Complete TOEFL test preparation with practice tests', 44.99, 65, 5, 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', 0.00, 0, NOW(), NOW()),
('Business English Handbook', 'Professional business communication guide', 36.75, 72, 5, 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', 0.00, 0, NOW(), NOW()),
('Vocabulary Builder 5000 Words', 'Progressive vocabulary learning with context examples', 21.50, 110, 5, 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', 0.00, 0, NOW(), NOW()),
('Academic Writing Master', 'Guide to academic essay and research paper writing', 32.99, 95, 5, 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', 0.00, 0, NOW(), NOW());

-- 4. INSERT BUNDLES
INSERT INTO bundles (name, description, price, status, image_url, created_at, updated_at) VALUES
('Student Starter Pack', 'Perfect bundle for new students with essential supplies', 149.99, 'ACTIVE', 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400', NOW(), NOW()),
('Digital Learning Suite', 'Complete digital resources for online learning', 89.99, 'ACTIVE', 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', NOW(), NOW()),
('Exam Preparation Kit', 'Everything needed for exam preparation and success', 199.99, 'ACTIVE', 'https://images.unsplash.com/photo-1507842217343-583f20270319?w=400', NOW(), NOW()),
('Productivity Bundle', 'Tools and resources to maximize study productivity', 124.99, 'ACTIVE', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400', NOW(), NOW()),
('Complete Study Essentials', 'All-in-one bundle with physical and digital resources', 299.99, 'ACTIVE', 'https://images.unsplash.com/photo-1555421692-202b9440d0cb?w=400', NOW(), NOW());

-- 5. INSERT BUNDLE ITEMS
INSERT INTO bundle_items (bundle_id, product_id, quantity, created_at, updated_at) VALUES
-- Student Starter Pack (bundle_id=1)
(1, 1, 1, NOW(), NOW()),
(1, 2, 1, NOW(), NOW()),
(1, 9, 1, NOW(), NOW()),
(1, 16, 1, NOW(), NOW()),
(1, 17, 1, NOW(), NOW()),

-- Digital Learning Suite (bundle_id=2)
(2, 11, 1, NOW(), NOW()),
(2, 12, 1, NOW(), NOW()),
(2, 13, 1, NOW(), NOW()),
(2, 14, 1, NOW(), NOW()),
(2, 8, 1, NOW(), NOW()),

-- Exam Preparation Kit (bundle_id=3)
(3, 7, 1, NOW(), NOW()),
(3, 25, 1, NOW(), NOW()),
(3, 24, 1, NOW(), NOW()),
(3, 12, 1, NOW(), NOW()),

-- Productivity Bundle (bundle_id=4)
(4, 18, 1, NOW(), NOW()),
(4, 19, 1, NOW(), NOW()),
(4, 20, 1, NOW(), NOW()),
(4, 7, 1, NOW(), NOW()),

-- Complete Study Essentials (bundle_id=5)
(5, 1, 1, NOW(), NOW()),
(5, 6, 1, NOW(), NOW()),
(5, 7, 1, NOW(), NOW()),
(5, 11, 1, NOW(), NOW()),
(5, 16, 1, NOW(), NOW()),
(5, 17, 1, NOW(), NOW()),
(5, 22, 1, NOW(), NOW());

-- 6. INSERT SAMPLE REVIEWS
INSERT INTO reviews (product_id, customer_id, rating, title, comment, helpful_count, unhelpful_count, verified_purchase, created_at, updated_at) VALUES
(1, 1, 5, 'Excellent quality notebooks!', 'These notebooks are perfect for study. The paper quality is great and the binding is strong.', 5, 0, TRUE, NOW(), NOW()),
(1, 2, 4, 'Good value for money', 'Great notebooks at a reasonable price. Fast delivery too!', 3, 0, TRUE, NOW(), NOW()),
(2, 3, 5, 'Best pencils I have used', 'High quality mechanical pencils. Very comfortable to use for long study sessions.', 4, 0, TRUE, NOW(), NOW()),
(3, 4, 4, 'Bright and vibrant colors', 'The highlighters are bright and dont bleed through the paper. Recommended!', 2, 0, TRUE, NOW(), NOW()),
(4, 5, 5, 'Premium quality pens', 'Smooth writing experience. These pens are worth every penny.', 6, 0, TRUE, NOW(), NOW()),
(6, 6, 5, 'Perfect for language learning', 'These flashcards are well-designed and helped me improve my vocabulary significantly.', 3, 0, TRUE, NOW(), NOW()),
(7, 7, 5, 'Game changer for productivity', 'This timer really helped me focus during study sessions. Highly recommend!', 7, 0, TRUE, NOW(), NOW()),
(8, 8, 4, 'Comprehensive learning resource', 'Great app with lots of features. The subscription is worth it.', 2, 0, TRUE, NOW(), NOW()),
(16, 9, 5, 'Comfortable and spacious backpack', 'Perfect for students! Multiple compartments and laptop sleeve. Great quality.', 5, 0, TRUE, NOW(), NOW()),
(17, 10, 4, 'Great desk organizer', 'Looks good on my desk and keeps everything organized. Highly satisfied!', 2, 0, TRUE, NOW(), NOW()),
(21, 11, 5, 'Essential grammar book', 'This grammar guide is comprehensive and easy to understand. Must have for English learners!', 4, 0, TRUE, NOW(), NOW()),
(22, 12, 5, 'Excellent TOEFL preparation', 'Very detailed guide with practice tests. Helped me ace the TOEFL exam!', 8, 0, TRUE, NOW(), NOW());

-- 7. ADD SAMPLE REVIEW REPLIES
INSERT INTO review_replies (review_id, responder_id, responder_type, comment, created_at, updated_at) VALUES
(7, 100, 'SELLER', 'Thank you for the wonderful feedback! We hope our product helped you achieve your study goals.', NOW(), NOW()),
(12, 100, 'SELLER', 'We are thrilled that our TOEFL guide helped you succeed! Congratulations on your achievement!', NOW(), NOW());

-- Verify the data was inserted
SELECT 'Categories' as table_name, COUNT(*) as record_count FROM categories
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Bundles', COUNT(*) FROM bundles
UNION ALL
SELECT 'Bundle Items', COUNT(*) FROM bundle_items
UNION ALL
SELECT 'Reviews', COUNT(*) FROM reviews
UNION ALL
SELECT 'Review Replies', COUNT(*) FROM review_replies;
