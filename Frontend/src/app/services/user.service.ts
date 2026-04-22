import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';
import { map } from 'rxjs/operators';

export interface UserDto {
    userId: number;
    username: string;
}

export interface SelectableUser {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber?: string;
  age?: number;
  educationLevel?: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = `${environment.apiUrl}/users`;
  private profilesUrl = 'http://localhost:8070/api/profiles';

  constructor(private http: HttpClient) {}

  getSelectableUsers(): Observable<SelectableUser[]> {
    return this.http.get<SelectableUser[]>(`${this.apiUrl}/selectable`);
  }

  getAllUsers(): Observable<UserDto[]> {
    return this.http.get<any[]>(this.profilesUrl).pipe(
      map(profiles => profiles.map(p => ({
        userId: p.user?.user_id,
        username: p.user?.username
      })).filter(u => u.userId != null))
    );
  }
}
