import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserInfoDto {
  userId: string;
  username: string;
  email?: string | null;
  firstName?: string | null;
  lastName?: string | null;
}

@Injectable({ providedIn: 'root' })
export class UserLookupService {
  private readonly baseUrl = 'http://localhost:8070/api/api/internal/user';

  constructor(private http: HttpClient) {}

  getUserInfo(username: string): Observable<UserInfoDto> {
    return this.http.get<UserInfoDto>(`${this.baseUrl}/${encodeURIComponent(username)}`);
  }
}
