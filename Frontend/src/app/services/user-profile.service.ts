import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserListDto {
  id: number;
  username: string;
  role?: string | null;
}

@Injectable({ providedIn: 'root' })
export class UserProfileService {
  private readonly baseUrl = 'http://localhost:8070/api';

  constructor(private http: HttpClient) {}

  listUsers(): Observable<UserListDto[]> {
    return this.http.get<UserListDto[]>(`${this.baseUrl}/user/list`);
  }
}
