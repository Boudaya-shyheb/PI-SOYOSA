import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {AuthService} from "./auth.service";

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  constructor(private http: HttpClient, private authService: AuthService) { }

    private baseUrl = 'http://localhost:8072/api/comments';



    // Get all comments by blog
    getCommentsByBlog(blogId: number): Observable<any> {
        return this.http.get(`${this.baseUrl}/all-by-blog/${blogId}`);
    }

    // Create comment
    createComment(comment: any): Observable<any> {
        return this.http.post(this.baseUrl, comment);
    }

    // Update comment
    updateComment(id: number, comment: any): Observable<any> {
        const payload = {
            ...comment,
            blog: { blogId: comment.blog.blogId }
        };
        return this.http.put(`${this.baseUrl}/${id}`, payload);
    }

    // Delete comment
    deleteComment(id: number): Observable<any> {
        return this.http.delete(`${this.baseUrl}/${id}`);
    }

    // Report comment
    reportComment(id: number, reason: string): Observable<any> {
        const data={
            userId : this.authService.getUserEmail(),
            reason: reason
        }
        return this.http.post(`${this.baseUrl}/${id}/report`, data);
    }

    likeComment(id: number, userId: any): Observable<any> {
        const data ={
            userId : userId,
        }
        return this.http.put(`${this.baseUrl}/${id}/like`, data);
    }

    dislikeComment(id: number, userId: any): Observable<any> {
        return this.http.put(`${this.baseUrl}/${id}/dislike/${userId}`, {});
    }

    checkLike(id:any, userId: any) {
        return this.http.get(`${this.baseUrl}/check_like/${id}/${userId}`, {});
    }

}
