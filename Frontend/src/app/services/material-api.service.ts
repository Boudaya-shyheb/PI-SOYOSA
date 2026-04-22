import { Injectable } from '@angular/core';
import { HttpClient, HttpProgressEvent, HttpResponse } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { COURSE_API_BASE_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';
import { MaterialCreateRequest, MaterialResponse, MaterialUpdateRequest } from '../models/api-models';

export interface FileUploadProgress {
  loaded: number;
  total: number;
  percent: number;
}

@Injectable({ providedIn: 'root' })
export class MaterialApiService {
  private readonly baseUrl = COURSE_API_BASE_URL;

  constructor(private http: HttpClient, private headers: ApiHeadersService) {}

  createMaterial(request: MaterialCreateRequest): Observable<MaterialResponse> {
    return this.http.post<MaterialResponse>(`${this.baseUrl}/materials`, request, { headers: this.headers.buildHeaders() });
  }

  updateMaterial(materialId: string, request: MaterialUpdateRequest): Observable<MaterialResponse> {
    return this.http.put<MaterialResponse>(`${this.baseUrl}/materials/${materialId}`, request, { headers: this.headers.buildHeaders() });
  }

  getMaterial(materialId: string): Observable<MaterialResponse> {
    return this.http.get<MaterialResponse>(`${this.baseUrl}/materials/${materialId}`);
  }

  listMaterials(lessonId: string): Observable<MaterialResponse[]> {
    return this.http.get<MaterialResponse[]>(`${this.baseUrl}/lessons/${lessonId}/materials`);
  }

  deleteMaterial(materialId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/materials/${materialId}`, { headers: this.headers.buildHeaders() });
  }

  /**
   * Upload a file for a material
   * Emits progress events and finally the updated material with file URL
   */
  uploadMaterialFile(materialId: string, file: File): Observable<FileUploadProgress | MaterialResponse> {
    const subject = new Subject<FileUploadProgress | MaterialResponse>();
    const formData = new FormData();
    formData.append('file', file);

    this.http.post<MaterialResponse>(
      `${this.baseUrl}/materials/${materialId}/upload`,
      formData,
      { 
        headers: this.headers.buildHeaders(),
        reportProgress: true,
        observe: 'events'
      }
    ).subscribe({
      next: (event: any) => {
        if (event.type === 1 || ('loaded' in event && 'total' in event)) {
          const total = event.total || 0;
          const percent = total > 0 ? Math.round((event.loaded / total) * 100) : 0;
          subject.next({ loaded: event.loaded, total, percent });
        } else if (event.type === 4 || event.body) {
          subject.next(event.body as MaterialResponse);
          subject.complete();
        }
      },
      error: (error) => {
        subject.error(error);
      }
    });

    return subject.asObservable();
  }

  /**
   * Get supported file types for different material types
   */
  getSupportedMimeTypes(): Record<string, string[]> {
    return {
      VIDEO: ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime'],
      AUDIO: ['audio/mpeg', 'audio/wav', 'audio/ogg', 'audio/aac'],
      IMAGE: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
      PDF: ['application/pdf'],
      DOCUMENT: ['application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
      SLIDES: ['application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation']
    };
  }

  /**
   * Validate file before upload
   */
  validateFile(file: File, maxSizeMB: number = 100): { valid: boolean; error?: string } {
    const maxBytes = maxSizeMB * 1024 * 1024;
    if (file.size > maxBytes) {
      return { valid: false, error: `File size must be under ${maxSizeMB}MB` };
    }
    return { valid: true };
  }
}
