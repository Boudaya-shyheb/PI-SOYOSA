import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { environment } from '../environments/environment';

export interface CloudinaryUploadResponse {
  secure_url: string;
  url?: string;
}

@Injectable({ providedIn: 'root' })
export class CloudinaryUploadService {
  constructor(private http: HttpClient) {}

  isConfigured(): boolean {
    return !!environment.cloudinaryCloudName && !!environment.cloudinaryUploadPreset;
  }

  uploadImage(file: File, folder = 'ecommerce'): Observable<CloudinaryUploadResponse> {
    if (!this.isConfigured()) {
      return throwError(() => new Error('Cloudinary is not configured.'));
    }
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', environment.cloudinaryUploadPreset);
    formData.append('folder', folder);

    const url = `https://api.cloudinary.com/v1_1/${environment.cloudinaryCloudName}/image/upload`;
    return this.http.post<CloudinaryUploadResponse>(url, formData);
  }
}
