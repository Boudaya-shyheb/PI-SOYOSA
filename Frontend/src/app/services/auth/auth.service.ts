import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, map, tap } from "rxjs/operators";
import { Router } from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = 'http://localhost:8070/api/user/';
  private authStatus = new BehaviorSubject<boolean>(this.hasValidToken());

  constructor(private http: HttpClient, private router: Router) { }

  // @ts-ignore
  login(data: any): Observable<any> {
    return this.http.post<any>(this.baseUrl + "login", data).pipe(
      tap(response => {
        if (response && response.token) {
          this.setSession(response.token);
        }
      })
    );
  }

  register(data: any): Observable<any> {
    return this.http.post<any>(this.baseUrl + "register", data);
  }

  isLoggedIn(): boolean {
    return this.hasValidToken();
  }


  changePassword(newP: any, oldP: any): Observable<any> {
      const body = {
          oldPassword: oldP,
          newPassword: newP
      };
      return this.http.post<any>(this.baseUrl + 'change-password/'+ this.getUserEmail(), body);
  }

  setToken(token: string): void {
    sessionStorage.setItem('access_token', token);
    this.authStatus.next(true);
  }

  // Validate token with server (if needed)
  validateToken(): Observable<boolean> {
    return this.http.get<{ valid: boolean }>(this.baseUrl + 'validate').pipe(
      map(response => response.valid),
      catchError(() => of(false))
    );
  }

  // Get stored token
  getToken(): string | null {
    return sessionStorage.getItem('access_token');
  }

  // After successful login
  setSession(token: string): void {
    sessionStorage.setItem('access_token', token);
    this.authStatus.next(true);
  }

  // Logout
  logout(): void {
    sessionStorage.removeItem('access_token');
    this.authStatus.next(false);
    this.router.navigate(['/user/login']);
  }

  // Get auth status as observable
  getAuthStatus(): Observable<boolean> {
    return this.authStatus.asObservable();
  }

  // Check token on initialization
  private hasValidToken(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const decoded = this.decodeToken(token);
      const expirationDate = new Date(decoded.exp * 1000);
      return expirationDate > new Date();
    } catch (e) {
      return false;
    }
  }

  // Manual JWT decoding since jwtHelper is not installed
  private decodeToken(token: string): any {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      return JSON.parse(atob(parts[1]));
    } catch (e) {
      return null;
    }
  }

  // Get user info from token
  getUserInfo(): any {
    const token = this.getToken();
    return token ? this.decodeToken(token) : null;
  }

  // Check if token exists and isn't expired
  isAuthenticated(): boolean {
    return this.hasValidToken();
  }

  // Remove token on logout
  removeToken(): void {
    sessionStorage.removeItem('access_token');
    this.authStatus.next(false);
  }

  getUserRole(): string {
    const decoded = this.getUserInfo();
    return decoded?.role || 'USER';
  }

  getRole(): string {
    return this.getUserRole();
  }

  // Check if user is admin
  isAdmin(): boolean {
    return this.getUserRole() === 'ADMIN';
  }

  getUserEmail(): string | null {
    const decoded = this.getUserInfo();
    return decoded?.email || decoded?.sub || null;
  }

  getUserStatus(): string | null {
    const decoded = this.getUserInfo();
    return decoded?.status || null;
  }
}
