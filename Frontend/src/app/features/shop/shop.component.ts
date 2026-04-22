import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { EcommerceAdminApiService, ProductDto, CategoryDto } from '../../services/ecommerce-admin-api.service';
import { AuthService } from '../../services/auth.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-shop',
  templateUrl: './shop.component.html',
  styleUrls: ['./shop.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule
  ]
})
export class ShopComponent implements OnInit, OnDestroy {
  private categorySource: CategoryDto[] = [];
  categories: any[] = [];
  products: any[] = [];
  isLoading = true;
  error: string | null = null;
  cartError: string | null = null;
  cartSuccess = '';
  now = Date.now();
  private timerId: any;
  private destroy$ = new Subject<void>();

  benefits = [
    { title: 'Fast delivery', description: 'Reliable shipping across Tunisia in 2-3 days.' },
    { title: 'Student-first pricing', description: 'Affordable essentials tailored for learners.' },
    { title: 'Quality guarantee', description: 'Carefully curated items you can trust.' }
  ];

  constructor(
    private ecommerceService: EcommerceAdminApiService,
    private router: Router,
    private auth: AuthService
  ) { }

  ngOnInit(): void {
    this.loadCategoriesAndProducts();
    this.timerId = setInterval(() => {
      this.now = Date.now();
    }, 60000);
  }

  private loadCategoriesAndProducts(): void {
    this.isLoading = true;
    this.error = null;
    this.cartError = null;
    this.cartSuccess = '';

    // Load categories
    this.ecommerceService.listCategories()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.categorySource = response.data ?? [];
          this.updateCategoryCards();
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
          const mapped = response.data.map((prod: ProductDto) => {
            const originalPrice = prod.originalPrice ?? null;
            const hasDiscount = originalPrice !== null && originalPrice > prod.price;
            return {
              id: prod.id,
              title: prod.name,
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
          // Sort by rating desc so the popular section shows best-rated first
          this.products = mapped.slice().sort((a: any, b: any) => (b.rating ?? 0) - (a.rating ?? 0));
          this.updateCategoryCards();
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Failed to load products:', err);
          this.error = 'Failed to load products';
          this.isLoading = false;
        }
      });
  }

  private updateCategoryCards(): void {
    if (!this.categorySource.length) {
      this.categories = [];
      return;
    }
    const countByName = new Map<string, number>();
    for (const product of this.products) {
      const name = product.category || 'Uncategorized';
      countByName.set(name, (countByName.get(name) || 0) + 1);
    }
    this.categories = this.categorySource.map((cat) => ({
      id: cat.id,
      name: cat.name,
      description: cat.description,
      icon: this.getCategoryIcon(cat.name),
      count: countByName.get(cat.name) || 0
    }));
  }

  private getCategoryIcon(name: string): string {
    const key = name.trim().toLowerCase();
    if (key.includes('stationery')) return '✍️';
    if (key.includes('study')) return '📚';
    if (key.includes('digital')) return '💾';
    if (key.includes('accessories')) return '🎒';
    if (key.includes('books')) return '📖';
    return '🧰';
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
