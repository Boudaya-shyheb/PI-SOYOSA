import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class PasswordService {

  constructor(private http: HttpClient) { }

    baseUrl = 'http://localhost:8080/api/user';

  forgotPassword(email: string) {
    return this.http.post(`${this.baseUrl}/forgot-password?email=${email}`, {});
  }



}
