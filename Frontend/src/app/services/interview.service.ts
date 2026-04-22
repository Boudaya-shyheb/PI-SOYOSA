import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class InterviewService {

    baseUrl = 'http://localhost:8071/api/entretiens';

    constructor(private http: HttpClient) { }


    getAllInterviews() {
        return this.http.get(`${this.baseUrl}`);
    }

    getInterviewById(id: number) {
        return this.http.get(`${this.baseUrl}/${id}`);
    }

    createInterview(interview: any) {
        return this.http.post(`${this.baseUrl}`, interview);
    }

    updateInterview(id: number, interview: any) {
        return this.http.put(`${this.baseUrl}/${id}/schedule`, interview);
    }

    getInterviewByUsername(username: string | null) {
        return this.http.get(`${this.baseUrl}/user/${username}`);
    }

    passInterview(id: number) {
        return this.http.put(`${this.baseUrl}/${id}/pass`, {});
    }

    failInterview(id: number) {
        return this.http.put(`${this.baseUrl}/${id}/fail`, {});
    }

}
