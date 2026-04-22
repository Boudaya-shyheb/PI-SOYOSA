import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';

export interface ApiResponse<T> {
  message: string;
  data: T;
}

export interface ProductDto {
  id: number;
  name: string;
  isbn?: string | null;
  description?: string | null;
  imageUrl?: string | null;
  price: number;
  originalPrice?: number | null;
  discountPercent?: number | null;
  discountEndsAt?: string | null;
  quantityAvailable: number;
  categoryId: number;
  categoryName?: string | null;
  averageRating?: number;
  reviewCount?: number;
}

export interface CategoryDto {
  id: number;
  name: string;
  description?: string | null;
}

export interface OrderItemDto {
  id?: number;
  productId: number;
  quantity: number;
  unitPrice?: number;
  productName?: string | null;
}

export interface OrderDto {
  id: number;
  customerId: number;
  status: string;
  totalPrice: number;
  orderItems: OrderItemDto[];
  createdAt?: string | null;
  updatedAt?: string | null;
  deliveryStreet?: string | null;
  deliveryCity?: string | null;
  deliveryPostalCode?: string | null;
  deliveryCountry?: string | null;
  deliveryLat?: number | null;
  deliveryLng?: number | null;
}

export interface OrderCourierAssignmentDto {
  orderId: number;
  courierId: number;
  courierName?: string | null;
  assignedBy?: number | null;
  assignedAt?: string | null;
}

export interface CartItemDto {
  id?: number;
  itemType?: 'PRODUCT' | 'BUNDLE';
  productId?: number | null;
  productName?: string | null;
  productPrice?: number | null;
  productImageUrl?: string | null;
  bundleId?: number | null;
  bundleName?: string | null;
  bundlePrice?: number | null;
  bundleImageUrl?: string | null;
  quantity: number;
  subtotal?: number | null;
}

export interface CartDto {
  id: number;
  customerId: number;
  cartItems: CartItemDto[];
  total?: number | null;
  totalItems?: number | null;
}

export interface AddToCartRequest {
  productId: number;
  quantity: number;
}

export interface BundleItemDto {
  id?: number;
  productId: number;
  productName?: string | null;
  productImageUrl?: string | null;
  categoryName?: string | null;
  quantity: number;
  productPrice?: number | null;
}

export interface BundleDto {
  id: number;
  name: string;
  description?: string | null;
  imageUrl?: string | null;
  price: number;
  status: 'ACTIVE' | 'DRAFT';
  items: BundleItemDto[];
}

export interface CreateProductRequest {
  name: string;
  isbn?: string | null;
  description?: string | null;
  imageUrl?: string | null;
  price: number;
  originalPrice?: number | null;
  discountPercent?: number | null;
  discountEndsAt?: string | null;
  quantityAvailable: number;
  categoryId: number;
}

export interface CreateCategoryRequest {
  name: string;
  description?: string | null;
}

export interface CreateOrderItemRequest {
  productId: number;
  quantity: number;
}

export interface CreateOrderRequest {
  customerId: number;
  items: CreateOrderItemRequest[];
  discountAmount?: number;
  couponCode?: string;
  deliveryStreet?: string;
  deliveryCity?: string;
  deliveryPostalCode?: string;
  deliveryCountry?: string;
  deliveryLat?: number;
  deliveryLng?: number;
}

export interface CreateBundleItemRequest {
  productId: number;
  quantity: number;
}

export interface CreateBundleRequest {
  name: string;
  description?: string | null;
  imageUrl?: string | null;
  price: number;
  status: 'ACTIVE' | 'DRAFT';
  items: CreateBundleItemRequest[];
}

export interface CouponDto {
  id: number;
  code: string;
  description?: string | null;
  type: 'PERCENT' | 'FIXED' | 'FREE_SHIPPING';
  value: number;
  minOrderTotal?: number | null;
  maxDiscount?: number | null;
  active: boolean;
  usageLimit?: number | null;
  usedCount?: number | null;
  startsAt?: string | null;
  endsAt?: string | null;
}

export interface CreateCouponRequest {
  code: string;
  description?: string | null;
  type: 'PERCENT' | 'FIXED' | 'FREE_SHIPPING';
  value: number;
  minOrderTotal?: number | null;
  maxDiscount?: number | null;
  active: boolean;
  usageLimit?: number | null;
  startsAt?: string | null;
  endsAt?: string | null;
}

export interface CouponApplyResponse {
  code: string;
  type: 'PERCENT' | 'FIXED' | 'FREE_SHIPPING';
  discountAmount: number;
  freeShipping: boolean;
}

export interface ExternalProductInsightDto {
  productId?: number | null;
  source: string;
  sourceId?: string | null;
  isbn?: string | null;
  title?: string | null;
  authors?: string | null;
  publisher?: string | null;
  publishedDate?: string | null;
  pageCount?: number | null;
  language?: string | null;
  categories?: string | null;
  averageRating?: number | null;
  ratingsCount?: number | null;
  thumbnailUrl?: string | null;
  infoLink?: string | null;
  updatedAt?: string | null;
}

export interface ExternalInsightRecordDto {
  id: number;
  source: string;
  sourceId?: string | null;
  isbn?: string | null;
  title?: string | null;
  authors?: string | null;
  publisher?: string | null;
  publishedDate?: string | null;
  pageCount?: number | null;
  language?: string | null;
  categories?: string | null;
  averageRating?: number | null;
  ratingsCount?: number | null;
  infoLink?: string | null;
  updatedAt?: string | null;
  matched: boolean;
  productId?: number | null;
  productName?: string | null;
  productIsbn?: string | null;
  productPrice?: number | null;
  productCategoryName?: string | null;
  productAverageRating?: number | null;
  productReviewCount?: number | null;
}

export interface ExternalInsightsSummaryDto {
  productsWithInsights: number;
  totalRecords: number;
  openAlexCount: number;
  crossrefCount: number;
  lastUpdatedAt?: string | null;
}

export interface ReviewDto {
  id?: number;
  productId: number;
  customerId: number;
  rating: number; // 1-5
  title: string;
  comment: string;
  helpfulCount?: number;
  unhelpfulCount?: number;
  verifiedPurchase?: boolean;
  replies?: ReviewReplyDto[];
  attachments?: ReviewAttachmentDto[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ReviewReplyDto {
  id?: number;
  reviewId: number;
  responderId: number;
  responderType: string;
  comment: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ReviewAttachmentDto {
  id?: number;
  reviewId: number;
  attachmentUrl: string;
  attachmentType: string;
  createdAt?: string;
}

export interface CreateReviewRequest {
  productId: number;
  customerId: number;
  rating: number;
  title: string;
  comment: string;
  verifiedPurchase?: boolean;
}

// ===== ADVANCED SEARCH & PAGINATION INTERFACES =====
export interface SearchRequest {
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
  maxRating?: number;
  categoryId?: number;
  sortBy?: string;
  sortDirection?: string;
}

export interface PageResponse<T> {
  content: T[];
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isFirst: boolean;
  isLast: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

// ===== RECOMMENDATIONS INTERFACE =====
export interface RelatedProductsDTO {
  relatedProducts: ProductDto[];
  bestSellers: ProductDto[];
  trending: ProductDto[];
}

// ===== COMPARISON INTERFACE =====
export interface ComparisonRequest {
  productIds: number[];
}

export interface ProductComparisonDTO {
  items: ComparisonItemDto[];
}

export interface ComparisonItemDto {
  id: number;
  name: string;
  description?: string | null;
  price: number;
  quantityAvailable?: number | null;
  averageRating?: number | null;
  reviewCount?: number | null;
  imageUrl?: string | null;
  category?: CategoryDto | null;
  inStock?: boolean | null;
}

export interface SearchKeywordCountDto {
  keyword: string;
  count: number;
}

export interface AuditLogDto {
  id: number;
  actorId?: string | null;
  actorRole?: string | null;
  action: string;
  entityType: string;
  entityId?: number | null;
  description?: string | null;
  createdAt?: string;
}

@Injectable({ providedIn: 'root' })
export class EcommerceAdminApiService {
  private readonly baseUrl = `${API_BASE_URL}/ecommerce`;

  constructor(private http: HttpClient, private headers: ApiHeadersService) { }

  listProducts(): Observable<ApiResponse<ProductDto[]>> {
    return this.http.get<ApiResponse<ProductDto[]>>(`${this.baseUrl}/products`);
  }

  getProductById(id: number): Observable<ApiResponse<ProductDto>> {
    return this.http.get<ApiResponse<ProductDto>>(`${this.baseUrl}/products/${id}`);
  }

  getProductsByCategory(categoryId: number): Observable<ApiResponse<ProductDto[]>> {
    return this.http.get<ApiResponse<ProductDto[]>>(`${this.baseUrl}/products/category/${categoryId}`);
  }

  searchProducts(keyword: string): Observable<ApiResponse<ProductDto[]>> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<ApiResponse<ProductDto[]>>(`${this.baseUrl}/products/search`, { params });
  }

  createProduct(request: CreateProductRequest): Observable<ApiResponse<ProductDto>> {
    return this.http.post<ApiResponse<ProductDto>>(`${this.baseUrl}/products`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  updateProduct(id: number, request: CreateProductRequest): Observable<ApiResponse<ProductDto>> {
    return this.http.put<ApiResponse<ProductDto>>(`${this.baseUrl}/products/${id}`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  getProductInsights(productId: number): Observable<ApiResponse<ExternalProductInsightDto[]>> {
    return this.http.get<ApiResponse<ExternalProductInsightDto[]>>(`${this.baseUrl}/products/${productId}/insights`, {
      headers: this.headers.buildHeaders()
    });
  }

  getExternalInsightsSummary(): Observable<ApiResponse<ExternalInsightsSummaryDto>> {
    return this.http.get<ApiResponse<ExternalInsightsSummaryDto>>(`${this.baseUrl}/products/insights/summary`, {
      headers: this.headers.buildHeaders()
    });
  }

  getExternalInsightsCatalog(page = 0, size = 25): Observable<ApiResponse<PageResponse<ExternalInsightRecordDto>>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<ApiResponse<PageResponse<ExternalInsightRecordDto>>>(`${this.baseUrl}/products/insights/catalog`, {
      headers: this.headers.buildHeaders(),
      params
    });
  }

  deleteProduct(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/products/${id}`, {
      headers: this.headers.buildHeaders()
    });
  }

  listCategories(): Observable<ApiResponse<CategoryDto[]>> {
    return this.http.get<ApiResponse<CategoryDto[]>>(`${this.baseUrl}/categories`);
  }

  getCategoryById(id: number): Observable<ApiResponse<CategoryDto>> {
    return this.http.get<ApiResponse<CategoryDto>>(`${this.baseUrl}/categories/${id}`);
  }

  createCategory(request: CreateCategoryRequest): Observable<ApiResponse<CategoryDto>> {
    return this.http.post<ApiResponse<CategoryDto>>(`${this.baseUrl}/categories`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  updateCategory(id: number, request: CreateCategoryRequest): Observable<ApiResponse<CategoryDto>> {
    return this.http.put<ApiResponse<CategoryDto>>(`${this.baseUrl}/categories/${id}`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  deleteCategory(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/categories/${id}`, {
      headers: this.headers.buildHeaders()
    });
  }

  listOrders(): Observable<ApiResponse<OrderDto[]>> {
    return this.http.get<ApiResponse<OrderDto[]>>(`${this.baseUrl}/orders`, {
      headers: this.headers.buildHeaders()
    });
  }

  getOrderById(id: number): Observable<ApiResponse<OrderDto>> {
    return this.http.get<ApiResponse<OrderDto>>(`${this.baseUrl}/orders/${id}`, {
      headers: this.headers.buildHeaders()
    });
  }

  listOrdersByCustomer(customerId: number): Observable<ApiResponse<OrderDto[]>> {
    return this.http.get<ApiResponse<OrderDto[]>>(`${this.baseUrl}/orders/customer/${customerId}`, {
      headers: this.headers.buildHeaders()
    });
  }

  listCourierAssignments(): Observable<ApiResponse<OrderCourierAssignmentDto[]>> {
    return this.http.get<ApiResponse<OrderCourierAssignmentDto[]>>(
      `${this.baseUrl}/orders/courier-assignments`,
      { headers: this.headers.buildHeaders() }
    );
  }

  createOrder(request: CreateOrderRequest): Observable<ApiResponse<OrderDto>> {
    return this.http.post<ApiResponse<OrderDto>>(`${this.baseUrl}/orders`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  updateOrderStatus(id: number, status: string): Observable<ApiResponse<OrderDto>> {
    const params = new HttpParams().set('status', status);
    return this.http.put<ApiResponse<OrderDto>>(`${this.baseUrl}/orders/${id}/status`, null, {
      params,
      headers: this.headers.buildHeaders()
    });
  }

  cancelOrder(id: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/orders/${id}/cancel`, null, {
      headers: this.headers.buildHeaders()
    });
  }

  assignCourier(orderId: number, courierId: number, courierName?: string | null): Observable<ApiResponse<OrderCourierAssignmentDto>> {
    const params = courierName ? new HttpParams().set('courierName', courierName) : undefined;
    return this.http.put<ApiResponse<OrderCourierAssignmentDto>>(
      `${this.baseUrl}/orders/${orderId}/assign-courier/${courierId}`,
      null,
      { headers: this.headers.buildHeaders(), params }
    );
  }

  unassignCourier(orderId: number): Observable<ApiResponse<OrderCourierAssignmentDto>> {
    return this.http.delete<ApiResponse<OrderCourierAssignmentDto>>(
      `${this.baseUrl}/orders/${orderId}/unassign-courier`,
      { headers: this.headers.buildHeaders() }
    );
  }

  getCart(customerId: number): Observable<ApiResponse<CartDto>> {
    return this.http.get<ApiResponse<CartDto>>(`${this.baseUrl}/carts/customer/${customerId}`);
  }

  addToCart(customerId: number, request: AddToCartRequest): Observable<ApiResponse<CartDto>> {
    return this.http.post<ApiResponse<CartDto>>(`${this.baseUrl}/carts/customer/${customerId}/add-item`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  addBundleToCart(customerId: number, bundleId: number, quantity = 1): Observable<ApiResponse<CartDto>> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.post<ApiResponse<CartDto>>(`${this.baseUrl}/carts/customer/${customerId}/add-bundle/${bundleId}`, null, {
      params,
      headers: this.headers.buildHeaders()
    });
  }

  removeFromCart(customerId: number, productId: number): Observable<ApiResponse<CartDto>> {
    return this.http.delete<ApiResponse<CartDto>>(`${this.baseUrl}/carts/customer/${customerId}/remove-item/${productId}`, {
      headers: this.headers.buildHeaders()
    });
  }

  removeBundleFromCart(customerId: number, bundleId: number): Observable<ApiResponse<CartDto>> {
    return this.http.delete<ApiResponse<CartDto>>(`${this.baseUrl}/carts/customer/${customerId}/remove-bundle/${bundleId}`, {
      headers: this.headers.buildHeaders()
    });
  }

  updateCartItemQuantity(customerId: number, productId: number, quantity: number): Observable<ApiResponse<CartDto>> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.put<ApiResponse<CartDto>>(`${this.baseUrl}/carts/customer/${customerId}/update-item/${productId}`, null, {
      params,
      headers: this.headers.buildHeaders()
    });
  }

  updateBundleItemQuantity(customerId: number, bundleId: number, quantity: number): Observable<ApiResponse<CartDto>> {
    const params = new HttpParams().set('quantity', quantity.toString());
    return this.http.put<ApiResponse<CartDto>>(`${this.baseUrl}/carts/customer/${customerId}/update-bundle/${bundleId}`, null, {
      params,
      headers: this.headers.buildHeaders()
    });
  }

  clearCart(customerId: number): Observable<ApiResponse<CartDto>> {
    return this.http.delete<ApiResponse<CartDto>>(`${this.baseUrl}/carts/customer/${customerId}/clear`, {
      headers: this.headers.buildHeaders()
    });
  }

  applyCoupon(customerId: number, code: string): Observable<ApiResponse<CouponApplyResponse>> {
    const params = new HttpParams().set('code', code);
    return this.http.post<ApiResponse<CouponApplyResponse>>(`${this.baseUrl}/carts/customer/${customerId}/apply-coupon`, null, {
      params,
      headers: this.headers.buildHeaders()
    });
  }

  listBundles(): Observable<ApiResponse<BundleDto[]>> {
    return this.http.get<ApiResponse<BundleDto[]>>(`${this.baseUrl}/bundles`);
  }

  getBundleById(id: number): Observable<ApiResponse<BundleDto>> {
    return this.http.get<ApiResponse<BundleDto>>(`${this.baseUrl}/bundles/${id}`);
  }

  createBundle(request: CreateBundleRequest): Observable<ApiResponse<BundleDto>> {
    return this.http.post<ApiResponse<BundleDto>>(`${this.baseUrl}/bundles`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  updateBundle(id: number, request: CreateBundleRequest): Observable<ApiResponse<BundleDto>> {
    return this.http.put<ApiResponse<BundleDto>>(`${this.baseUrl}/bundles/${id}`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  deleteBundle(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/bundles/${id}`, {
      headers: this.headers.buildHeaders()
    });
  }

  listCoupons(): Observable<ApiResponse<CouponDto[]>> {
    return this.http.get<ApiResponse<CouponDto[]>>(`${this.baseUrl}/coupons`);
  }

  createCoupon(request: CreateCouponRequest): Observable<ApiResponse<CouponDto>> {
    return this.http.post<ApiResponse<CouponDto>>(`${this.baseUrl}/coupons`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  updateCoupon(id: number, request: CreateCouponRequest): Observable<ApiResponse<CouponDto>> {
    return this.http.put<ApiResponse<CouponDto>>(`${this.baseUrl}/coupons/${id}`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  deleteCoupon(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/coupons/${id}`, {
      headers: this.headers.buildHeaders()
    });
  }

  // Review APIs
  getProductReviews(productId: number): Observable<ApiResponse<ReviewDto[]>> {
    return this.http.get<ApiResponse<ReviewDto[]>>(`${this.baseUrl}/reviews/product/${productId}`);
  }

  getProductVerifiedReviews(productId: number): Observable<ApiResponse<ReviewDto[]>> {
    return this.http.get<ApiResponse<ReviewDto[]>>(`${this.baseUrl}/reviews/product/${productId}/verified`);
  }

  getCustomerReviews(customerId: number): Observable<ApiResponse<ReviewDto[]>> {
    return this.http.get<ApiResponse<ReviewDto[]>>(`${this.baseUrl}/reviews/customer/${customerId}`);
  }

  getReview(id: number): Observable<ApiResponse<ReviewDto>> {
    return this.http.get<ApiResponse<ReviewDto>>(`${this.baseUrl}/reviews/${id}`);
  }

  createReview(request: CreateReviewRequest): Observable<ApiResponse<ReviewDto>> {
    return this.http.post<ApiResponse<ReviewDto>>(`${this.baseUrl}/reviews`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  updateReview(id: number, request: CreateReviewRequest): Observable<ApiResponse<ReviewDto>> {
    return this.http.put<ApiResponse<ReviewDto>>(`${this.baseUrl}/reviews/${id}`, request, {
      headers: this.headers.buildHeaders()
    });
  }

  deleteReview(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/reviews/${id}`, {
      headers: this.headers.buildHeaders()
    });
  }

  markReviewHelpful(id: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/reviews/${id}/helpful`, null, {
      headers: this.headers.buildHeaders()
    });
  }

  markReviewUnhelpful(id: number): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.baseUrl}/reviews/${id}/unhelpful`, null, {
      headers: this.headers.buildHeaders()
    });
  }

  addReviewReply(reviewId: number, reply: ReviewReplyDto): Observable<ApiResponse<ReviewReplyDto>> {
    return this.http.post<ApiResponse<ReviewReplyDto>>(`${this.baseUrl}/reviews/${reviewId}/replies`, reply, {
      headers: this.headers.buildHeaders()
    });
  }

  addReviewAttachment(reviewId: number, attachment: ReviewAttachmentDto): Observable<ApiResponse<ReviewAttachmentDto>> {
    return this.http.post<ApiResponse<ReviewAttachmentDto>>(`${this.baseUrl}/reviews/${reviewId}/attachments`, attachment, {
      headers: this.headers.buildHeaders()
    });
  }

  // ===== PAGINATION METHOD =====
  getProductsPaginated(
    page: number = 0,
    size: number = 10,
    sortBy: string = 'id',
    sortDirection: string = 'asc'
  ): Observable<ApiResponse<PageResponse<ProductDto>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDirection', sortDirection);
    return this.http.get<ApiResponse<PageResponse<ProductDto>>>(`${this.baseUrl}/products/paginated`, { params });
  }

  // ===== ADVANCED SEARCH METHOD =====
  advancedSearch(
    searchRequest: SearchRequest,
    page: number = 0,
    size: number = 10
  ): Observable<ApiResponse<PageResponse<ProductDto>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.post<ApiResponse<PageResponse<ProductDto>>>(
      `${this.baseUrl}/products/advanced-search`,
      searchRequest,
      { params, headers: this.headers.buildHeaders() }
    );
  }

  // ===== RECOMMENDATIONS METHOD =====
  getRecommendations(productId: number): Observable<ApiResponse<RelatedProductsDTO>> {
    return this.http.get<ApiResponse<RelatedProductsDTO>>(
      `${this.baseUrl}/products/${productId}/recommendations`
    );
  }

  // ===== COMPARISON METHOD =====
  compareProducts(comparisonRequest: ComparisonRequest): Observable<ApiResponse<ProductComparisonDTO>> {
    return this.http.post<ApiResponse<ProductComparisonDTO>>(
      `${this.baseUrl}/products/compare`,
      comparisonRequest,
      { headers: this.headers.buildHeaders() }
    );
  }

  getTopSearches(days: number = 30, limit: number = 10): Observable<ApiResponse<SearchKeywordCountDto[]>> {
    const params = new HttpParams()
      .set('days', days.toString())
      .set('limit', limit.toString());
    return this.http.get<ApiResponse<SearchKeywordCountDto[]>>(`${this.baseUrl}/analytics/search/top`, {
      params,
      headers: this.headers.buildHeaders()
    });
  }

  getZeroResultSearches(days: number = 30, limit: number = 10): Observable<ApiResponse<SearchKeywordCountDto[]>> {
    const params = new HttpParams()
      .set('days', days.toString())
      .set('limit', limit.toString());
    return this.http.get<ApiResponse<SearchKeywordCountDto[]>>(`${this.baseUrl}/analytics/search/zero-results`, {
      params,
      headers: this.headers.buildHeaders()
    });
  }

  getAuditLogs(limit: number = 25): Observable<ApiResponse<AuditLogDto[]>> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<ApiResponse<AuditLogDto[]>>(`${this.baseUrl}/audit-logs`, {
      params,
      headers: this.headers.buildHeaders()
    });
  }
}
