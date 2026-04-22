import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class BlogService {

  constructor(private http: HttpClient) {}

    BASE_URL = "http://localhost:8072/api/blogs";

  getAllBlogs() {
    return this.http.get(`${this.BASE_URL}`);
  }

  getBlogById(id: string) {
    return this.http.get(`${this.BASE_URL}/${id}`);
  }

  createBlog(formData: FormData):Observable<any> {
      return this.http.post(`${this.BASE_URL}`, formData);
  }

    likeBlog(id: number, userId: any): Observable<any> {
      const data ={
          userId :userId
      }
        return this.http.put(`${this.BASE_URL}/${id}/like`, data);
    }

    dislikeBlog(id: number, userId: any): Observable<any> {
        return this.http.put(`${this.BASE_URL}/${id}/dislike/${userId}`, {});
    }

    getBlogs(page: number, size: number, search: string, sort: string) {
        return this.http.get(
            `${this.BASE_URL}?page=${page}&size=${size}&search=${search}&sort=${sort}`
        );
    }

    checkLike(id: number, userId: any) {
      return this.http.get(`${this.BASE_URL}/check_like/${id}/${userId}`, {});
    }

}
