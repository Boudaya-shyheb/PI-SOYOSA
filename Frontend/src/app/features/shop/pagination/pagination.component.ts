import { Component, OnInit, OnDestroy, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EcommerceAdminApiService, ProductDto, PageResponse } from '../../../services/ecommerce-admin-api.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-shop-pagination',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.css']
})
export class PaginationComponent implements OnInit, OnDestroy {
  @Input() pageSize = 10;
  @Output() productsLoaded = new EventEmitter<ProductDto[]>();
  @Output() pageChanged = new EventEmitter<number>();

  currentPage = 0;
  products: ProductDto[] = [];
  pageResponse: PageResponse<ProductDto> | null = null;
  isLoading = false;
  pageSizeOptions = [5, 10, 15, 20];
  private destroy$ = new Subject<void>();

  constructor(private ecommerceService: EcommerceAdminApiService) {}

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(page: number): void {
    this.isLoading = true;
    this.currentPage = page;
    this.pageChanged.emit(page);

    this.ecommerceService.getProductsPaginated(page, this.pageSize, 'id', 'asc')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.pageResponse = response.data;
          this.products = response.data.content || [];
          this.productsLoaded.emit(this.products);
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Failed to load paginated products:', err);
          this.products = [];
          this.isLoading = false;
        }
      });
  }

  goToPage(page: number): void {
    if (this.pageResponse && page >= 0 && page < this.pageResponse.totalPages) {
      this.loadPage(page);
    }
  }

  nextPage(): void {
    if (this.pageResponse && this.pageResponse.hasNext) {
      this.loadPage(this.currentPage + 1);
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.loadPage(this.currentPage - 1);
    }
  }

  onPageSizeChange(newSize: number): void {
    this.pageSize = newSize;
    this.loadPage(0);
  }

  getPageNumbers(): number[] {
    if (!this.pageResponse) return [];
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(0, this.currentPage - Math.floor(maxVisible / 2));
    const end = Math.min(this.pageResponse.totalPages, start + maxVisible);

    if (end - start < maxVisible) {
      start = Math.max(0, end - maxVisible);
    }

    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
