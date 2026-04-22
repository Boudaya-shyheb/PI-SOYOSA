package com.ecommerce.config;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataInitializerConfig {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            try {
                // Check if data already exists
                long categoryCount = categoryRepository.count();
                if (categoryCount > 0) {
                    log.info("Database already initialized with {} categories, skipping data load", categoryCount);
                    return;
                }

                log.info("Initializing sample data...");

                // 1. Create categories
                Map<String, Long> categoryIds = initializeCategories();

                // 2. Create products
                initializeProducts(categoryIds);

                log.info("Sample data initialization completed successfully!");

            } catch (Exception e) {
                log.error("Error initializing sample data: ", e);
            }
        };
    }

    private Map<String, Long> initializeCategories() {
        log.info("Creating categories...");
        Map<String, Long> categoryIds = new LinkedHashMap<>();
        
        String[] categoryNames = {"Stationery", "Study Tools", "Digital", "Accessories", "Books"};
        String[] descriptions = {
            "Writing instruments, notebooks, and paper products",
            "Learning aids and study materials",
            "Digital downloads and online resources",
            "Bags, organizers, and desk accessories",
            "Educational and reference books"
        };

        for (int i = 0; i < categoryNames.length; i++) {
            try {
                Category cat = Category.builder()
                        .name(categoryNames[i])
                        .description(descriptions[i])
                        .build();
                Category saved = categoryRepository.save(cat);
                categoryIds.put(categoryNames[i], saved.getId());
                log.info("Created category: {} with ID: {}", categoryNames[i], saved.getId());
            } catch (Exception e) {
                log.warn("Error creating category {}: {}", categoryNames[i], e.getMessage());
            }
        }
        
        return categoryIds;
    }

    private void initializeProducts(Map<String, Long> categoryIds) {
        log.info("Creating products...");
        
        String[][] products = {
            // Stationery
            {"Premium Notebook Set", "High-quality A4 lined notebooks with soft covers, pack of 3", "24.99", "150", "Stationery", "https://images.unsplash.com/photo-1507842217343-583f7270bfba?w=500"},
            {"Mechanical Pencil Set", "Professional grade mechanical pencils with 5 different lead sizes", "18.50", "200", "Stationery", "https://images.unsplash.com/photo-1586985289688-cacf56ca0000?w=500"},
            {"Highlighter Pack", "Assorted neon highlighters, pack of 12 with comfortable grip", "12.99", "300", "Stationery", "https://images.unsplash.com/photo-1570303923492-8e9cc34ce3c1?w=500"},
            {"Pen Collection", "Set of 24 premium ballpoint pens in various colors", "16.75", "180", "Stationery", "https://images.unsplash.com/photo-1538108149393-fbbd81895907?w=500"},
            {"Sticky Notes Bulk", "Assorted colors and sizes, pack of 50", "14.30", "250", "Stationery", "https://images.unsplash.com/photo-1569481269369-8e92789c3bbb?w=500"},
            
            // Study Tools
            {"Vocabulary Flashcards", "English-Arabic bilingual flashcards for language learning", "22.00", "100", "Study Tools", "https://images.unsplash.com/photo-1507842217343-583f7270bfba?w=500"},
            {"Study Timer - Pomodoro", "Digital timer for focused study sessions with alarm", "45.99", "75", "Study Tools", "https://images.unsplash.com/photo-1611148814347-88c0d5b0e646?w=500"},
            {"Language Learning App Subscription", "6-month access to premium language app", "89.99", "500", "Study Tools", "https://images.unsplash.com/photo-1546410531-bb4caa6b0872?w=500"},
            {"Math Formula Sheet", "Laminated printable math formulas for quick reference", "8.50", "400", "Study Tools", "https://images.unsplash.com/photo-1596556189898-01c9844b9168?w=500"},
            {"Grammar Guide Handbook", "Comprehensive English grammar reference book", "19.99", "120", "Study Tools", "https://images.unsplash.com/photo-1507842217343-583f7270bfba?w=500"},
            
            // Digital
            {"Digital Study Templates Bundle", "Downloadable Notion templates for organization", "12.99", "1000", "Digital", "https://images.unsplash.com/photo-1573141520009-b76b27e84530?w=500"},
            {"Exam Prep Guide PDF", "150-page comprehensive exam preparation guide", "9.99", "2000", "Digital", "https://images.unsplash.com/photo-1507842217343-583f7270bfba?w=500"},
            {"Note-taking Strategies eBook", "Digital guide to effective note-taking methods", "7.50", "1500", "Digital", "https://images.unsplash.com/photo-1532521776974-7f6729dbe742?w=500"},
            {"Study Schedule Planner", "Editable digital calendar for semester planning", "5.99", "1200", "Digital", "https://images.unsplash.com/photo-1453614512568-c4024d13c247?w=500"},
            {"Research Tips eBook", "Guide to conducting academic research effectively", "11.99", "800", "Digital", "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=500"},
            
            // Accessories
            {"School Backpack - Blue", "Durable polyester backpack with multiple compartments and laptop sleeve", "89.99", "45", "Accessories", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500"},
            {"Desk Organizer Set", "Wooden desk organizer with 3 compartments and pen holder", "34.50", "90", "Accessories", "https://images.unsplash.com/photo-1595521624861-0a4517cf4c93?w=500"},
            {"Desk Lamp - LED", "Adjustable LED desk lamp with USB charging port", "42.99", "60", "Accessories", "https://images.unsplash.com/photo-1565546418040-8b1f99e8c3d8?w=500"},
            {"Monitor Stand with Storage", "Adjustable monitor stand with 2 storage drawers", "65.75", "55", "Accessories", "https://images.unsplash.com/photo-1572365992253-3cb3e56dd362?w=500"},
            {"Keyboard and Mouse Combo", "Wireless ergonomic keyboard and mouse set", "55.30", "80", "Accessories", "https://images.unsplash.com/photo-1587829191301-521fd573d32c?w=500"},
            
            // Books
            {"English Grammar in Use", "Essential grammar reference for intermediate learners", "28.99", "85", "Books", "https://images.unsplash.com/photo-1507842217343-583f7270bfba?w=500"},
            {"TOEFL Preparation Guide", "Complete TOEFL test preparation with practice tests", "44.99", "65", "Books", "https://images.unsplash.com/photo-1507842217343-583f7270bfba?w=500"},
            {"Business English Handbook", "Professional business communication guide", "36.75", "72", "Books", "https://images.unsplash.com/photo-1507842217343-583f7270bfba?w=500"},
            {"Vocabulary Builder 5000 Words", "Progressive vocabulary learning with context examples", "21.50", "110", "Books", "https://images.unsplash.com/photo-1507842217343-583f7270bfba?w=500"},
            {"Academic Writing Master", "Guide to academic essay and research paper writing", "32.99", "95", "Books", "https://images.unsplash.com/photo-1507842217343-583f7270bfba?w=500"}
        };

        for (String[] p : products) {
            try {
                Long categoryId = categoryIds.get(p[4]);
                if (categoryId == null) {
                    log.warn("Category {} not found for product {}", p[4], p[0]);
                    continue;
                }

                Category category = new Category();
                category.setId(categoryId);

                Product product = Product.builder()
                        .name(p[0])
                        .description(p[1])
                        .price(new BigDecimal(p[2]))
                        .quantityAvailable(Integer.parseInt(p[3]))
                        .category(category)
                        .imageUrl(p[5])
                        .averageRating(BigDecimal.ZERO)
                        .reviewCount(0)
                        .build();
                
                productRepository.save(product);
                log.debug("Created product: {}", p[0]);
            } catch (Exception e) {
                log.warn("Error creating product: {}", e.getMessage());
            }
        }
        
        log.info("Product creation completed");
    }
}
