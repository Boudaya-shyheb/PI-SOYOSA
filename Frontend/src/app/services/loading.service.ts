import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private loadingMap = new Map<string, boolean>();
  private loadingSubject = new BehaviorSubject<boolean>(false);
  private loadingKeysSubject = new BehaviorSubject<Set<string>>(new Set());

  /** Observable that emits true when any loading operation is in progress */
  loading$: Observable<boolean> = this.loadingSubject.asObservable();

  /** Observable that emits the set of active loading keys */
  loadingKeys$: Observable<Set<string>> = this.loadingKeysSubject.asObservable();

  /**
   * Start a loading operation with an optional key.
   * If no key is provided, uses 'default'.
   */
  start(key: string = 'default'): void {
    this.loadingMap.set(key, true);
    this.updateState();
  }

  /**
   * Stop a loading operation with the given key.
   */
  stop(key: string = 'default'): void {
    this.loadingMap.delete(key);
    this.updateState();
  }

  /**
   * Check if a specific loading key is active.
   */
  isLoading(key: string = 'default'): boolean {
    return this.loadingMap.has(key);
  }

  /**
   * Check if any loading operation is in progress.
   */
  get isAnyLoading(): boolean {
    return this.loadingMap.size > 0;
  }

  /**
   * Clear all loading states.
   */
  clear(): void {
    this.loadingMap.clear();
    this.updateState();
  }

  /**
   * Execute an async operation with automatic loading state management.
   */
  async withLoading<T>(operation: () => Promise<T>, key: string = 'default'): Promise<T> {
    this.start(key);
    try {
      return await operation();
    } finally {
      this.stop(key);
    }
  }

  private updateState(): void {
    this.loadingSubject.next(this.loadingMap.size > 0);
    this.loadingKeysSubject.next(new Set(this.loadingMap.keys()));
  }
}
