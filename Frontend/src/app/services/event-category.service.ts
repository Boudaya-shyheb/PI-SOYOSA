import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EventCategory } from '../models/event-category.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EventCategoryService {

  private apiUrl = 'http://localhost:8086/api/categories';

  constructor(private http: HttpClient) {}

  getAllCategories(): Observable<EventCategory[]> {
    return this.http.get<EventCategory[]>(this.apiUrl);
  }

  getCategoryById(id: number): Observable<EventCategory> {
    return this.http.get<EventCategory>(`${this.apiUrl}/${id}`);
  }

  createCategory(category: EventCategory): Observable<EventCategory> {
    return this.http.post<EventCategory>(this.apiUrl, category);
  }

  updateCategory(id: number, category: EventCategory): Observable<EventCategory> {
    return this.http.put<EventCategory>(`${this.apiUrl}/${id}`, category);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
