import { Injectable } from '@angular/core';
import { HttpClient, HttpProgressEvent, HttpResponse } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';

export interface UploadProgress {
  loaded: number;
  total: number;
  percent: number;
}

export interface UploadResponse {
  url: string;
  fileName: string;
  fileSize: number;
}

@Injectable({ providedIn: 'root' })
export class FileUploadService {
  private apiUrl = 'http://localhost:8082/api';

  constructor(private http: HttpClient) {}

  uploadFile(file: File, materialId?: string): Observable<UploadResponse | HttpProgressEvent> {
    const formData = new FormData();
    formData.append('file', file);

    const url = materialId 
      ? `${this.apiUrl}/materials/${materialId}/upload`
      : `${this.apiUrl}/files`;

    return this.http.post<UploadResponse>(url, formData, {
      reportProgress: true,
      responseType: 'json'
    });
  }

  /**
   * Upload a file and return its URL
   * Returns a subject that emits progress events and finally the uploaded URL
   */
  uploadAndGetUrl(file: File, materialId?: string): Observable<string | UploadProgress> {
    const subject = new Subject<string | UploadProgress>();

    const formData = new FormData();
    formData.append('file', file);

    const url = materialId 
      ? `${this.apiUrl}/materials/${materialId}/upload`
      : `${this.apiUrl}/files`;

    this.http.post<UploadResponse>(url, formData, {
      reportProgress: true
    }).subscribe({
      next: (event: any) => {
        if (event.type === 1 || ('loaded' in event && 'total' in event)) {
          const percent = Math.round((event.loaded / (event.total || 1)) * 100);
          subject.next({ loaded: event.loaded, total: event.total || 0, percent });
        } else if (event.type === 4 || event.body) {
          subject.next(event.body?.url || event);
          subject.complete();
        }
      },
      error: (error) => {
        subject.error(error);
      }
    });

    return subject.asObservable();
  }

  deleteFile(fileUrl: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/files`, {
      body: { url: fileUrl }
    });
  }

  /**
   * Get supported file types for lesson content
   */
  getSupportedTypes(): Record<string, string[]> {
    return {
      video: ['video/mp4', 'video/webm', 'video/ogg'],
      audio: ['audio/mpeg', 'audio/wav', 'audio/ogg'],
      document: ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'],
      image: ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
    };
  }

  /**
   * Validate file before upload
   */
  validateFile(file: File, maxSizeMB: number = 100): { valid: boolean; error?: string } {
    const maxBytes = maxSizeMB * 1024 * 1024;
    
    if (file.size > maxBytes) {
      return { valid: false, error: `File size exceeds ${maxSizeMB}MB limit` };
    }

    return { valid: true };
  }
}
