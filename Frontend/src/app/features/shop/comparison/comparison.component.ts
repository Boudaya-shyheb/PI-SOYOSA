import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { EcommerceAdminApiService, ComparisonRequest, ProductDto, ProductComparisonDTO, ComparisonItemDto } from '../../../services/ecommerce-admin-api.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-shop-comparison',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './comparison.component.html',
  styleUrls: ['./comparison.component.css']
})
export class ComparisonComponent implements OnInit, OnDestroy {
  selectedProductIds: number[] = [];
  comparisonProducts: any[] = [];
  allProducts: any[] = [];
  comparisonAttributes: { [key: string]: string[] } = {};
  isLoading = false;
  isExpanded = true;
  error: string | null = null;
  maxProducts = 4;
  private destroy$ = new Subject<void>();

  constructor(
    private ecommerceService: EcommerceAdminApiService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check for products in localStorage first
    this.loadFromStorageAndSetup();
    
    // Load all products
    this.loadAllProducts();
  }

  private loadFromStorageAndSetup(): void {
    const stored = localStorage.getItem('comparison_products');
    if (stored) {
      try {
        const storedProducts = JSON.parse(stored);
        if (storedProducts && storedProducts.length > 0) {
          this.selectedProductIds = storedProducts
            .map((item: any) => Number(item?.id))
            .filter((id: number) => Number.isFinite(id) && id > 0);
          if (this.selectedProductIds.length >= 2) {
            this.requestComparison(this.selectedProductIds);
          }
        }
      } catch (e) {
        this.comparisonProducts = [];
      }
    }
  }

  private loadAllProducts(): void {
    this.ecommerceService.listProducts()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.allProducts = response.data.map((prod: ProductDto) => ({
            id: prod.id,
            title: prod.name,
            name: prod.name,
            description: prod.description || 'No description',
            price: prod.price,
            category: prod.categoryName || 'Uncategorized',
            image: prod.imageUrl || 'https://picsum.photos/seed/default/800/600',
            imageUrl: prod.imageUrl || 'https://picsum.photos/seed/default/800/600',
            rating: prod.averageRating ?? 0,
            reviews: prod.reviewCount ?? 0,
            quantityAvailable: prod.quantityAvailable ?? 0,
            averageRating: prod.averageRating ?? 0,
            reviewCount: prod.reviewCount ?? 0
          }));
        },
        error: (err) => {
          console.error('Failed to load products:', err);
        }
      });
  }

  private filterComparisonProducts(ids: number[]): void {
    this.selectedProductIds = ids.filter((id) => Number.isFinite(id) && id > 0);
    if (this.selectedProductIds.length >= 2) {
      this.requestComparison(this.selectedProductIds);
    } else {
      this.comparisonProducts = [];
      this.comparisonAttributes = {};
    }
  }

  toggleProductSelection(productId: number): void {
    if (this.selectedProductIds.includes(productId)) {
      this.selectedProductIds = this.selectedProductIds.filter(id => id !== productId);
    } else {
      if (this.selectedProductIds.length < this.maxProducts) {
        this.selectedProductIds.push(productId);
      } else {
        alert(`You can only compare up to ${this.maxProducts} products`);
      }
    }
  }

  compareProducts(): void {
    if (this.selectedProductIds.length < 2) {
      alert('Please select at least 2 products to compare');
      return;
    }
    this.requestComparison(this.selectedProductIds);
  }

  private requestComparison(productIds: number[]): void {
    this.isLoading = true;
    this.error = null;
    this.ecommerceService.compareProducts({ productIds })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          const items: ComparisonItemDto[] = response.data?.items ?? [];
          this.comparisonProducts = items.map((item) => ({
            id: item.id,
            title: item.name,
            name: item.name,
            description: item.description || 'No description',
            price: item.price,
            category: item.category?.name || 'Uncategorized',
            image: item.imageUrl || 'https://picsum.photos/seed/default/800/600',
            imageUrl: item.imageUrl || 'https://picsum.photos/seed/default/800/600',
            rating: item.averageRating ?? 0,
            reviews: item.reviewCount ?? 0,
            quantityAvailable: item.quantityAvailable ?? 0,
            averageRating: item.averageRating ?? 0,
            reviewCount: item.reviewCount ?? 0,
            inStock: item.inStock ?? false
          }));
          this.comparisonAttributes = {
            'Price': this.comparisonProducts.map(p => `${p.price} TND`),
            'Category': this.comparisonProducts.map(p => p.category),
            'Rating': this.comparisonProducts.map(p => `${p.rating}★`),
            'Reviews': this.comparisonProducts.map(p => `${p.reviews} reviews`),
            'In stock': this.comparisonProducts.map(p => p.inStock ? 'Yes' : 'No'),
            'Available qty': this.comparisonProducts.map(p => `${p.quantityAvailable}`)
          };
          this.isLoading = false;
        },
        error: (err) => {
          this.isLoading = false;
          this.error = err?.error?.message || 'Failed to compare products.';
          this.comparisonProducts = [];
          this.comparisonAttributes = {};
        }
      });
  }

  clearComparison(): void {
    this.selectedProductIds = [];
    this.comparisonProducts = [];
    this.comparisonAttributes = {};
    this.error = null;
    localStorage.removeItem('comparison_products');
  }

  removeProduct(productId: number): void {
    this.comparisonProducts = this.comparisonProducts.filter(p => p.id !== productId);
    localStorage.setItem('comparison_products', JSON.stringify(this.comparisonProducts));
    this.selectedProductIds = this.comparisonProducts.map(p => p.id);
    if (this.selectedProductIds.length >= 2) {
      this.requestComparison(this.selectedProductIds);
    } else {
      this.comparisonAttributes = {};
      this.router.navigate(['/shop/catalog']);
    }
  }

  toggleExpanded(): void {
    this.isExpanded = !this.isExpanded;
  }

  getAttributeKeys(): string[] {
    return Object.keys(this.comparisonAttributes);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
