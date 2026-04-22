# Ecommerce Detailed Documentation (Changes Added)

Date: 2026-03-28
Scope: Ecommerce microservice + shop frontend only
Note: This document describes new or modified logic and methods (HTML view markup is excluded).

---

## 1) Token Validation and Role Utilities (User Service)

### TokenValidationController
File: Backend/user-microservice/src/main/java/soyosa/userservice/Controller/TokenValidationController.java

Purpose: Provide internal JWT validation and user metadata for other services.

Key methods:
- validateToken(authHeader)
  - Verifies the Authorization header format (Bearer token).
  - Parses token with the configured JWT secret.
  - Returns TokenValidationResponse containing:
    - isValid boolean
    - userId (from subject)
    - role (from claim "role" or fallback claim "authorities")
    - expiration time
  - Used by ecommerce microservice to validate incoming user tokens.

- getRole(authHeader)
  - Reads role claim from token.
  - Supports fallback to "authorities" claim if "role" is absent.
  - Used by ecommerce microservice to enforce ADMIN access.

- getUserId(authHeader)
  - Returns the subject (user id) from the token.
  - Used by ecommerce microservice to match customerId in requests.

These methods enable role-based restrictions and ownership validation without exposing user-service internals.

---

## 2) Ecommerce Access Control

### OrderController
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/controller/OrderController.java

Changes:
- Admin-only access was enforced for:
  - GET /api/ecommerce/orders
  - GET /api/ecommerce/orders/paginated
  - GET /api/ecommerce/orders/{id}

Core helpers:
- requireValidToken(authHeader)
  - Validates non-empty header and calls user service to validate token.

- requireAdmin(authHeader)
  - Calls requireValidToken.
  - Uses userServiceFeignService.getUserRole(authHeader).
  - Enforces role == ADMIN.

- requireCustomerAccess(customerId, authHeader)
  - Validates token, then resolves userId from token.
  - Fetches user info with getUserInfo for stable id mapping.
  - Ensures token user matches customerId.

Result: only admins can list all orders or view arbitrary orders. Customers can only access their own orders.

---

### ReviewController
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/controller/ReviewController.java

Changes:
- Review update and delete now require owner or admin.

Core helpers:
- requireOwnerOrAdmin(reviewId, authHeader)
  - Validates token and role.
  - Allows ADMIN directly.
  - Otherwise loads the review and compares review.customerId with token userId.

Result: customers can only modify their own reviews; admins can manage any review.

---

## 3) Coupon Usage Integrity

### CreateOrderRequest
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/dto/CreateOrderRequest.java

Changes:
- Added couponCode field to explicitly validate and apply coupon on order creation.
- discountAmount now requires couponCode, preventing arbitrary discount application.

### CartServiceImpl (applyCoupon)
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/CartServiceImpl.java

Changes:
- applyCoupon no longer increments usedCount.
- It only validates the coupon and returns discount info.

Purpose: coupon usage is now counted only when an order is successfully created.

### OrderServiceImpl (applyCouponToOrder)
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/OrderServiceImpl.java

Key logic:
- When couponCode is present, applyCouponToOrder validates:
  - active flag
  - date range
  - usage limit
  - minimum order total
- Calculates discount based on coupon type:
  - PERCENT: percentage of total, capped by maxDiscount if present
  - FIXED: min(value, order total)
  - FREE_SHIPPING: discount 0 (shipping is handled separately in UI)
- Increments usedCount only when the order is created.

Result: coupon usage is consistent and cannot be exploited by repeated apply-coupon calls.

---

## 4) Verified Purchase Logic

### OrderItemRepository
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/repository/OrderItemRepository.java

Added method:
- existsByOrderCustomerIdAndOrderStatusAndProductId(customerId, status, productId)

Purpose: check whether a customer actually bought a product (Delivered status).

### ReviewServiceImpl
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/ReviewServiceImpl.java

Changes:
- createReview now computes verifiedPurchase based on delivered orders.
- The client-provided verifiedPurchase flag is ignored.

Result: verified purchase is always truthful, derived from real order data.

---

## 5) Discount Normalization (Read vs Write)

### ProductServiceImpl
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/ProductServiceImpl.java

Changes:
- Read endpoints now call applyDiscountSnapshot instead of normalizeDiscount.
- applyDiscountSnapshot returns a temporary product with expired discounts cleared.
- It does not persist changes during reads.

Write-time cleanup:
- applyDiscountFromRequest uses clearExpiredDiscount to clean expired discounts on writes only.

Result: no DB writes during GET requests, improving consistency and performance.

### BundleServiceImpl
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/BundleServiceImpl.java

Changes:
- applyBundleDiscountSnapshot clones items for expired discounts without DB writes.
- Avoids unexpected discount resets from read endpoints.

---

## 6) Cart Image Support

### CartItemDTO and EntityMapper
Files:
- Backend/ecommerce-microservice/src/main/java/com/ecommerce/dto/CartItemDTO.java
- Backend/ecommerce-microservice/src/main/java/com/ecommerce/mapper/EntityMapper.java

Changes:
- Added productImageUrl to CartItemDTO.
- EntityMapper now fills productImageUrl from product.imageUrl.

### Frontend Cart Mapping
File: Frontend/src/app/features/shop/cart/shop-cart.component.ts

Changes:
- Cart item images now use productImageUrl when present.
- Fallback placeholders remain for safety.

Result: product images display in cart consistently.

---

## 7) Frontend Auth Identity Binding

### Token-based customerId
Files:
- Frontend/src/app/features/shop/shop.component.ts
- Frontend/src/app/features/shop/catalog/shop-catalog.component.ts
- Frontend/src/app/features/shop/cart/shop-cart.component.ts
- Frontend/src/app/features/shop/product-detail/shop-product-detail.component.ts
- Frontend/src/app/features/shop/bundle-detail/shop-bundle-detail.component.ts

Changes:
- resolveCustomerId now reads from auth token payload (userId/id/sub).
- When no valid user, cart/review actions are blocked with a user-facing error.

Result: consistent identity usage across cart, reviews, and orders.

---

## 8) Comparison API Alignment

### Backend comparison DTO
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/dto/ProductComparisonDTO.java

Purpose: Standardize comparison response items (name, price, rating, stock).

### Frontend comparison usage
Files:
- Frontend/src/app/features/shop/comparison/comparison.component.ts
- Frontend/src/app/services/ecommerce-admin-api.service.ts

Changes:
- Frontend now uses compareProducts endpoint.
- DTO shape aligned to backend (items list).
- UI includes inStock and quantityAvailable for deeper comparison.

---

## 9) Cloudinary Image Upload Flow

### CloudinaryUploadService
File: Frontend/src/app/services/cloudinary-upload.service.ts

Purpose:
- Upload local files to Cloudinary using unsigned presets.
- Returns secure_url for storage in product/bundle imageUrl.

### AuthInterceptor exception for Cloudinary
File: Frontend/src/app/interceptors/auth.interceptor.ts

Change:
- Skips auth headers for Cloudinary upload URLs to avoid CORS preflight rejection.

### ShopAdminComponent upload integration
File: Frontend/src/app/features/shop/admin/shop-admin.component.ts

Changes:
- onProductImageSelected and onBundleImageSelected now upload to Cloudinary.
- Successful upload populates imageUrl + imagePreviewUrl.
- Shows success or error toasts.

---

## 10) Frontend Data Parity and UX Logic (non-HTML)

### Shop landing data
File: Frontend/src/app/features/shop/shop.component.ts

Changes:
- Category cards show counts computed from loaded product data.
- Category icons are derived from category name keywords.

### Catalog data
File: Frontend/src/app/features/shop/catalog/shop-catalog.component.ts

Changes:
- Rating and review count use backend data (averageRating, reviewCount).

### Admin validation helpers
File: Frontend/src/app/features/shop/admin/shop-admin.component.ts

Changes:
- Added isProductFormValid, isCategoryFormValid, isBundleFormValid,
  isCouponFormValid, and isOrderFormValid getters for UI enable/disable.

---

## 11) Configuration (Cloudinary)

Files:
- Frontend/src/app/environments/environment.ts
- Frontend/src/app/environments/environment.prod.ts

Added:
- cloudinaryCloudName
- cloudinaryUploadPreset

Used by CloudinaryUploadService for consistent dev/prod behavior.

---

## 12) Search Analytics and Audit Logs

### SearchLog entity
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/entity/SearchLog.java

Purpose:
- Store keyword, resultCount, searchType, userId, and createdAt for analytics.

Recorded on:
- /api/ecommerce/products/search (SIMPLE)
- /api/ecommerce/products/search/paginated (PAGINATED)
- /api/ecommerce/products/advanced-search (ADVANCED)

### SearchLogServiceImpl
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/SearchLogServiceImpl.java

Logic:
- Validates token (if provided) to attach userId.
- Stores keyword, searchType, and resultCount.

### Search analytics endpoints
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/controller/AnalyticsController.java

Endpoints:
- GET /api/ecommerce/analytics/search/top
- GET /api/ecommerce/analytics/search/zero-results

Both require ADMIN and return SearchKeywordCountDTO list.

### AuditLog entity
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/entity/AuditLog.java

Purpose:
- Trace admin actions with actorId, actorRole, action, entityType, entityId, description.

### AuditLogServiceImpl
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/AuditLogServiceImpl.java

Logic:
- Resolves actorId and actorRole from token.
- Persists action metadata.

### Audit log endpoint
File: Backend/ecommerce-microservice/src/main/java/com/ecommerce/controller/AuditLogController.java

Endpoint:
- GET /api/ecommerce/audit-logs

### Admin UI integration
Files:
- Frontend/src/app/features/shop/admin/shop-admin.component.ts
- Frontend/src/app/features/shop/admin/shop-admin.component.html
- Frontend/src/app/services/ecommerce-admin-api.service.ts

UI panels:
- Top searches
- Zero-result searches
- Recent audit logs

---

## 13) Admin Dashboard Navigation and Filters

Files:
- Frontend/src/app/features/shop/admin/shop-admin.component.html
- Frontend/src/app/features/shop/admin/shop-admin.component.ts
- Frontend/src/app/features/shop/admin/shop-admin.component.css

Changes:
- Added left sidebar navigation with anchor links to each section.
- Added search filters for products, categories, bundles, coupons, orders, and audit logs.
- List panels use fixed-height scroll containers to prevent long lists from breaking layout.
- Product list is limited to 10 visible items with a hint when more are available.

---

## Summary of Functional Impact
- Stronger access control (admin vs customer).
- Trustworthy reviews (verified purchase derived server-side).
- Correct coupon usage accounting.
- No DB writes on read endpoints for discount cleanup.
- Image uploads now reliable and production-ready.
- Comparison, ratings, and cart images are accurate across pages.
