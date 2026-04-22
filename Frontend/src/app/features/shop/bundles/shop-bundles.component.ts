import { Component, OnInit, OnDestroy } from '@angular/core';
import { EcommerceAdminApiService, BundleDto } from '../../../services/ecommerce-admin-api.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-shop-bundles',
  templateUrl: './shop-bundles.component.html',
  styleUrls: ['./shop-bundles.component.css']
})
export class ShopBundlesComponent implements OnInit, OnDestroy {
  bundles: any[] = [];
  isLoading = true;
  error: string | null = null;
  private destroy$ = new Subject<void>();

  constructor(private ecommerceService: EcommerceAdminApiService) {}

  ngOnInit(): void {
    this.loadBundles();
  }

  private loadBundles(): void {
    this.isLoading = true;
    this.error = null;

    this.ecommerceService.listBundles()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.bundles = response.data.map((bundle: BundleDto) => {
            const originalPrice = (bundle.items ?? []).reduce((sum, item) => {
              const unit = Number(item.productPrice || 0);
              return sum + unit * item.quantity;
            }, 0);
            const hasDiscount = originalPrice > bundle.price;
            return {
              id: bundle.id,
              title: bundle.name,
              description: bundle.description || 'No description',
              price: bundle.price,
              oldPrice: hasDiscount ? originalPrice : null,
              savings: hasDiscount ? originalPrice - bundle.price : null,
              items: bundle.items.length,
              image: bundle.imageUrl || 'https://picsum.photos/seed/default/900/700',
              status: bundle.status
            };
          });
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Failed to load bundles:', err);
          this.error = 'Failed to load bundles';
          this.isLoading = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }}