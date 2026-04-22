# Phase 1 Update - Order Access Controls

Date: 2026-03-27
Scope: Ecommerce microservice only

## What changed
- Restricted order list endpoints to admin access.
- Restricted single order lookup to admin access.

## Files updated
- Backend/ecommerce-microservice/src/main/java/com/ecommerce/controller/OrderController.java

## Endpoint behavior
- GET /api/ecommerce/orders
  - Before: any valid token
  - Now: ADMIN only

- GET /api/ecommerce/orders/paginated
  - Before: any valid token
  - Now: ADMIN only

- GET /api/ecommerce/orders/{id}
  - Before: no auth
  - Now: ADMIN only

## Notes
- Customer-scoped endpoints remain unchanged:
  - GET /api/ecommerce/orders/customer/{customerId}
  - GET /api/ecommerce/orders/customer/{customerId}/paginated
  - POST /api/ecommerce/orders

## Next steps
- Phase 1 Step 2: Enforce review ownership (owner or admin) for update/delete.
- Phase 1 Step 3: Bind customerId in frontend to token identity (remove local fallback id).

## Phase 1 Step 3 changes
- Shop UI now derives customerId from the auth token payload and blocks cart/review actions when not logged in.
- Removed localStorage fallback id usage in shop components.

## Files updated (Step 3)
- Frontend/src/app/features/shop/shop.component.ts
- Frontend/src/app/features/shop/catalog/shop-catalog.component.ts
- Frontend/src/app/features/shop/cart/shop-cart.component.ts
- Frontend/src/app/features/shop/product-detail/shop-product-detail.component.ts
- Frontend/src/app/features/shop/bundle-detail/shop-bundle-detail.component.ts

## Phase 2 Step 1 changes (Data integrity)
- Coupon usage is now incremented at order creation, not at apply-coupon.
- Order creation now validates coupon codes server-side.
- Orders require a coupon code when a discount is applied.

## Files updated (Phase 2 Step 1)
- Backend/ecommerce-microservice/src/main/java/com/ecommerce/dto/CreateOrderRequest.java
- Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/CartServiceImpl.java
- Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/OrderServiceImpl.java
- Frontend/src/app/services/ecommerce-admin-api.service.ts
- Frontend/src/app/features/shop/cart/shop-cart.component.ts

## Phase 2 Step 2 changes (Verified purchase)
- Review verified purchase is now derived from delivered orders.
- Client-provided verifiedPurchase flag is ignored.

## Files updated (Phase 2 Step 2)
- Backend/ecommerce-microservice/src/main/java/com/ecommerce/repository/OrderItemRepository.java
- Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/ReviewServiceImpl.java

## Phase 2 Step 3 changes (Discount snapshot)
- Read endpoints no longer persist discount normalization.
- Expired discounts are cleared in returned DTOs without writing to DB.

## Files updated (Phase 2 Step 3)
- Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/ProductServiceImpl.java
- Backend/ecommerce-microservice/src/main/java/com/ecommerce/service/impl/BundleServiceImpl.java

## Phase 3 Step 1 changes (UX/data parity)
- Category cards now show product counts and icons derived from live data.
- Catalog and comparison views now use real rating and review counts from the API.

## Files updated (Phase 3 Step 1)
- Frontend/src/app/features/shop/shop.component.ts
- Frontend/src/app/features/shop/catalog/shop-catalog.component.ts
- Frontend/src/app/features/shop/comparison/comparison.component.ts

## Phase 3 Step 2 changes (Comparison data)
- Comparison page now uses the backend compare endpoint for consistent data.

## Files updated (Phase 3 Step 2)
- Frontend/src/app/features/shop/comparison/comparison.component.ts

## Phase 3 Step 3 changes (Admin images)
- Admin product/bundle image uploads now preview locally but require a public URL to save.
- Data URLs are blocked to avoid storing base64 in the database.

## Files updated (Phase 3 Step 3)
- Frontend/src/app/features/shop/admin/shop-admin.component.ts

## Frontend verification checklist
- Shop landing: category cards show counts and icons; click a category to filter catalog.
- Catalog: ratings and review counts match product data (not hardcoded).
- Comparison: load comparison page, verify data matches API compare results.
- Admin images: selecting a local file shows preview but saving requires a URL.
- Admin lists show empty-state cards when data is missing.
- Bundles page shows an empty-state when no bundles exist.

## Quick tests
1) Login, open /shop/admin, try selecting a local image and saving.
  - Expect a toast warning and save blocked without a URL.
2) Open /shop/catalog and confirm rating/review values match backend.
3) Add two products to compare and open /shop/comparison.
  - Expect attributes (price, rating, stock) to reflect backend data.
4) Clear all bundles/products in DB and refresh admin lists to see empty-state cards.
