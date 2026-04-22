# Ecommerce Improvements Summary

Date: 2026-03-28
Scope: Ecommerce microservice + shop frontend only

## Phase 1 - Security and Access Control

### Step 1 - Order access
- Admin-only access enforced for all-orders endpoints and order detail.
- Endpoints:
  - GET /api/ecommerce/orders
  - GET /api/ecommerce/orders/paginated
  - GET /api/ecommerce/orders/{id}
- Files:
  - Backend/ecommerce-microservice/src/main/java/com/ecommerce/controller/OrderController.java

### Step 2 - Review ownership
- Update/delete reviews now require owner or admin.
- Files:
  - Backend/ecommerce-microservice/src/main/java/com/ecommerce/controller/ReviewController.java

### Step 3 - Customer identity binding
- Shop frontend now derives customerId from auth token payload.
- Cart/review actions require login; localStorage fallback removed.
- Files:
  - Frontend/src/app/features/shop/shop.component.ts
  - Frontend/src/app/features/shop/catalog/shop-catalog.component.ts
  - Frontend/src/app/features/shop/cart/shop-cart.component.ts
  - Frontend/src/app/features/shop/product-detail/shop-product-detail.component.ts
  - Frontend/src/app/features/shop/bundle-detail/shop-bundle-detail.component.ts

## Phase 2 - Data Integrity

### Step 1 - Coupon usage
- Coupon usage is incremented at order creation, not at apply-coupon.
- Order creation validates coupon code; discount requires couponCode.
- Files:
  - Backend/ecommerce-microservice/src/main/java/com/ecommerce/dto/CreateOrderRequest.java
  - Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/CartServiceImpl.java
  - Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/OrderServiceImpl.java
  - Frontend/src/app/services/ecommerce-admin-api.service.ts
  - Frontend/src/app/features/shop/cart/shop-cart.component.ts

### Step 2 - Verified purchase
- Review verifiedPurchase is computed from delivered orders; client flag ignored.
- Files:
  - Backend/ecommerce-microservice/src/main/java/com/ecommerce/repository/OrderItemRepository.java
  - Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/ReviewServiceImpl.java

### Step 3 - Discount normalization
- Product and bundle reads no longer write discount changes to DB.
- Expired discounts are cleared in returned DTOs via snapshots only.
- Files:
  - Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/ProductServiceImpl.java
  - Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/BundleServiceImpl.java

## Phase 3 - UX and Data Parity

### Step 1 - Catalog and categories
- Category cards show icon + live product count.
- Catalog list uses real rating and review counts from API.
- Comparison list uses real rating and review counts from API.
- Files:
  - Frontend/src/app/features/shop/shop.component.ts
  - Frontend/src/app/features/shop/catalog/shop-catalog.component.ts
  - Frontend/src/app/features/shop/comparison/comparison.component.ts

### Step 2 - Comparison API
- Comparison page now calls backend compare endpoint.
- Files:
  - Frontend/src/app/features/shop/comparison/comparison.component.ts
  - Frontend/src/app/services/ecommerce-admin-api.service.ts

### Step 3 - Admin images and UI states
  - Frontend/src/app/features/shop/admin/shop-admin.component.ts
  - Frontend/src/app/features/shop/admin/shop-admin.component.html

## Phase 3 Step 4 changes (UX polish)
- Shop theme styling: typography, background gradients, card glow, sticky sidebar and cart summary.
- Landing, catalog, product detail, cart, and admin pages now share a consistent visual language.

## Files updated (Phase 3 Step 4)
- Frontend/src/styles.css
- Frontend/src/app/features/shop/shop.component.html
- Frontend/src/app/features/shop/catalog/shop-catalog.component.html
- Frontend/src/app/features/shop/product-detail/shop-product-detail.component.html
- Frontend/src/app/features/shop/cart/shop-cart.component.html
- Frontend/src/app/features/shop/admin/shop-admin.component.html
  - Frontend/src/app/features/shop/bundles/shop-bundles.component.html
  - Frontend/src/app/features/shop/comparison/comparison.component.html

## Frontend Verification Paths
- /shop
  - Category cards show counts and icons.
- /shop/catalog
  - Ratings and review counts match backend.
- /shop/comparison
  - Attributes reflect backend compare data; error banner on failure.
- /shop/bundles
  - Empty-state message appears when no bundles.
- /shop/admin
  - Forms disable buttons until valid; empty-state cards show when lists are empty; image upload requires URL.

## Image Upload Setup
- Cloudinary setup guide: Backend/ecommerce-microservice/CLOUDINARY_SETUP.md

## Suggested Test Flow
1) Login and open /shop/admin.
2) Try selecting a local image file and saving a product.
   - Expect a warning and no save without URL.
3) Open /shop/catalog and verify ratings/reviews reflect backend.
4) Add two products to comparison; open /shop/comparison and verify attributes.
5) Clear bundles/products in DB and verify empty-states show in admin and bundles page.

## Image Troubleshooting
- Cart items now include productImageUrl in API responses.
- If images still do not show, verify that product imageUrl is stored in DB and returned by /api/ecommerce/products.

## Recommendation Scoring
- Related, bestsellers, and trending lists are re-ranked using category match, rating, and recency (30-day window).
- See Backend/ecommerce-microservice/RECOMMENDATION_SCORING_EXPLAINED.md for the formula.

## Search Analytics and Audit Logs
- Search endpoints log keywords and result counts.
- Admin dashboard shows top searches, zero-result searches, and recent audit actions.
- Endpoints:
  - GET /api/ecommerce/analytics/search/top
  - GET /api/ecommerce/analytics/search/zero-results
  - GET /api/ecommerce/audit-logs

## Admin Dashboard Layout
- Added a left sidebar for navigation between sections.
- List panels are scrollable to keep layout compact.
- Search filters added for products, categories, bundles, coupons, orders, and audit logs.
