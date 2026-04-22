import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export interface Enrollment {
    id?: number;
    student: any;
    training: any;
    session?: any;
    enrollmentDate: string;
    present: boolean;
    certificateIssued: boolean;
    certificateIssuedDate?: string;
}

@Injectable({
    providedIn: 'root'
})
export class TrainingEnrollmentService {
    private apiUrl = 'http://localhost:8082/api/enrollment';

    constructor(private http: HttpClient, private authService: AuthService) { }

    buyTraining(trainingId: number): Observable<Enrollment> {
        // Only students can buy trainings
        if (!this.isStudent()) {
            return throwError(() => new Error('Unauthorized: Only students can enroll in trainings'));
        }
        return this.http.post<Enrollment>(`${this.apiUrl}/training/${trainingId}`, {});
    }

    enrollInSession(trainingId: number, sessionId: number): Observable<Enrollment> {
        // Only students can enroll in sessions
        if (!this.isStudent()) {
            return throwError(() => new Error('Unauthorized: Only students can enroll in sessions'));
        }
        return this.http.post<Enrollment>(`${this.apiUrl}/training/${trainingId}/session/${sessionId}`, {});
    }

    getMyEnrollments(page: number = 0, size: number = 5): Observable<any> {
        // Only students can view their own enrollments
        if (!this.isStudent()) {
            return throwError(() => new Error('Unauthorized: Only students can view enrollments'));
        }
        const params = { page: page.toString(), size: size.toString(), sort: 'enrollmentDate,desc' };
        return this.http.get<any>(`${this.apiUrl}/my-enrollments`, { params });
    }

    isStudentEnrolledInTraining(trainingId: number): Observable<{ enrolled: boolean, eligibleToReview: boolean }> {
        return this.http.get<{ enrolled: boolean, eligibleToReview: boolean }>(`${this.apiUrl}/training/${trainingId}/status`);
    }

    getSessionEnrollments(sessionId: number): Observable<Enrollment[]> {
        // Only tutors can view session enrollments
        if (!this.canModifyTraining()) {
            return throwError(() => new Error('Unauthorized: Only tutors can view session enrollments'));
        }
        return this.http.get<Enrollment[]>(`${this.apiUrl}/session/${sessionId}`);
    }

    updatePresence(enrollmentId: number, present: boolean): Observable<Enrollment> {
        // Only tutors can update presence
        if (!this.canModifyTraining()) {
            return throwError(() => new Error('Unauthorized: Only tutors can update presence'));
        }
        return this.http.patch<Enrollment>(`${this.apiUrl}/${enrollmentId}/presence`, null, {
            params: { present: present.toString() }
        });
    }

    issueCertificate(enrollmentId: number): Observable<Enrollment> {
        // Only tutors can issue certificates
        if (!this.canModifyTraining()) {
            return throwError(() => new Error('Unauthorized: Only tutors can issue certificates'));
        }
        return this.http.post<Enrollment>(`${this.apiUrl}/${enrollmentId}/issue-certificate`, {});
    }

    getUpcomingEnrollments(): Observable<Enrollment[]> {
        return this.http.get<Enrollment[]>(`${this.apiUrl}/upcoming`);
    }

    getMyCertificates(): Observable<Enrollment[]> {
        // Only students can view their certificates
        if (!this.isStudent()) {
            return throwError(() => new Error('Unauthorized: Only students can view certificates'));
        }
        return this.http.get<Enrollment[]>(`${this.apiUrl}/my-certificates`);
    }

    // ========== ROLE-BASED ACCESS CONTROL ==========
    private canModifyTraining(): boolean {
        const role = this.authService.getRole();
        return role === 'TUTOR' || role === 'TEACHER' || role === 'ADMIN';
    }

    private isStudent(): boolean {
        const role = this.authService.getRole();
        return role === 'STUDENT' || role === 'USER';
    }
}
