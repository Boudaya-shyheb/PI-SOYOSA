import { Component, OnInit } from '@angular/core';
import {
  EcommerceAdminApiService,
  ExternalInsightsSummaryDto,
  ExternalInsightRecordDto,
  PageResponse
} from '../../../services/ecommerce-admin-api.service';

@Component({
  selector: 'app-shop-insights',
  templateUrl: './shop-insights.component.html',
  styleUrls: ['../admin/shop-admin.component.css', './shop-insights.component.css']
})
export class ShopInsightsComponent implements OnInit {
  summary: ExternalInsightsSummaryDto = {
    productsWithInsights: 0,
    totalRecords: 0,
    openAlexCount: 0,
    crossrefCount: 0,
    lastUpdatedAt: null
  };
  records: ExternalInsightRecordDto[] = [];
  searchQuery = '';
  sourceFilter: 'ALL' | 'openalex' | 'crossref' = 'ALL';
  matchFilter: 'ALL' | 'MATCHED' | 'UNMATCHED' = 'ALL';
  currentPage = 0;
  pageSize = 25;
  totalElements = 0;
  totalPages = 0;
  hasNext = false;
  hasPrevious = false;
  loadingSummary = false;
  loadingCatalog = false;
  errorMessage = '';

  constructor(private api: EcommerceAdminApiService) {}

  ngOnInit(): void {
    this.loadSummaryAndCatalog();
  }

  get filteredRecords(): ExternalInsightRecordDto[] {
    const term = this.searchQuery.trim().toLowerCase();
    return this.records.filter((record) => {
      if (this.sourceFilter !== 'ALL' && record.source !== this.sourceFilter) {
        return false;
      }
      if (this.matchFilter === 'MATCHED' && !record.matched) {
        return false;
      }
      if (this.matchFilter === 'UNMATCHED' && record.matched) {
        return false;
      }
      if (!term) {
        return true;
      }
      const haystack = [
        record.title,
        record.authors,
        record.isbn,
        record.publisher,
        record.productName,
        record.productIsbn
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();
      return haystack.includes(term);
    });
  }

  loadSummaryAndCatalog(): void {
    this.loadingSummary = true;
    this.api.getExternalInsightsSummary().subscribe({
      next: (response) => {
        this.summary = response.data || this.summary;
        this.loadingSummary = false;
        this.loadCatalog(0);
      },
      error: () => {
        this.loadingSummary = false;
        this.loadCatalog(0);
      }
    });
  }

  loadCatalog(page: number): void {
    this.loadingCatalog = true;
    this.errorMessage = '';
    this.api.getExternalInsightsCatalog(page, this.pageSize).subscribe({
      next: (response) => {
        const data = response.data as PageResponse<ExternalInsightRecordDto> | null;
        this.records = data?.content || [];
        this.currentPage = data?.currentPage ?? 0;
        this.pageSize = data?.pageSize ?? this.pageSize;
        this.totalElements = data?.totalElements ?? this.records.length;
        this.totalPages = data?.totalPages ?? 0;
        this.hasNext = data?.hasNext ?? false;
        this.hasPrevious = data?.hasPrevious ?? false;
        this.loadingCatalog = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load external insights catalog.';
        this.loadingCatalog = false;
      }
    });
  }

  goToPrevious(): void {
    if (!this.hasPrevious) {
      return;
    }
    this.loadCatalog(this.currentPage - 1);
  }

  goToNext(): void {
    if (!this.hasNext) {
      return;
    }
    this.loadCatalog(this.currentPage + 1);
  }

  updatePageSize(value: string): void {
    const size = Number(value);
    if (!Number.isFinite(size) || size <= 0) {
      return;
    }
    this.pageSize = size;
    this.loadCatalog(0);
  }

  isDifferentTitle(record: ExternalInsightRecordDto): boolean {
    if (!record.matched) {
      return false;
    }
    const score = this.textSimilarity(record.title, record.productName);
    const isBook = (record.productCategoryName || '').toLowerCase() === 'books';
    return score > 0 && score < (isBook ? 0.75 : 0.9);
  }

  isDifferentIsbn(record: ExternalInsightRecordDto): boolean {
    if (!record.matched) {
      return false;
    }
    const left = this.normalizeIsbn(record.isbn);
    const right = this.normalizeIsbn(record.productIsbn);
    if (!left || !right) {
      return false;
    }
    return left !== right;
  }

  private normalizeIsbn(value?: string | null): string | null {
    if (!value) {
      return null;
    }
    const trimmed = value.trim();
    if (!trimmed) {
      return null;
    }
    return trimmed.replace(/[\s-]/g, '').toUpperCase();
  }

  private textSimilarity(left?: string | null, right?: string | null): number {
    if (!left || !right) {
      return 0;
    }
    const leftTokens = this.tokenize(left);
    const rightTokens = this.tokenize(right);
    if (!leftTokens.length || !rightTokens.length) {
      return 0;
    }
    let overlap = 0;
    for (const token of leftTokens) {
      if (rightTokens.includes(token)) {
        overlap += 1;
      }
    }
    const denom = Math.max(leftTokens.length, rightTokens.length);
    return denom === 0 ? 0 : overlap / denom;
  }

  private tokenize(value: string): string[] {
    const normalized = value.toLowerCase().replace(/[^a-z0-9]+/g, ' ').trim();
    if (!normalized) {
      return [];
    }
    const stopWords = new Set([
      'the', 'and', 'for', 'with', 'from', 'into', 'that', 'this', 'your', 'you', 'our', 'are', 'was', 'were',
      'book', 'books'
    ]);
    return normalized
      .split(/\s+/)
      .filter((token) => token.length > 1 && !stopWords.has(token));
  }

  formatSource(source: string): string {
    if (source === 'openalex') {
      return 'OpenAlex';
    }
    if (source === 'crossref') {
      return 'Crossref';
    }
    return source;
  }
}
