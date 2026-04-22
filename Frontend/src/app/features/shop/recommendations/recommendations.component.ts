import { Component, OnInit, OnDestroy, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EcommerceAdminApiService, ProductDto, RelatedProductsDTO } from '../../../services/ecommerce-admin-api.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-shop-recommendations',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './recommendations.component.html',
  styleUrls: ['./recommendations.component.css']
})
export class RecommendationsComponent implements OnInit, OnDestroy {
  @Input() productId: number = 1;

  relatedProducts: ProductDto[] = [];
  bestSellers: ProductDto[] = [];
  trending: ProductDto[] = [];
  isLoading = false;
  error: string | null = null;
  activeTab: 'related' | 'bestsellers' | 'trending' = 'related';
  private destroy$ = new Subject<void>();

  constructor(private ecommerceService: EcommerceAdminApiService) {}

  ngOnInit(): void {
    this.loadRecommendations();
  }

  private loadRecommendations(): void {
    if (!this.productId) return;

    this.isLoading = true;
    this.error = null;

    this.ecommerceService.getRecommendations(this.productId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          const data: RelatedProductsDTO = response.data;
          this.relatedProducts = data.relatedProducts || [];
          this.bestSellers = data.bestSellers || [];
          this.trending = data.trending || [];
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Failed to load recommendations:', err);
          this.error = 'Failed to load recommendations';
          this.isLoading = false;
        }
      });
  }

  setActiveTab(tab: 'related' | 'bestsellers' | 'trending'): void {
    this.activeTab = tab;
  }

  getActiveProducts(): ProductDto[] {
    switch (this.activeTab) {
      case 'related':
        return this.relatedProducts;
      case 'bestsellers':
        return this.bestSellers;
      case 'trending':
        return this.trending;
      default:
        return [];
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
