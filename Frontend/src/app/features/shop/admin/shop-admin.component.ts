import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
import {
  BundleDto,
  CategoryDto,
  CouponDto,
  CreateCategoryRequest,
  CreateBundleRequest,
  CreateCouponRequest,
  CreateOrderRequest,
  CreateProductRequest,
  AuditLogDto,
  EcommerceAdminApiService,
  OrderDto,
  OrderCourierAssignmentDto,
  ProductDto,
  SearchKeywordCountDto
} from '../../../services/ecommerce-admin-api.service';
import { UserInfoDto, UserLookupService } from '../../../services/user-lookup.service';
import { CloudinaryUploadService } from '../../../services/cloudinary-upload.service';

@Component({
  selector: 'app-shop-admin',
  templateUrl: './shop-admin.component.html',
  styleUrls: ['./shop-admin.component.css']
})
export class ShopAdminComponent implements OnInit {
  loading = false;
  successMessage = '';
  errorMessage = '';
  toasts: { id: number; type: 'success' | 'error' | 'info'; message: string }[] = [];
  private toastId = 0;

  stats = [
    { label: 'Products', value: 0, note: 'Ready' },
    { label: 'Categories', value: 0, note: 'Ready' },
    { label: 'Bundles', value: 0, note: 'Ready' },
    { label: 'Orders', value: 0, note: 'Ready' },
    { label: 'Coupons', value: 0, note: 'Ready' }
  ];

  products: ProductDto[] = [];
  categories: CategoryDto[] = [];
  orders: OrderDto[] = [];
  courierAssignments: OrderCourierAssignmentDto[] = [];
  bundles: BundleDto[] = [];
  coupons: CouponDto[] = [];
  courierCandidates: UserInfoDto[] = [];
  courierUsername = '';
  courierLookupLoading = false;
  courierDropdownOpen = false;
  courierNameById: Record<number, string> = {};
  topSearches: SearchKeywordCountDto[] = [];
  zeroResultSearches: SearchKeywordCountDto[] = [];
  auditLogs: AuditLogDto[] = [];
  analyticsLoading = false;
  expandedOrders = new Set<number>();
  orderStatusDrafts: Record<number, string> = {};
  selectedOrderSummary: { items: number; total: number; status: string } | null = null;
  selectedOrderId: number | null = null;
  isImageUploading = false;
  productQuery = '';
  categoryQuery = '';
  bundleQuery = '';
  couponQuery = '';
  orderQuery = '';
  orderDateFrom = '';
  orderDateTo = '';
  auditQuery = '';
  activeSection: 'overview' | 'products' | 'categories' | 'bundles' | 'coupons' | 'orders' | 'analytics' = 'overview';
  productPage = 1;
  productPageSize = 10;
  orderPage = 1;
  orderPageSize = 6;
  categoryPage = 1;
  categoryPageSize = 8;
  bundlePage = 1;
  bundlePageSize = 8;
  couponPage = 1;
  couponPageSize = 8;
  auditPage = 1;
  auditPageSize = 8;

  productForm = {
    id: null as number | null,
    name: '',
    isbn: '',
    description: '',
    imageUrl: '',
    imagePreviewUrl: '',
    price: null as number | null,
    originalPrice: null as number | null,
    discountPercent: null as number | null,
    discountEndsAt: '' as string,
    quantityAvailable: null as number | null,
    categoryId: null as number | null
  };

  categoryForm = {
    id: null as number | null,
    name: '',
    description: ''
  };

  bundleForm = {
    id: null as number | null,
    name: '',
    description: '',
    imageUrl: '',
    imagePreviewUrl: '',
    price: null as number | null,
    status: 'DRAFT' as 'ACTIVE' | 'DRAFT',
    selectedItems: [] as { productId: number; quantity: number }[]
  };

  orderForm = {
    customerId: null as number | null,
    selectedItems: [] as { productId: number; quantity: number }[]
  };

  couponForm = {
    id: null as number | null,
    code: '',
    description: '',
    type: 'PERCENT' as 'PERCENT' | 'FIXED' | 'FREE_SHIPPING',
    value: null as number | null,
    minOrderTotal: null as number | null,
    maxDiscount: null as number | null,
    active: true,
    usageLimit: null as number | null,
    startsAt: '',
    endsAt: ''
  };

  orderStatusForm = {
    id: null as number | null,
    status: 'PENDING'
  };

  courierAssignmentForm = {
    orderId: null as number | null,
    courierId: null as number | null
  };

  isBundlePickerOpen = false;
  isOrderPickerOpen = false;

  constructor(
    private api: EcommerceAdminApiService,
    private cloudinary: CloudinaryUploadService,
    private userLookup: UserLookupService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const section = (params.get('section') || 'overview').toLowerCase();
      if (this.isValidSection(section)) {
        this.activeSection = section as typeof this.activeSection;
      } else {
        this.activeSection = 'overview';
        this.router.navigate(['/shop/admin']);
      }
    });
    this.refreshData();
  }

  isSection(section: string): boolean {
    return this.activeSection === section;
  }

  getSectionTitle(): string {
    switch (this.activeSection) {
      case 'products':
        return 'Product management';
      case 'categories':
        return 'Category management';
      case 'bundles':
        return 'Bundle management';
      case 'coupons':
        return 'Coupon management';
      case 'orders':
        return 'Order management';
      case 'analytics':
        return 'Analytics overview';
      default:
        return 'Admin overview';
    }
  }

  getOrderCountByStatus(status: string): number {
    const target = status.toLowerCase();
    return this.orders.filter((order) => (order.status || '').toLowerCase() === target).length;
  }

  getSectionSearch(): string {
    switch (this.activeSection) {
      case 'products':
        return this.productQuery;
      case 'categories':
        return this.categoryQuery;
      case 'bundles':
        return this.bundleQuery;
      case 'coupons':
        return this.couponQuery;
      case 'orders':
        return this.orderQuery;
      case 'analytics':
        return this.auditQuery;
      default:
        return '';
    }
  }

  setSectionSearch(value: string): void {
    switch (this.activeSection) {
      case 'products':
        this.productQuery = value;
        break;
      case 'categories':
        this.categoryQuery = value;
        break;
      case 'bundles':
        this.bundleQuery = value;
        break;
      case 'coupons':
        this.couponQuery = value;
        break;
      case 'orders':
        this.orderQuery = value;
        break;
      case 'analytics':
        this.auditQuery = value;
        break;
      default:
        break;
    }
  }

  private isValidSection(section: string): boolean {
    return ['overview', 'products', 'categories', 'bundles', 'coupons', 'orders', 'analytics'].includes(section);
  }

  refreshData(): void {
    this.loading = true;
    this.clearMessages();
    forkJoin({
      products: this.api.listProducts(),
      categories: this.api.listCategories(),
      orders: this.api.listOrders(),
      courierAssignments: this.api.listCourierAssignments(),
      bundles: this.api.listBundles(),
      coupons: this.api.listCoupons()
    }).subscribe({
      next: ({ products, categories, orders, courierAssignments, bundles, coupons }) => {
        this.products = products.data ?? [];
        this.categories = categories.data ?? [];
        this.orders = orders.data ?? [];
        this.courierAssignments = courierAssignments.data ?? [];
        this.courierNameById = (courierAssignments.data ?? []).reduce<Record<number, string>>((acc, item) => {
          if (item.courierId && item.courierName) {
            acc[item.courierId] = item.courierName;
          }
          return acc;
        }, {});
        this.bundles = bundles.data ?? [];
        this.coupons = coupons.data ?? [];
        this.syncOrderStatusDrafts();
        this.updateStats();
        this.loading = false;
        this.loadAnalytics();
      },
      error: (error) => this.handleError(error)
    });
  }

  private loadAnalytics(): void {
    this.analyticsLoading = true;
    forkJoin({
      topSearches: this.api.getTopSearches(30, 6),
      zeroResults: this.api.getZeroResultSearches(30, 6),
      auditLogs: this.api.getAuditLogs(12)
    }).subscribe({
      next: ({ topSearches, zeroResults, auditLogs }) => {
        this.topSearches = topSearches.data ?? [];
        this.zeroResultSearches = zeroResults.data ?? [];
        this.auditLogs = auditLogs.data ?? [];
        this.analyticsLoading = false;
      },
      error: () => {
        this.analyticsLoading = false;
      }
    });
  }

  getMaxSearchCount(list: SearchKeywordCountDto[]): number {
    const safeList = Array.isArray(list) ? list : [];
    if (!safeList.length) {
      return 0;
    }
    const counts = safeList.map((item) => item.count || 0);
    return Math.max.apply(Math, counts);
  }

  getBarWidth(count: number, max: number): string {
    if (!max || max <= 0) {
      return '0%';
    }
    const ratio = Math.min(count / max, 1);
    return `${Math.round(ratio * 100)}%`;
  }

  get isProductFormValid(): boolean {
    return !!this.productForm.name.trim()
      && this.productForm.price !== null
      && Number(this.productForm.price) > 0
      && this.productForm.quantityAvailable !== null
      && Number(this.productForm.quantityAvailable) > 0
      && this.productForm.categoryId !== null;
  }

  get isCategoryFormValid(): boolean {
    return !!this.categoryForm.name.trim();
  }

  get isBundleFormValid(): boolean {
    return !!this.bundleForm.name.trim()
      && this.bundleForm.price !== null
      && Number(this.bundleForm.price) > 0
      && this.bundleForm.selectedItems.length > 0;
  }

  get isCouponFormValid(): boolean {
    return !!this.couponForm.code.trim()
      && this.couponForm.value !== null
      && Number(this.couponForm.value) > 0;
  }

  get isOrderFormValid(): boolean {
    return this.orderForm.customerId !== null
      && Number(this.orderForm.customerId) > 0
      && this.orderForm.selectedItems.length > 0;
  }

  get filteredProducts(): ProductDto[] {
    const term = this.productQuery.trim().toLowerCase();
    if (!term) {
      return this.products;
    }
    return this.products.filter((product) => {
      const name = (product.name || '').toLowerCase();
      const category = (product.categoryName || '').toLowerCase();
      return name.includes(term) || category.includes(term);
    });
  }

  get pagedProducts(): ProductDto[] {
    const start = (this.productPage - 1) * this.productPageSize;
    return this.filteredProducts.slice(start, start + this.productPageSize);
  }

  get productTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredProducts.length / this.productPageSize));
  }

  goToProductPage(page: number): void {
    const safePage = Math.max(1, Math.min(page, this.productTotalPages));
    this.productPage = safePage;
  }

  resetProductPagination(): void {
    this.productPage = 1;
  }

  get filteredCategories(): CategoryDto[] {
    const term = this.categoryQuery.trim().toLowerCase();
    if (!term) {
      return this.categories;
    }
    return this.categories.filter((category) =>
      (category.name || '').toLowerCase().includes(term)
    );
  }

  get pagedCategories(): CategoryDto[] {
    const start = (this.categoryPage - 1) * this.categoryPageSize;
    return this.filteredCategories.slice(start, start + this.categoryPageSize);
  }

  get categoryTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredCategories.length / this.categoryPageSize));
  }

  goToCategoryPage(page: number): void {
    const safePage = Math.max(1, Math.min(page, this.categoryTotalPages));
    this.categoryPage = safePage;
  }

  resetCategoryPagination(): void {
    this.categoryPage = 1;
  }

  getCategoryProductCount(categoryId?: number | null): number {
    if (!categoryId) {
      return 0;
    }
    return this.products.filter((product) => product.categoryId === categoryId).length;
  }

  getLowStockCount(threshold: number = 5): number {
    return this.products.filter((product) => (product.quantityAvailable ?? 0) <= threshold).length;
  }

  getDiscountedProductCount(): number {
    return this.products.filter((product) => (product.discountPercent ?? 0) > 0).length;
  }

  getZeroProductCategoriesCount(): number {
    return this.categories.filter((category) => this.getCategoryProductCount(category.id) === 0).length;
  }

  getBundleStatusCount(status: 'ACTIVE' | 'DRAFT'): number {
    return this.bundles.filter((bundle) => (bundle.status || 'DRAFT') === status).length;
  }

  getCouponActiveCount(active: boolean): number {
    return this.coupons.filter((coupon) => (coupon.active ?? false) === active).length;
  }

  getMaxValue(...values: number[]): number {
    const safeValues = Array.isArray(values) ? values : [];
    if (!safeValues.length) {
      return 0;
    }
    return Math.max.apply(Math, safeValues);
  }

  getStatPercent(value: number, max: number): string {
    if (!max || max <= 0) {
      return '0%';
    }
    return `${Math.round((value / max) * 100)}%`;
  }

  get filteredBundles(): BundleDto[] {
    const term = this.bundleQuery.trim().toLowerCase();
    if (!term) {
      return this.bundles;
    }
    return this.bundles.filter((bundle) => {
      const name = (bundle.name || '').toLowerCase();
      const description = (bundle.description || '').toLowerCase();
      return name.includes(term) || description.includes(term);
    });
  }

  get pagedBundles(): BundleDto[] {
    const start = (this.bundlePage - 1) * this.bundlePageSize;
    return this.filteredBundles.slice(start, start + this.bundlePageSize);
  }

  get bundleTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredBundles.length / this.bundlePageSize));
  }

  goToBundlePage(page: number): void {
    const safePage = Math.max(1, Math.min(page, this.bundleTotalPages));
    this.bundlePage = safePage;
  }

  resetBundlePagination(): void {
    this.bundlePage = 1;
  }

  get filteredCoupons(): CouponDto[] {
    const term = this.couponQuery.trim().toLowerCase();
    if (!term) {
      return this.coupons;
    }
    return this.coupons.filter((coupon) =>
      (coupon.code || '').toLowerCase().includes(term)
    );
  }

  get pagedCoupons(): CouponDto[] {
    const start = (this.couponPage - 1) * this.couponPageSize;
    return this.filteredCoupons.slice(start, start + this.couponPageSize);
  }

  get couponTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredCoupons.length / this.couponPageSize));
  }

  goToCouponPage(page: number): void {
    const safePage = Math.max(1, Math.min(page, this.couponTotalPages));
    this.couponPage = safePage;
  }

  resetCouponPagination(): void {
    this.couponPage = 1;
  }

  get filteredOrders(): OrderDto[] {
    const term = this.orderQuery.trim().toLowerCase();
    return this.orders.filter((order) => {
      const matchesText = !term
        || (order.status || '').toLowerCase().includes(term)
        || String(order.totalPrice || '').includes(term)
        || String(order.orderItems?.length || '').includes(term);
      const matchesDate = this.isOrderWithinDateRange(order);
      return matchesText && matchesDate;
    });
  }

  private isOrderWithinDateRange(order: OrderDto): boolean {
    const hasFilter = !!this.orderDateFrom || !!this.orderDateTo;
    if (!hasFilter) {
      return true;
    }
    if (!order.createdAt) {
      return false;
    }
    const created = new Date(order.createdAt);
    if (Number.isNaN(created.getTime())) {
      return false;
    }
    if (this.orderDateFrom) {
      const from = new Date(`${this.orderDateFrom}T00:00:00`);
      if (created < from) {
        return false;
      }
    }
    if (this.orderDateTo) {
      const to = new Date(`${this.orderDateTo}T23:59:59`);
      if (created > to) {
        return false;
      }
    }
    return true;
  }

  get pagedOrders(): OrderDto[] {
    const start = (this.orderPage - 1) * this.orderPageSize;
    return this.filteredOrders.slice(start, start + this.orderPageSize);
  }

  get orderTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredOrders.length / this.orderPageSize));
  }

  goToOrderPage(page: number): void {
    const safePage = Math.max(1, Math.min(page, this.orderTotalPages));
    this.orderPage = safePage;
  }

  resetOrderPagination(): void {
    this.orderPage = 1;
  }

  get filteredAuditLogs(): AuditLogDto[] {
    const term = this.auditQuery.trim().toLowerCase();
    if (!term) {
      return this.auditLogs;
    }
    return this.auditLogs.filter((log) => {
      const action = (log.action || '').toLowerCase();
      const entity = (log.entityType || '').toLowerCase();
      const actor = (log.actorId || '').toLowerCase();
      return action.includes(term) || entity.includes(term) || actor.includes(term);
    });
  }

  get pagedAuditLogs(): AuditLogDto[] {
    const start = (this.auditPage - 1) * this.auditPageSize;
    return this.filteredAuditLogs.slice(start, start + this.auditPageSize);
  }

  get auditTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredAuditLogs.length / this.auditPageSize));
  }

  goToAuditPage(page: number): void {
    const safePage = Math.max(1, Math.min(page, this.auditTotalPages));
    this.auditPage = safePage;
  }

  resetAuditPagination(): void {
    this.auditPage = 1;
  }

  createProduct(): void {
    const request = this.buildProductRequest();
    if (!request) {
      return;
    }
    this.loading = true;
    this.api.createProduct(request).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  updateProduct(): void {
    const selected = this.ensureSelectedProduct('update');
    if (!selected) {
      return;
    }
    const request = this.buildProductRequest();
    if (!request) {
      return;
    }
    this.loading = true;
    this.api.updateProduct(selected.id, request).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  deleteProduct(): void {
    const selected = this.ensureSelectedProduct('remove');
    if (!selected) {
      return;
    }
    this.loading = true;
    this.api.deleteProduct(selected.id).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.resetProductForm();
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  selectProduct(product: ProductDto): void {
    this.productForm = {
      id: product.id,
      name: product.name ?? '',
      isbn: product.isbn ?? '',
      description: product.description ?? '',
      imageUrl: product.imageUrl ?? '',
      imagePreviewUrl: product.imageUrl ?? '',
      price: product.price ?? null,
      originalPrice: product.originalPrice ?? null,
      discountPercent: product.discountPercent ?? null,
      discountEndsAt: product.discountEndsAt ? product.discountEndsAt.slice(0, 16) : '',
      quantityAvailable: product.quantityAvailable ?? null,
      categoryId: product.categoryId ?? null
    };
  }

  createCategory(): void {
    const request = this.buildCategoryRequest();
    if (!request) {
      return;
    }
    this.loading = true;
    this.api.createCategory(request).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  updateCategory(): void {
    const selected = this.ensureSelectedCategory('update');
    if (!selected) {
      return;
    }
    const request = this.buildCategoryRequest();
    if (!request) {
      return;
    }
    this.loading = true;
    this.api.updateCategory(selected.id, request).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  deleteCategory(): void {
    const selected = this.ensureSelectedCategory('remove');
    if (!selected) {
      return;
    }
    this.loading = true;
    this.api.deleteCategory(selected.id).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.resetCategoryForm();
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  selectCategory(category: CategoryDto): void {
    this.categoryForm = {
      id: category.id,
      name: category.name ?? '',
      description: category.description ?? ''
    };
  }

  createBundle(): void {
    const request = this.buildBundleRequest();
    if (!request) {
      return;
    }
    this.loading = true;
    this.api.createBundle(request).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.resetBundleForm();
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  updateBundle(): void {
    const selected = this.ensureSelectedBundle('update');
    if (!selected) {
      return;
    }
    const request = this.buildBundleRequest();
    if (!request) {
      return;
    }
    this.loading = true;
    this.api.updateBundle(selected.id, request).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  deleteBundle(): void {
    const selected = this.ensureSelectedBundle('remove');
    if (!selected) {
      return;
    }
    this.loading = true;
    this.api.deleteBundle(selected.id).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.resetBundleForm();
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  selectBundle(bundle: BundleDto): void {
    this.bundleForm = {
      id: bundle.id,
      name: bundle.name ?? '',
      description: bundle.description ?? '',
      imageUrl: bundle.imageUrl ?? '',
      imagePreviewUrl: bundle.imageUrl ?? '',
      price: bundle.price ?? null,
      status: bundle.status ?? 'DRAFT',
      selectedItems: (bundle.items ?? []).map((item) => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    };
  }

  openBundlePicker(): void {
    this.isBundlePickerOpen = true;
  }

  closeBundlePicker(): void {
    this.isBundlePickerOpen = false;
  }

  addBundleItem(product: ProductDto): void {
    const existing = this.bundleForm.selectedItems.find((item) => item.productId === product.id);
    if (existing) {
      existing.quantity += 1;
      return;
    }
    this.bundleForm.selectedItems = [
      ...this.bundleForm.selectedItems,
      { productId: product.id, quantity: 1 }
    ];
  }

  updateBundleItemQuantity(productId: number, quantity: number): void {
    const nextQuantity = Number(quantity);
    if (!Number.isFinite(nextQuantity) || nextQuantity <= 0) {
      this.bundleForm.selectedItems = this.bundleForm.selectedItems.filter((item) => item.productId !== productId);
      return;
    }
    this.bundleForm.selectedItems = this.bundleForm.selectedItems.map((item) =>
      item.productId === productId ? { ...item, quantity: nextQuantity } : item
    );
  }

  removeBundleItem(productId: number): void {
    this.bundleForm.selectedItems = this.bundleForm.selectedItems.filter((item) => item.productId !== productId);
  }

  getBundleItemProduct(productId: number): ProductDto | undefined {
    return this.products.find((product) => product.id === productId);
  }

  createOrder(): void {
    const request = this.buildOrderRequest();
    if (!request) {
      return;
    }
    this.loading = true;
    this.api.createOrder(request).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.resetOrderForm();
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  updateOrderStatus(): void {
    if (!this.orderStatusForm.id) {
      this.setErrorMessage('Select an order to update.');
      return;
    }
    this.loading = true;
    this.api.updateOrderStatus(this.orderStatusForm.id, this.orderStatusForm.status).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  cancelOrder(): void {
    if (!this.orderStatusForm.id) {
      this.setErrorMessage('Select an order to cancel.');
      return;
    }
    this.loading = true;
    this.api.cancelOrder(this.orderStatusForm.id).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  assignCourier(): void {
    const orderId = this.resolveCourierOrderId();
    const courierId = Number(this.courierAssignmentForm.courierId);
    if (!orderId) {
      this.setErrorMessage('Select an order to assign a delivery agent.');
      return;
    }
    if (!Number.isFinite(courierId) || courierId <= 0) {
      this.setErrorMessage('Select a delivery agent.');
      return;
    }
    const courierName = this.courierCandidates.find((item) => Number(item.userId) === courierId);
    const displayName = courierName ? this.getCourierDisplayName(courierName) : undefined;
    this.loading = true;
    this.api.assignCourier(orderId, courierId, displayName).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message || 'Courier assigned.');
        this.courierAssignmentForm.orderId = orderId;
        this.courierAssignmentForm.courierId = courierId;
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  unassignCourier(): void {
    const orderId = this.resolveCourierOrderId();
    if (!orderId) {
      this.setErrorMessage('Select an order to unassign the delivery agent.');
      return;
    }
    this.loading = true;
    this.api.unassignCourier(orderId).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message || 'Courier unassigned.');
        this.courierAssignmentForm.orderId = orderId;
        this.courierAssignmentForm.courierId = null;
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  selectOrder(order: OrderDto): void {
    this.courierAssignmentForm.orderId = order.id;
    this.selectedOrderId = order.id;
    this.selectedOrderSummary = {
      items: order.orderItems?.length || 0,
      total: order.totalPrice || 0,
      status: order.status || 'PENDING'
    };
  }

  get selectedCourierLabel(): string {
    if (!this.courierAssignmentForm.courierId) {
      return '';
    }
    const id = Number(this.courierAssignmentForm.courierId);
    const match = this.courierCandidates.find((item) => Number(item.userId) === id);
    return match ? this.getCourierDisplayName(match) : '';
  }

  selectCourierCandidate(profile: UserInfoDto | null): void {
    if (!profile) {
      this.courierAssignmentForm.courierId = null;
      this.courierDropdownOpen = false;
      return;
    }
    const parsedId = Number(profile.userId);
    if (!Number.isFinite(parsedId) || parsedId <= 0) {
      this.setErrorMessage('User ID not found.');
      return;
    }
    this.courierAssignmentForm.courierId = parsedId;
    this.courierNameById[parsedId] = this.getCourierDisplayName(profile);
    this.courierDropdownOpen = false;
  }

  getOrderStatusDraft(order: OrderDto): string {
    return this.orderStatusDrafts[order.id] || order.status || 'PENDING';
  }

  setOrderStatusDraft(orderId: number, status: string): void {
    this.orderStatusDrafts[orderId] = status;
  }

  saveOrderStatus(order: OrderDto): void {
    const nextStatus = this.getOrderStatusDraft(order);
    this.loading = true;
    this.api.updateOrderStatus(order.id, nextStatus).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message || 'Order status updated.');
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  saveSelectedOrderStatus(): void {
    if (!this.selectedOrderId) {
      this.setErrorMessage('Select an order to update.');
      return;
    }
    this.loading = true;
    this.api.updateOrderStatus(this.selectedOrderId, this.orderStatusForm.status).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message || 'Order status updated.');
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  cancelOrderById(order: OrderDto): void {
    this.loading = true;
    this.api.cancelOrder(order.id).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message || 'Order cancelled.');
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  cancelSelectedOrder(): void {
    if (!this.selectedOrderId) {
      this.setErrorMessage('Select an order to cancel.');
      return;
    }
    this.loading = true;
    this.api.cancelOrder(this.selectedOrderId).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message || 'Order cancelled.');
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  private syncOrderStatusDrafts(): void {
    this.orderStatusDrafts = this.orders.reduce<Record<number, string>>((acc, order) => {
      acc[order.id] = this.orderStatusDrafts[order.id] || order.status || 'PENDING';
      return acc;
    }, {});
  }

  toggleOrderItems(orderId: number): void {
    if (this.expandedOrders.has(orderId)) {
      this.expandedOrders.delete(orderId);
      return;
    }
    this.expandedOrders.add(orderId);
  }

  isOrderExpanded(orderId: number): boolean {
    return this.expandedOrders.has(orderId);
  }

  openOrderPicker(): void {
    this.isOrderPickerOpen = true;
  }

  closeOrderPicker(): void {
    this.isOrderPickerOpen = false;
  }

  addOrderItem(product: ProductDto): void {
    const existing = this.orderForm.selectedItems.find((item) => item.productId === product.id);
    if (existing) {
      existing.quantity += 1;
      return;
    }
    this.orderForm.selectedItems = [
      ...this.orderForm.selectedItems,
      { productId: product.id, quantity: 1 }
    ];
  }

  updateOrderItemQuantity(productId: number, quantity: number): void {
    const nextQuantity = Number(quantity);
    if (!Number.isFinite(nextQuantity) || nextQuantity <= 0) {
      this.orderForm.selectedItems = this.orderForm.selectedItems.filter((item) => item.productId !== productId);
      return;
    }
    this.orderForm.selectedItems = this.orderForm.selectedItems.map((item) =>
      item.productId === productId ? { ...item, quantity: nextQuantity } : item
    );
  }

  removeOrderItem(productId: number): void {
    this.orderForm.selectedItems = this.orderForm.selectedItems.filter((item) => item.productId !== productId);
  }

  getOrderProduct(productId: number): ProductDto | undefined {
    return this.products.find((product) => product.id === productId);
  }

  getCourierAssignmentLabel(orderId: number): string {
    const assignment = this.courierAssignments.find((item) => item.orderId === orderId);
    if (!assignment || !assignment.courierId) {
      return 'Unassigned';
    }
    const name = assignment.courierName || this.courierNameById[assignment.courierId];
    if (name) {
      return `Delivery agent: ${name}`;
    }
    return 'Delivery agent assigned';
  }

  lookupCourier(): void {
    const username = this.courierUsername.trim();
    if (!username) {
      this.setErrorMessage('Enter a username to search.');
      return;
    }
    this.courierLookupLoading = true;
    this.lookupCourierByUsername(username, true);
  }

  private lookupCourierByUsername(username: string, allowEmailFallback: boolean): void {
    this.userLookup.getUserInfo(username).subscribe({
      next: (user) => {
        const parsedId = Number(user.userId);
        if (!Number.isFinite(parsedId) || parsedId <= 0) {
          this.setErrorMessage('User ID not found.');
          this.courierLookupLoading = false;
          return;
        }
        const exists = this.courierCandidates.some((item) => item.userId === user.userId);
        if (!exists) {
          this.courierCandidates = [...this.courierCandidates, user];
        }
        this.courierNameById[parsedId] = this.getCourierDisplayName(user);
        this.courierAssignmentForm.courierId = parsedId;
        this.courierLookupLoading = false;
      },
      error: () => {
        if (allowEmailFallback && username.includes('@')) {
          const localPart = username.split('@')[0].trim();
          if (localPart && localPart !== username) {
            this.lookupCourierByUsername(localPart, false);
            return;
          }
        }
        this.setErrorMessage('User not found.');
        this.courierLookupLoading = false;
      }
    });
  }

  getCourierDisplayName(profile: UserInfoDto): string {
    const first = profile.firstName || '';
    const last = profile.lastName || '';
    const name = `${first} ${last}`.trim();
    return name || profile.username || profile.email || 'User';
  }

  selectCoupon(coupon: CouponDto): void {
    this.couponForm = {
      id: coupon.id,
      code: coupon.code ?? '',
      description: coupon.description ?? '',
      type: coupon.type ?? 'PERCENT',
      value: coupon.value ?? null,
      minOrderTotal: coupon.minOrderTotal ?? null,
      maxDiscount: coupon.maxDiscount ?? null,
      active: coupon.active ?? true,
      usageLimit: coupon.usageLimit ?? null,
      startsAt: coupon.startsAt ? coupon.startsAt.slice(0, 16) : '',
      endsAt: coupon.endsAt ? coupon.endsAt.slice(0, 16) : ''
    };
  }

  createCoupon(): void {
    const request = this.buildCouponRequest();
    if (!request) {
      return;
    }
    this.loading = true;
    this.api.createCoupon(request).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.resetCouponForm();
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  updateCoupon(): void {
    if (!this.couponForm.id) {
      this.setErrorMessage('Select a coupon to update.');
      return;
    }
    const request = this.buildCouponRequest();
    if (!request) {
      return;
    }
    this.loading = true;
    this.api.updateCoupon(this.couponForm.id, request).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  deleteCoupon(): void {
    if (!this.couponForm.id) {
      this.setErrorMessage('Select a coupon to remove.');
      return;
    }
    this.loading = true;
    this.api.deleteCoupon(this.couponForm.id).subscribe({
      next: (response) => {
        this.setSuccessMessage(response.message);
        this.resetCouponForm();
        this.refreshData();
      },
      error: (error) => this.handleError(error)
    });
  }

  private buildCouponRequest(): CreateCouponRequest | null {
    const code = this.couponForm.code.trim().toUpperCase();
    if (!code) {
      this.setErrorMessage('Coupon code is required.');
      return null;
    }
    if (!this.couponForm.value || Number(this.couponForm.value) <= 0) {
      this.setErrorMessage('Coupon value must be greater than 0.');
      return null;
    }
    const startsAt = this.couponForm.startsAt?.trim() || null;
    const endsAt = this.couponForm.endsAt?.trim() || null;
    return {
      code,
      description: this.couponForm.description || null,
      type: this.couponForm.type,
      value: Number(this.couponForm.value),
      minOrderTotal: this.couponForm.minOrderTotal === null || this.couponForm.minOrderTotal === undefined
        ? null
        : Number(this.couponForm.minOrderTotal),
      maxDiscount: this.couponForm.maxDiscount === null || this.couponForm.maxDiscount === undefined
        ? null
        : Number(this.couponForm.maxDiscount),
      active: this.couponForm.active,
      usageLimit: this.couponForm.usageLimit === null || this.couponForm.usageLimit === undefined
        ? null
        : Number(this.couponForm.usageLimit),
      startsAt,
      endsAt
    };
  }

  private resetCouponForm(): void {
    this.couponForm = {
      id: null,
      code: '',
      description: '',
      type: 'PERCENT',
      value: null,
      minOrderTotal: null,
      maxDiscount: null,
      active: true,
      usageLimit: null,
      startsAt: '',
      endsAt: ''
    };
  }

  private buildProductRequest(): CreateProductRequest | null {
    const name = this.productForm.name.trim();
    if (!name || this.productForm.price === null || this.productForm.quantityAvailable === null || this.productForm.categoryId === null) {
      this.setErrorMessage('Fill name, price, quantity, and category.');
      return null;
    }
    const rawIsbn = this.productForm.isbn?.trim() || '';
    const normalizedIsbn = rawIsbn ? rawIsbn.replace(/[\s-]/g, '').toUpperCase() : '';
    if (normalizedIsbn && normalizedIsbn.length > 32) {
      this.setErrorMessage('ISBN must be 32 characters or less.');
      return null;
    }
    if (normalizedIsbn && !this.isValidIsbn(normalizedIsbn)) {
      this.setErrorMessage('ISBN must be a valid ISBN-10 or ISBN-13.');
      return null;
    }
    const category = this.categories.find((item) => item.id === this.productForm.categoryId) ?? null;
    if (!category) {
      this.setErrorMessage('Select a valid category.');
      return null;
    }
    const nameExists = this.products.some((product) => product.name?.trim().toLowerCase() === name.toLowerCase() && product.id !== this.productForm.id);
    if (nameExists) {
      this.setErrorMessage('Product already exists.');
      return null;
    }
    const price = Number(this.productForm.price);
    const originalPriceValue = this.productForm.originalPrice === null || this.productForm.originalPrice === undefined
      ? null
      : Number(this.productForm.originalPrice);
    if (originalPriceValue !== null && (!Number.isFinite(originalPriceValue) || originalPriceValue <= 0)) {
      this.setErrorMessage('Original price must be greater than 0.');
      return null;
    }
    if (originalPriceValue !== null && originalPriceValue < price) {
      this.setErrorMessage('Original price must be greater than or equal to price.');
      return null;
    }
    const discountPercentValue = this.productForm.discountPercent === null || this.productForm.discountPercent === undefined
      ? null
      : Number(this.productForm.discountPercent);
    if (discountPercentValue !== null && (!Number.isFinite(discountPercentValue) || discountPercentValue <= 0 || discountPercentValue > 90)) {
      this.setErrorMessage('Discount percent must be between 1 and 90.');
      return null;
    }
    const discountEndsAtValue = this.productForm.discountEndsAt?.trim() || null;
    const imageUrlValue = this.productForm.imageUrl?.trim() || '';
    if (imageUrlValue && imageUrlValue.startsWith('data:')) {
      this.setErrorMessage('Use a public image URL, not a local file.');
      return null;
    }
    if (imageUrlValue && imageUrlValue.length > 60000) {
      this.setErrorMessage('Image URL is too long.');
      return null;
    }
    return {
      name,
      isbn: normalizedIsbn ? normalizedIsbn : null,
      description: this.productForm.description || null,
      imageUrl: imageUrlValue ? imageUrlValue : null,
      price,
      originalPrice: originalPriceValue,
      discountPercent: discountPercentValue,
      discountEndsAt: discountEndsAtValue,
      quantityAvailable: Number(this.productForm.quantityAvailable),
      categoryId: Number(this.productForm.categoryId)
    };
  }

  private buildCategoryRequest(): CreateCategoryRequest | null {
    const name = this.categoryForm.name.trim();
    if (!name) {
      this.setErrorMessage('Fill the category name.');
      return null;
    }
    const nameExists = this.categories.some((category) => category.name?.trim().toLowerCase() === name.toLowerCase() && category.id !== this.categoryForm.id);
    if (nameExists) {
      this.setErrorMessage('Category already exists.');
      return null;
    }
    return {
      name,
      description: this.categoryForm.description || null
    };
  }

  private buildBundleRequest(): CreateBundleRequest | null {
    const name = this.bundleForm.name.trim();
    if (!name || this.bundleForm.price === null) {
      this.setErrorMessage('Fill bundle name and price.');
      return null;
    }
    if (name.length < 2) {
      this.setErrorMessage('Bundle name must be at least 2 characters.');
      return null;
    }
    if (Number(this.bundleForm.price) <= 0) {
      this.setErrorMessage('Bundle price must be greater than 0.');
      return null;
    }
    if (this.bundleForm.selectedItems.length === 0) {
      this.setErrorMessage('Add at least one product to the bundle.');
      return null;
    }
    const nameExists = this.bundles.some((bundle) => bundle.name?.trim().toLowerCase() === name.toLowerCase() && bundle.id !== this.bundleForm.id);
    if (nameExists) {
      this.setErrorMessage('Bundle already exists.');
      return null;
    }
    const invalidItems = this.bundleForm.selectedItems.filter((item) => !this.products.some((product) => product.id === item.productId));
    if (invalidItems.length > 0) {
      this.setErrorMessage('Some selected products do not exist.');
      return null;
    }
    const cleanedItems = this.bundleForm.selectedItems
      .map((item) => ({ productId: item.productId, quantity: Number(item.quantity) }))
      .filter((item) => Number.isFinite(item.quantity) && item.quantity > 0);
    if (cleanedItems.length === 0) {
      this.setErrorMessage('Bundle items must have quantity.');
      return null;
    }
    const imageUrl = this.bundleForm.imageUrl?.trim() || '';
    if (imageUrl && imageUrl.startsWith('data:')) {
      this.setErrorMessage('Use a public image URL, not a data image.');
      return null;
    }
    if (imageUrl && imageUrl.length > 60000) {
      this.setErrorMessage('Image URL is too long.');
      return null;
    }
    return {
      name,
      description: this.bundleForm.description || null,
      imageUrl: imageUrl || null,
      price: Number(this.bundleForm.price),
      status: this.bundleForm.status,
      items: cleanedItems
    };
  }

  private buildOrderRequest(): CreateOrderRequest | null {
    const customerId = Number(this.orderForm.customerId);
    if (!Number.isFinite(customerId) || customerId <= 0) {
      this.setErrorMessage('Add a valid customer id.');
      return null;
    }
    if (this.orderForm.selectedItems.length === 0) {
      this.setErrorMessage('Add at least one product to the order.');
      return null;
    }
    const missingProducts = this.orderForm.selectedItems.filter((item) => !this.products.some((product) => product.id === item.productId));
    if (missingProducts.length > 0) {
      this.setErrorMessage('Some selected products do not exist.');
      return null;
    }
    const cleanedItems = this.orderForm.selectedItems
      .map((item) => ({ productId: item.productId, quantity: Number(item.quantity) }))
      .filter((item) => Number.isFinite(item.quantity) && item.quantity > 0);
    if (cleanedItems.length === 0) {
      this.setErrorMessage('Order items must have quantity.');
      return null;
    }
    return {
      customerId,
      items: cleanedItems
    };
  }

  private isValidIsbn(isbn: string): boolean {
    if (!isbn) {
      return false;
    }
    if (isbn.length === 10) {
      return this.isValidIsbn10(isbn);
    }
    if (isbn.length === 13) {
      return this.isValidIsbn13(isbn);
    }
    return false;
  }

  private isValidIsbn10(isbn: string): boolean {
    let sum = 0;
    for (let i = 0; i < 9; i += 1) {
      const ch = isbn.charAt(i);
      if (ch < '0' || ch > '9') {
        return false;
      }
      sum += (i + 1) * (ch.charCodeAt(0) - 48);
    }
    const last = isbn.charAt(9);
    let checkValue = 0;
    if (last === 'X') {
      checkValue = 10;
    } else if (last >= '0' && last <= '9') {
      checkValue = last.charCodeAt(0) - 48;
    } else {
      return false;
    }
    sum += 10 * checkValue;
    return sum % 11 === 0;
  }

  private isValidIsbn13(isbn: string): boolean {
    let sum = 0;
    for (let i = 0; i < 12; i += 1) {
      const ch = isbn.charAt(i);
      if (ch < '0' || ch > '9') {
        return false;
      }
      const digit = ch.charCodeAt(0) - 48;
      sum += i % 2 === 0 ? digit : digit * 3;
    }
    const last = isbn.charAt(12);
    if (last < '0' || last > '9') {
      return false;
    }
    const checkDigit = (10 - (sum % 10)) % 10;
    return checkDigit === (last.charCodeAt(0) - 48);
  }

  private updateStats(): void {
    this.stats = [
      { label: 'Products', value: this.products.length, note: 'Ready' },
      { label: 'Categories', value: this.categories.length, note: 'Ready' },
      { label: 'Bundles', value: this.bundles.length, note: 'Ready' },
      { label: 'Orders', value: this.orders.length, note: 'Ready' },
      { label: 'Coupons', value: this.coupons.length, note: 'Ready' }
    ];
  }

  private handleError(error: any): void {
    this.loading = false;
    const fieldErrors = error?.error?.fieldErrors;
    const message = fieldErrors && typeof fieldErrors === 'object'
      ? Object.values(fieldErrors).join(' ')
      : error?.error?.message || error?.error?.error || 'Request failed.';
    this.successMessage = '';
    this.setErrorMessage(message);
  }

  private resetProductForm(): void {
    this.productForm = {
      id: null,
      name: '',
      isbn: '',
      description: '',
      imageUrl: '',
      imagePreviewUrl: '',
      price: null,
      originalPrice: null,
      discountPercent: null,
      discountEndsAt: '',
      quantityAvailable: null,
      categoryId: null
    };
  }

  private resetBundleForm(): void {
    this.bundleForm = {
      id: null,
      name: '',
      description: '',
      imageUrl: '',
      imagePreviewUrl: '',
      price: null,
      status: 'DRAFT',
      selectedItems: []
    };
  }

  onProductImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.uploadImage(file, 'product');
  }

  clearProductImage(): void {
    this.productForm.imageUrl = '';
    this.productForm.imagePreviewUrl = '';
  }

  onBundleImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.uploadImage(file, 'bundle');
  }

  private uploadImage(file: File, target: 'product' | 'bundle'): void {
    if (!this.cloudinary.isConfigured()) {
      this.setErrorMessage('Cloudinary is not configured. Add cloud name and upload preset.');
      return;
    }
    this.isImageUploading = true;
    this.cloudinary.uploadImage(file, 'ecommerce')
      .pipe(finalize(() => {
        this.isImageUploading = false;
      }))
      .subscribe({
        next: (response) => {
          const url = response.secure_url || response.url || '';
          if (!url) {
            this.setErrorMessage('Image upload failed. No URL returned.');
            return;
          }
          if (target === 'product') {
            this.productForm.imageUrl = url;
            this.productForm.imagePreviewUrl = url;
          } else {
            this.bundleForm.imageUrl = url;
            this.bundleForm.imagePreviewUrl = url;
          }
          this.setSuccessMessage('Image uploaded successfully.');
        },
        error: (error) => {
          const message = error?.error?.message || error?.message || 'Image upload failed.';
          this.setErrorMessage(message);
        }
      });
  }

  clearBundleImage(): void {
    this.bundleForm.imageUrl = '';
    this.bundleForm.imagePreviewUrl = '';
  }

  getSelectedCategory(): CategoryDto | null {
    if (!this.productForm.categoryId) {
      return null;
    }
    return this.categories.find((category) => category.id === this.productForm.categoryId) ?? null;
  }

  private resetCategoryForm(): void {
    this.categoryForm = {
      id: null,
      name: '',
      description: ''
    };
  }

  private resetOrderForm(): void {
    this.orderForm = {
      customerId: null,
      selectedItems: []
    };
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  private setSuccessMessage(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    this.pushToast('success', message);
    setTimeout(() => {
      if (this.successMessage === message) {
        this.successMessage = '';
      }
    }, 3500);
  }

  private setErrorMessage(message: string): void {
    this.errorMessage = message;
    this.successMessage = '';
    this.pushToast('error', message);
    setTimeout(() => {
      if (this.errorMessage === message) {
        this.errorMessage = '';
      }
    }, 3500);
  }

  private pushToast(type: 'success' | 'error' | 'info', message: string): void {
    const id = this.toastId + 1;
    this.toastId = id;
    this.toasts = [...this.toasts, { id, type, message }];
    setTimeout(() => this.dismissToast(id), 3500);
  }

  dismissToast(id: number): void {
    this.toasts = this.toasts.filter((toast) => toast.id !== id);
  }

  private ensureSelectedProduct(action: string): ProductDto | null {
    if (!this.productForm.id) {
      this.setErrorMessage(`Select a product to ${action}.`);
      return null;
    }
    const product = this.products.find((item) => item.id === this.productForm.id) ?? null;
    if (!product) {
      this.setErrorMessage('Selected product does not exist.');
      return null;
    }
    return product;
  }

  private ensureSelectedCategory(action: string): CategoryDto | null {
    if (!this.categoryForm.id) {
      this.setErrorMessage(`Select a category to ${action}.`);
      return null;
    }
    const category = this.categories.find((item) => item.id === this.categoryForm.id) ?? null;
    if (!category) {
      this.setErrorMessage('Selected category does not exist.');
      return null;
    }
    return category;
  }

  private ensureSelectedBundle(action: string): BundleDto | null {
    if (!this.bundleForm.id) {
      this.setErrorMessage(`Select a bundle to ${action}.`);
      return null;
    }
    const bundle = this.bundles.find((item) => item.id === this.bundleForm.id) ?? null;
    if (!bundle) {
      this.setErrorMessage('Selected bundle does not exist.');
      return null;
    }
    return bundle;
  }

  private resolveCourierOrderId(): number | null {
    if (this.courierAssignmentForm.orderId) {
      return this.courierAssignmentForm.orderId;
    }
    if (this.orderStatusForm.id) {
      return this.orderStatusForm.id;
    }
    return null;
  }
}
