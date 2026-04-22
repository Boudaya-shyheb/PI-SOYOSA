import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable, of } from "rxjs";
import { catchError, map, tap } from "rxjs/operators";
import { Router } from "@angular/router";
import { Role } from '../models/api-models';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = environment.userApiUrl + '/user/';
  private authStatus = new BehaviorSubject<boolean>(this.hasValidToken());
  private roleSubject = new BehaviorSubject<Role>('USER');
  private userIdSubject = new BehaviorSubject<string>('guest');

  constructor(private http: HttpClient, private router: Router) { }

  login(data: any): Observable<any> {
    return this.http.post<any>(this.baseUrl + "login", data).pipe(
      tap(response => {
        if (response && response.token) {
          this.setSession(response.token);
        }
      })
    );
  }

  loginWithGoogle(): void {
    window.location.href = environment.userApiUrl + '/oauth2/authorization/google';
  }

  loginWithLinkedIn(): void {
    window.location.href = environment.userApiUrl + '/oauth2/authorization/linkedin';
  }

  register(data: any): Observable<any> {
    return this.http.post<any>(this.baseUrl + "register", data);
  }

  isLoggedIn(): boolean {
    return this.hasValidToken();
  }

  setToken(token: string): void {
    sessionStorage.setItem('access_token', token);
    this.authStatus.next(true);
  }

  validateToken(): Observable<boolean> {
    return this.http.get<{ valid: boolean }>(this.baseUrl + 'validate').pipe(
      map(response => response.valid),
      catchError(() => of(false))
    );
  }

  getToken(): string | null {
    return sessionStorage.getItem('access_token');
  }

  setSession(token: string): void {
    sessionStorage.setItem('access_token', token);
    this.authStatus.next(true);

    const decoded = this.decodeToken(token);
    if (decoded) {
      if (decoded.role) {
        this.roleSubject.next(decoded.role as Role);
      }
      const userId = decoded.userId || decoded.id || decoded.sub;
      if (userId) {
        this.userIdSubject.next(String(userId));
      }
    }
  }

  logout(): void {
    sessionStorage.removeItem('access_token');
    this.authStatus.next(false);
    this.roleSubject.next('USER');
    this.userIdSubject.next('guest');
    this.router.navigate(['/user/login']);
  }

  getAuthStatus(): Observable<boolean> {
    return this.authStatus.asObservable();
  }

  // Check token on initialization
  private hasValidToken(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const decoded = this.decodeToken(token);
      if (!decoded || !decoded.exp) return false;
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
      
      // Fix Base64URL to standard Base64
      let base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      
      // Add padding if necessary
      const pad = base64.length % 4;
      if (pad) {
        if (pad === 1) return null;
        base64 += new Array(5 - pad).join('=');
      }
      
      return JSON.parse(atob(base64));
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
    this.roleSubject.next('USER');
    this.userIdSubject.next('guest');
  }

  getRole(): Role {
    const decoded = this.getUserInfo();
    return (decoded?.role as Role) || this.roleSubject.value;
  }

  getUserRole(): string | null {
    return this.getRole();
  }

  getUserId(): string {
    const decoded = this.getUserInfo();
    const id = decoded?.userId || decoded?.id || decoded?.sub || this.userIdSubject.value;
    return String(id);
  }

  roleChanges(): Observable<Role> {
    return this.roleSubject.asObservable();
  }

  setRole(role: Role): void {
    this.roleSubject.next(role);
  }

  setUserId(userId: string): void {
    this.userIdSubject.next(userId);
  }

  isStudent(): boolean {
    const role = this.getRole();
    return role === 'STUDENT' || role === 'USER';
  }

  isTeacher(): boolean {
    const role = this.getRole();
    return role === 'TEACHER' || role === 'TUTOR';
  }

  isTutor(): boolean {
    return this.getRole() === 'TUTOR' || this.getRole() === 'TEACHER';
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }

  getUserEmail(): string | null {
    const decoded = this.getUserInfo();
    return decoded?.email || decoded?.sub || null;
  }

  getUserStatus(): string | null {
    const decoded = this.getUserInfo();
    return decoded?.status || null;
  }

  getUserDisplayName(): string | null {
    const payload = this.getUserInfo();
    if (!payload) return null;
    return payload.name || payload.unique_name || payload.preferred_username || payload.sub || null;
  }

  isGuest(): boolean {
    return this.getRole() === 'USER' && !this.isLoggedIn();
  }

  canModifyContent(): boolean {
    return this.getRole() === 'TUTOR' || this.getRole() === 'ADMIN';
  }

  canEnroll(): boolean {
    return this.isLoggedIn() && this.getRole() === 'STUDENT';
  }

  canViewContent(): boolean {
    return true;
  }

  getAllUsersByRole(role: string, page: number = 0, size: number = 10, search: string = ''): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}all/${role}?page=${page}&size=${size}&search=${search}`);
  }

  isClient(): boolean {
    return this.isStudent() || this.isGuest();
  }

  getEmail(): string {
    return this.getUserEmail() || '';
  }

  getProfile(): any {
    const info = this.getUserInfo();
    return {
      email: info?.email || '',
      firstName: info?.name || '',
      lastName: '',
      phoneNumber: ''
    };
  }
}
