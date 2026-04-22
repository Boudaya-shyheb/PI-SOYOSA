import { Injectable } from '@angular/core';
import { HttpHeaders } from '@angular/common/http';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class ApiHeadersService {
  constructor(private auth: AuthService) {}

  buildHeaders(): HttpHeaders {
    return new HttpHeaders({
      'X-User-Id': this.auth.getUserId(),
      'X-Role': this.auth.getRole()
    });
  }
}
