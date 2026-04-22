import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EcommerceAdminApiService, ProductDto, CategoryDto } from '../../../services/ecommerce-admin-api.service';
import { AuthService } from '../../../services/auth.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-shop-catalog',
  templateUrl: './shop-catalog.component.html',
  styleUrls: ['./shop-catalog.component.css']
})
export class ShopCatalogComponent implements OnInit, OnDestroy {
  searchTerm = '';
  activeCategory = 'All';
  priceRange = 0;
  sortBy = 'popular';

  // Pagination
  currentPage = 0;
  pageSize = 12;

  // Comparison feature
  comparisonProducts: any[] = [];
  showComparisonBar = false;

  categories: any[] = [];
  allProducts: any[] = [];
  isLoading = true;
  error: string | null = null;
  cartError: string | null = null;
  cartSuccess = '';
  now = Date.now();
  private timerId: any;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ecommerceService: EcommerceAdminApiService,
    private auth: AuthService
  ) {
    this.loadComparisonFromStorage();
  }

  ngOnInit(): void {
    this.loadData();
    this.timerId = setInterval(() => {
      const prevNow = this.now;
      this.now = Date.now();
      // If any product had a discount that just expired since last tick, reload
      const anyJustExpired = this.allProducts.some(p => {
        if (!p.discountEndsAt) return false;
        const t = new Date(p.discountEndsAt).getTime();
        return t > prevNow && t <= this.now;
      });
      if (anyJustExpired) {
        this.loadData();
      }
    }, 5000);
    this.route.queryParamMap.subscribe((params) => {
      const category = params.get('category');
      if (category && this.categoryFilters.includes(category)) {
        this.activeCategory = category;
      } else {
        this.activeCategory = 'All';
      }
    });
  }

  private loadData(): void {
    this.isLoading = true;
    this.error = null;
    this.cartError = null;
    this.cartSuccess = '';

    // Load categories
    this.ecommerceService.listCategories()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          const data = Array.isArray(response.data) ? response.data : [];
          this.categories = data.map((cat: CategoryDto) => ({
            id: cat.id,
            name: cat.name,
            description: cat.description
          }));
        },
        error: (err) => {
          console.error('Failed to load categories:', err);
          this.error = 'Failed to load categories';
        }
      });

    // Load products
    this.ecommerceService.listProducts()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          const data = Array.isArray(response.data) ? response.data : [];
          this.allProducts = data.map((prod: ProductDto) => {
            const originalPrice = prod.originalPrice ?? null;
            const hasDiscount = originalPrice !== null && originalPrice > prod.price;
            return {
              id: prod.id,
              title: prod.name,
              isbn: prod.isbn || null,
              description: prod.description || 'No description',
              price: prod.price,
              oldPrice: hasDiscount ? originalPrice : null,
              tag: hasDiscount ? 'Sale' : '',
              discountEndsAt: prod.discountEndsAt ?? null,
              category: prod.categoryName || 'Uncategorized',
              image: prod.imageUrl || 'https://picsum.photos/seed/default/800/600',
              rating: prod.averageRating ?? 0,
              reviews: prod.reviewCount ?? 0
            };
          });
          this.priceRange = this.maxPrice;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Failed to load products:', err);
          this.error = 'Failed to load products';
          this.isLoading = false;
        }
      });
  }

  get maxPrice(): number {
    const products = Array.isArray(this.allProducts) ? this.allProducts : [];
    if (products.length === 0) return 100;
    const prices = products.map((product) => product.price);
    return Math.max.apply(Math, prices);
  }

  get categoryFilters(): string[] {
    const categories = Array.isArray(this.categories) ? this.categories : [];
    const names = categories.map((category) => category.name);
    return (['All'] as string[]).concat(names);
  }

  get filteredProducts() {
    const term = this.searchTerm.trim().toLowerCase();
    let result = this.allProducts.filter((product) => {
      const matchesCategory = this.activeCategory === 'All' || product.category === this.activeCategory;
      const matchesPrice = product.price <= this.priceRange;
      const matchesTerm = !term
        || product.title.toLowerCase().includes(term)
        || product.description.toLowerCase().includes(term)
        || (product.isbn || '').toLowerCase().includes(term);
      return matchesCategory && matchesPrice && matchesTerm;
    });

    if (this.sortBy === 'popular') {
      result = result.slice().sort((a, b) => (b.rating ?? 0) - (a.rating ?? 0));
    }
    if (this.sortBy === 'price-low') {
      result = result.slice().sort((a, b) => a.price - b.price);
    }
    if (this.sortBy === 'price-high') {
      result = result.slice().sort((a, b) => b.price - a.price);
    }
    if (this.sortBy === 'rating') {
      result = result.slice().sort((a, b) => (b.rating ?? 0) - (a.rating ?? 0));
    }

    return result;
  }

  updateSearch(value: string) {
    this.searchTerm = value;
    this.currentPage = 0;
  }

  updatePrice(value: string) {
    this.priceRange = Number(value);
    this.currentPage = 0;
  }

  updateSort(value: string) {
    this.sortBy = value;
    this.currentPage = 0;
  }

  setCategory(category: string) {
    this.activeCategory = category;
    this.currentPage = 0;
  }

  addToCart(product: any): void {
    const customerId = this.resolveCustomerId();
    if (customerId === null) {
      this.cartError = 'Login required to add items to cart.';
      return;
    }
    this.cartError = null;
    this.cartSuccess = '';
    this.ecommerceService.addToCart(customerId, {
      productId: product.id,
      quantity: 1
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.cartSuccess = response.message || 'Added to cart.';
          this.router.navigate(['/shop/cart']);
        },
        error: (err) => {
          this.cartError = err?.error?.message || 'Failed to add to cart.';
        }
      });
  }

  private resolveCustomerId(): number | null {
    const payload = this.auth.getUserInfo();
    const raw = payload?.userId ?? payload?.id ?? payload?.sub ?? '';
    const parsed = Number(raw);
    if (!Number.isFinite(parsed) || parsed <= 0) {
      return null;
    }
    return parsed;
  }

  // Comparison Methods
  addToComparison(product: any) {
    if (!this.comparisonProducts.find(p => p.id === product.id)) {
      if (this.comparisonProducts.length < 4) {
        this.comparisonProducts.push(product);
        this.showComparisonBar = true;
        this.saveComparisonToStorage();
      } else {
        alert('You can compare up to 4 products');
      }
    }
  }

  removeFromComparison(productId: number) {
    this.comparisonProducts = this.comparisonProducts.filter(p => p.id !== productId);
    if (this.comparisonProducts.length === 0) {
      this.showComparisonBar = false;
    }
    this.saveComparisonToStorage();
  }

  clearComparison() {
    this.comparisonProducts = [];
    this.showComparisonBar = false;
    this.saveComparisonToStorage();
  }

  isInComparison(productId: number): boolean {
    return this.comparisonProducts.some(p => p.id === productId);
  }

  get filteredAndSortedProducts(): any[] {
    return this.filteredProducts;
  }

  goToComparison() {
    if (this.comparisonProducts.length > 0) {
      // Save to localStorage first
      this.saveComparisonToStorage();
      // Then navigate
      this.router.navigate(['/shop/comparison']);
    }
  }

  get paginatedProducts(): any[] {
    const start = this.currentPage * this.pageSize;
    return this.filteredAndSortedProducts.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredAndSortedProducts.length / this.pageSize);
  }

  goToPage(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  private saveComparisonToStorage() {
    localStorage.setItem('comparison_products', JSON.stringify(this.comparisonProducts));
  }

  private loadComparisonFromStorage() {
    const stored = localStorage.getItem('comparison_products');
    if (stored) {
      try {
        this.comparisonProducts = JSON.parse(stored);
        this.showComparisonBar = this.comparisonProducts.length > 0;
      } catch (e) {
        this.comparisonProducts = [];
      }
    }
  }

  ngOnDestroy(): void {
    if (this.timerId) {
      clearInterval(this.timerId);
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  getDiscountCountdown(endsAt: string | null): string {
    if (!endsAt) {
      return '';
    }
    const target = new Date(endsAt).getTime();
    if (Number.isNaN(target)) {
      return '';
    }
    const diff = target - this.now;
    if (diff <= 0) {
      return '';
    }
    const minutes = Math.floor(diff / 60000);
    const days = Math.floor(minutes / (24 * 60));
    const hours = Math.floor((minutes % (24 * 60)) / 60);
    const mins = minutes % 60;
    if (days > 0) {
      return `${days}d ${hours}h`;
    }
    if (hours > 0) {
      return `${hours}h ${mins}m`;
    }
    return `${mins}m`;
  }
}