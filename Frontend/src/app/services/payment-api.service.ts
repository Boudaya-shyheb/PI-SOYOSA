import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { COURSE_API_BASE_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';
import { PaymentCheckoutRequest, PaymentCheckoutResponse } from '../models/api-models';

@Injectable({ providedIn: 'root' })
export class PaymentApiService {
  private readonly baseUrl = `${COURSE_API_BASE_URL}/payments`;

  constructor(private http: HttpClient, private headers: ApiHeadersService) {}

  processCoursePayment(courseId: string): Observable<PaymentCheckoutResponse> {
    const request: PaymentCheckoutRequest = { courseId };
    return this.http.post<PaymentCheckoutResponse>(`${this.baseUrl}/checkout`, request, {
      headers: this.headers.buildHeaders()
    });
  }
}
