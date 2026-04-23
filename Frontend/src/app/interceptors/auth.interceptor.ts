import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private readonly externalHosts = new Set([
    'api.mapbox.com',
    'router.project-osrm.org',
    'nominatim.openstreetmap.org'
  ]);

  constructor(private auth: AuthService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (req.url.startsWith('https://api.cloudinary.com/') || this.isExternalRequest(req.url)) {
      return next.handle(req);
    }
    const token = this.auth.getToken();
    const role = this.auth.getRole();
    const userId = this.auth.getUserId();
    const email = this.auth.getUserEmail();

    const headers: Record<string, string> = {
      'X-Role': role,
      'User-Role': role
    };

    if (email) {
      headers['User-Email'] = email;
    }

    if (userId) {
      headers['X-User-Id'] = userId;
    }

    // Add JWT token to Authorization header if available
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const cloned = req.clone({
      setHeaders: headers
    });

    return next.handle(cloned);
  }
  private isExternalRequest(url: string): boolean {
  try {
    const host = new URL(url).host;
    return this.externalHosts.has(host);
  } catch {
    return false;
  }
}
}

