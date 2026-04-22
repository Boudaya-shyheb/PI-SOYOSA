import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EcommerceAdminApiService, SearchRequest, ProductDto, PageResponse } from '../../../services/ecommerce-admin-api.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-shop-advanced-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './advanced-search.component.html',
  styleUrls: ['./advanced-search.component.css']
})
export class AdvancedSearchComponent implements OnInit, OnDestroy {
  searchRequest: SearchRequest = {};
  searchResults: ProductDto[] = [];
  isLoading = false;
  isExpanded = false;
  totalResults = 0;
  private destroy$ = new Subject<void>();

  // Filter ranges
  minPrice = 0;
  maxPrice = 1000;
  minRating = 0;
  maxRating = 5;
  sortOptions = ['price', 'rating', 'name', 'id'];
  directionOptions = ['asc', 'desc'];

  constructor(private ecommerceService: EcommerceAdminApiService) {}

  ngOnInit(): void {
    // Initialize with default search
    this.performSearch();
  }

  performSearch(): void {
    if (!this.validateFilters()) {
      return;
    }

    this.isLoading = true;
    this.searchRequest = {
      keyword: this.searchRequest.keyword?.trim() || undefined,
      minPrice: this.searchRequest.minPrice || undefined,
      maxPrice: this.searchRequest.maxPrice || undefined,
      minRating: this.searchRequest.minRating || undefined,
      maxRating: this.searchRequest.maxRating || undefined,
      categoryId: this.searchRequest.categoryId || undefined,
      sortBy: this.searchRequest.sortBy || 'id',
      sortDirection: this.searchRequest.sortDirection || 'asc'
    };

    this.ecommerceService.advancedSearch(this.searchRequest, 0, 10)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.searchResults = response.data.content || [];
          this.totalResults = response.data.totalElements || 0;
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Advanced search failed:', err);
          this.searchResults = [];
          this.isLoading = false;
        }
      });
  }

  private validateFilters(): boolean {
    if (this.searchRequest.minPrice !== undefined && this.searchRequest.maxPrice !== undefined) {
      if (this.searchRequest.minPrice > this.searchRequest.maxPrice) {
        alert('Min price cannot be greater than max price');
        return false;
      }
    }

    if (this.searchRequest.minRating !== undefined && this.searchRequest.maxRating !== undefined) {
      if (this.searchRequest.minRating > this.searchRequest.maxRating) {
        alert('Min rating cannot be greater than max rating');
        return false;
      }
    }

    return true;
  }

  resetFilters(): void {
    this.searchRequest = {
      sortBy: 'id',
      sortDirection: 'asc'
    };
    this.performSearch();
  }

  toggleExpanded(): void {
    this.isExpanded = !this.isExpanded;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
