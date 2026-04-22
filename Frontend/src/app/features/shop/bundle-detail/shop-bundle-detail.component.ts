import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EcommerceAdminApiService, BundleDto, BundleItemDto } from '../../../services/ecommerce-admin-api.service';
import { AuthService } from '../../../services/auth.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-shop-bundle-detail',
  templateUrl: './shop-bundle-detail.component.html',
  styleUrls: ['./shop-bundle-detail.component.css']
})
export class ShopBundleDetailComponent implements OnInit, OnDestroy {
  bundle: BundleDto | null = null;
  isLoading = true;
  error: string | null = null;
  isAdding = false;
  cartMessage = '';
  cartError: string | null = null;
  customerId = 1;
  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private ecommerceService: EcommerceAdminApiService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    const resolved = this.resolveCustomerId();
    if (resolved === null) {
      this.error = 'Login required to add bundles to cart.';
      this.isLoading = false;
      return;
    }
    this.customerId = resolved;
    this.loadBundle();
  }

  get totalItems(): number {
    return (this.bundle?.items ?? []).reduce((sum, item) => sum + (item.quantity || 0), 0);
  }

  get bundleOriginalPrice(): number {
    return (this.bundle?.items ?? []).reduce((sum, item) => {
      const unit = Number(item.productPrice || 0);
      return sum + unit * item.quantity;
    }, 0);
  }

  get bundleSavings(): number {
    const original = this.bundleOriginalPrice;
    const current = Number(this.bundle?.price || 0);
    return original > current ? original - current : 0;
  }

  addBundleToCart(): void {
    if (this.isAdding || !this.bundle) {
      return;
    }
    if (!this.customerId) {
      this.cartError = 'Login required to add bundles to cart.';
      return;
    }
    this.isAdding = true;
    this.cartMessage = '';
    this.cartError = null;
    this.ecommerceService.addBundleToCart(this.customerId, this.bundle.id, 1)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.isAdding = false;
          this.cartMessage = 'Bundle added to cart.';
        },
        error: (err) => {
          this.isAdding = false;
          this.cartError = err?.error?.message || 'Failed to add bundle to cart.';
        }
      });
  }

  getBundleImageUrl(): string {
    const fallbackId = this.bundle?.id ?? 'default';
    return this.bundle?.imageUrl || `https://picsum.photos/seed/bundle-${fallbackId}/1200/800`;
  }

  getItemImageUrl(item: BundleItemDto): string {
    const fallbackId = item.productId ?? 'item';
    return item.productImageUrl || `https://picsum.photos/seed/bundle-item-${fallbackId}/600/450`;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadBundle(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam);
    if (!Number.isFinite(id) || id <= 0) {
      this.error = 'Invalid bundle id.';
      this.isLoading = false;
      return;
    }
    this.isLoading = true;
    this.error = null;
    this.ecommerceService.getBundleById(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.bundle = response.data ?? null;
          if (!this.bundle) {
            this.error = 'Bundle not found.';
          }
          this.isLoading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || 'Failed to load bundle.';
          this.isLoading = false;
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
}
