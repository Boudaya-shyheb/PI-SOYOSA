import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EcommerceAdminApiService, ProductDto, CreateReviewRequest } from '../../../services/ecommerce-admin-api.service';
import { AuthService } from '../../../services/auth.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-shop-product-detail',
  templateUrl: './shop-product-detail.component.html',
  styleUrls: ['./shop-product-detail.component.css']
})
export class ShopProductDetailComponent implements OnInit, OnDestroy {
  product: any = null;
  relatedProducts: any[] = [];
  isLoading = true;
  error: string | null = null;
  quantity = 1;
  comparisonProducts: any[] = [];
  reviewModalOpen = false;
  reviewForm = {
    customerId: 1,
    rating: 5,
    title: '',
    comment: '',
    verifiedPurchase: false
  };
  reviewError = '';
  reviewSuccess = '';
  isSubmittingReview = false;
  cartError = '';
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
    this.timerId = setInterval(() => {
      this.now = Date.now();
    }, 60000);
    this.route.paramMap.subscribe((params) => {
      const productId = params.get('id');
      if (productId) {
        this.loadProductDetail(Number(productId));
      }
    });
  }

  private loadProductDetail(productId: number): void {
    this.isLoading = true;
    this.error = null;

    this.ecommerceService.getProductById(productId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          const originalPrice = response.data.originalPrice ?? null;
          const hasDiscount = originalPrice !== null && originalPrice > response.data.price;
          this.product = {
            id: response.data.id,
            title: response.data.name,
            isbn: response.data.isbn || null,
            description: response.data.description || 'No description available',
            price: response.data.price,
            oldPrice: hasDiscount ? originalPrice : null,
            tag: hasDiscount ? 'Sale' : '',
            discountEndsAt: response.data.discountEndsAt ?? null,
            category: response.data.categoryName || 'Uncategorized',
            image: response.data.imageUrl || 'https://picsum.photos/seed/default/1000/800',
            rating: response.data.averageRating || 0,
            reviews: response.data.reviewCount || 0,
            quantity: response.data.quantityAvailable,
            highlights: [
              'Verified quality',
              'Fast delivery available',
              'Customer satisfaction guaranteed'
            ]
          };
          this.loadRelatedProducts(response.data.categoryId);
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Failed to load product:', err);
          this.error = 'Failed to load product details';
          this.isLoading = false;
        }
      });
  }

  private loadRelatedProducts(categoryId: number): void {
    this.ecommerceService.getProductsByCategory(categoryId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.relatedProducts = response.data
            .filter((prod: ProductDto) => prod.id !== this.product.id)
            .slice(0, 4)
            .map((prod: ProductDto) => ({
              id: prod.id,
              title: prod.name,
              price: prod.price,
              image: prod.imageUrl || 'https://picsum.photos/seed/default/400/400',
              category: prod.categoryName
            }));
        },
        error: (err) => console.error('Failed to load related products:', err)
      });
  }

  updateQuantity(value: number): void {
    if (value > 0 && value <= (this.product?.quantity || 1)) {
      this.quantity = value;
    }
  }

  addToCart(): void {
    if (!this.product) {
      return;
    }
    const customerId = this.resolveCustomerId();
    if (customerId === null) {
      this.cartError = 'Login required to add items to cart.';
      return;
    }
    this.cartError = '';
    this.ecommerceService.addToCart(customerId, {
      productId: this.product.id,
      quantity: this.quantity
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.router.navigate(['/shop/cart']),
        error: () => {
          this.cartError = 'Failed to add this item to cart.';
        }
      });
  }

  addToComparison(): void {
    if (this.product) {
      const productData = {
        id: this.product.id,
        title: this.product.title,
        description: this.product.description,
        price: this.product.price,
        category: this.product.category,
        image: this.product.image,
        rating: this.product.rating,
        reviews: this.product.reviews
      };
      
      if (!this.comparisonProducts.find(p => p.id === this.product.id)) {
        if (this.comparisonProducts.length < 4) {
          this.comparisonProducts.push(productData);
          this.saveComparisonToStorage();
        } else {
          alert('You can compare up to 4 products');
        }
      }
    }
  }

  goToComparison(): void {
    if (this.comparisonProducts.length > 0) {
      this.router.navigate(['/shop/comparison'], {
        queryParams: { products: this.comparisonProducts.map(p => p.id).join(',') }
      });
    }
  }

  isInComparison(): boolean {
    return this.product && this.comparisonProducts.some(p => p.id === this.product.id);
  }

  private saveComparisonToStorage(): void {
    localStorage.setItem('comparison_products', JSON.stringify(this.comparisonProducts));
  }

  private loadComparisonFromStorage(): void {
    const stored = localStorage.getItem('comparison_products');
    if (stored) {
      try {
        this.comparisonProducts = JSON.parse(stored);
      } catch (e) {
        this.comparisonProducts = [];
      }
    }
  }

  clearComparison(): void {
    this.comparisonProducts = [];
    this.saveComparisonToStorage();
  }

  openReviewModal(): void {
    if (!this.product) {
      return;
    }
    const customerId = this.resolveCustomerId();
    if (customerId === null) {
      this.reviewError = 'Login required to leave a review.';
      return;
    }
    this.reviewError = '';
    this.reviewSuccess = '';
    this.reviewForm = {
      customerId,
      rating: 5,
      title: '',
      comment: '',
      verifiedPurchase: false
    };
    this.reviewModalOpen = true;
  }

  closeReviewModal(): void {
    this.reviewModalOpen = false;
  }

  submitReview(): void {
    if (!this.product) {
      return;
    }
    const customerId = Number(this.reviewForm.customerId);
    const rating = Number(this.reviewForm.rating);
    const title = this.reviewForm.title.trim();
    const comment = this.reviewForm.comment.trim();

    if (!Number.isFinite(customerId) || customerId <= 0) {
      this.reviewError = 'Customer id is required.';
      return;
    }
    if (!Number.isFinite(rating) || rating < 1 || rating > 5) {
      this.reviewError = 'Rating must be between 1 and 5.';
      return;
    }
    if (!title || !comment) {
      this.reviewError = 'Title and comment are required.';
      return;
    }

    const payload: CreateReviewRequest = {
      productId: this.product.id,
      customerId,
      rating,
      title,
      comment,
      verifiedPurchase: this.reviewForm.verifiedPurchase
    };

    this.reviewError = '';
    this.isSubmittingReview = true;
    this.ecommerceService.createReview(payload)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.isSubmittingReview = false;
          this.reviewSuccess = 'Review submitted successfully.';
          this.reviewModalOpen = false;
          this.loadProductDetail(this.product.id);
        },
        error: (err) => {
          this.isSubmittingReview = false;
          this.reviewError = err?.error?.message || 'Failed to submit review.';
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
