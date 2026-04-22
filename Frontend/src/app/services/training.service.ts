import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map } from 'rxjs/operators';
import { Training, Session } from '../models/training.model';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'root'
})
export class TrainingService {

    private apiUrl = 'http://localhost:8082/api/training';
    private placementUrl = 'http://localhost:8082/api/placement-test';

    constructor(private http: HttpClient, private authService: AuthService) { }

    // ========== RECOMMENDATIONS & PLACEMENT ==========
    getRecommendations(): Observable<Training[]> {
        return this.http.get<any[]>(`${this.apiUrl}/recommendations`).pipe(
            map(list => list.map(t => this.normalizeTraining(t)))
        );
    }

    getPlacementQuestions(): Observable<any[]> {
        return this.http.get<any[]>(`${this.placementUrl}/questions`);
    }

    submitPlacementTest(answers: any): Observable<any> {
        return this.http.post<any>(`${this.placementUrl}/submit`, { answers });
    }

    getPlacementResult(): Observable<any> {
        return this.http.get<any>(`${this.placementUrl}/result`);
    }

    // ========== TRAINING METHODS ==========
    getAllTrainings(page: number = 0, size: number = 3, search?: string): Observable<any> {
        const params: any = { page: page.toString(), size: size.toString() };
        if (search) params.search = search;
        // backend returns a Page<Training> shape
        return this.http.get<any>(this.apiUrl, { params }).pipe(
            map(page => {
                if (page && Array.isArray(page.content)) {
                    page.content = page.content.map((t: any) => this.normalizeTraining(t));
                }
                return page;
            })
        );
    }

    getTrainingById(id: number): Observable<Training> {
        return this.http.get<Training>(`${this.apiUrl}/${id}`).pipe(
            map(t => this.normalizeTraining(t))
        );
    }

    createTraining(training: Training): Observable<Training> {
        // Only TUTOR, TEACHER, and ADMIN can create trainings
        if (!this.canModifyTraining()) {
            return throwError(() => new Error('Unauthorized: Only tutors can create trainings'));
        }
        return this.http.post<Training>(this.apiUrl, training);
    }

    updateTraining(id: number, training: Training): Observable<Training> {
        // Only TUTOR, TEACHER, and ADMIN can update trainings
        if (!this.canModifyTraining()) {
            return throwError(() => new Error('Unauthorized: Only tutors can update trainings'));
        }
        return this.http.put<Training>(`${this.apiUrl}/${id}`, training);
    }

    deleteTraining(id: number): Observable<void> {
        // Only TUTOR, TEACHER, and ADMIN can delete trainings
        if (!this.canModifyTraining()) {
            return throwError(() => new Error('Unauthorized: Only tutors can delete trainings'));
        }
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    uploadImage(file: File): Observable<{url: string}> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<{url: string}>(`${this.apiUrl}/image-upload`, formData);
    }

    // ========== SESSION METHODS ==========
    addSessionToTraining(trainingId: number, session: Partial<Session>): Observable<Session> {
        // Only TUTOR, TEACHER, and ADMIN can add sessions
        if (!this.canModifyTraining()) {
            return throwError(() => new Error('Unauthorized: Only tutors can add sessions'));
        }
        return this.http.post<Session>(`${this.apiUrl}/${trainingId}/sessions`, session).pipe(
            map(s => this.normalizeSession(s))
        );
    }

    getSessionsByTraining(trainingId: number, page: number = 0, size: number = 5, startDate?: string, endDate?: string): Observable<any> {
        const params: any = { page: page.toString(), size: size.toString(), sort: 'date,asc' };
        if (startDate) params.startDate = startDate;
        if (endDate) params.endDate = endDate;
        return this.http.get<any>(`${this.apiUrl}/${trainingId}/sessions`, { params }).pipe(
            map(p => {
                if (p && Array.isArray(p.content)) {
                    p.content = p.content.map((s: any) => this.normalizeSession(s));
                }
                return p;
            })
        );
    }

    updateSession(trainingId: number, sessionId: number, session: Partial<Session>): Observable<Session> {
        // Only TUTOR, TEACHER, and ADMIN can update sessions
        if (!this.canModifyTraining()) {
            return throwError(() => new Error('Unauthorized: Only tutors can update sessions'));
        }
        return this.http.put<Session>(`${this.apiUrl}/${trainingId}/sessions/${sessionId}`, session).pipe(
            map(s => this.normalizeSession(s))
        );
    }

    deleteSession(trainingId: number, sessionId: number): Observable<void> {
        // Only TUTOR, TEACHER, and ADMIN can delete sessions
        if (!this.canModifyTraining()) {
            return throwError(() => new Error('Unauthorized: Only tutors can delete sessions'));
        }
        return this.http.delete<void>(`${this.apiUrl}/${trainingId}/sessions/${sessionId}`);
    }

    markSessionCompleted(trainingId: number, sessionId: number): Observable<Session> {
        // Only TUTOR, TEACHER, and ADMIN can mark sessions as completed
        if (!this.canModifyTraining()) {
            return throwError(() => new Error('Unauthorized: Only tutors can mark sessions as completed'));
        }
        return this.http.put<Session>(`${this.apiUrl}/${trainingId}/sessions/${sessionId}/complete`, {});
    }

    // ========== REVIEW METHODS ==========
    addReview(trainingId: number, reviewRequest: any): Observable<any> {
        // Only students can add reviews
        if (!this.isStudent()) {
            return throwError(() => new Error('Unauthorized: Only students can add reviews'));
        }
        return this.http.post<any>(`${this.apiUrl}/${trainingId}/reviews`, reviewRequest);
    }

    getReviewsForTraining(trainingId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/${trainingId}/reviews`);
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

    // ---------- helpers for normalizing data returned from the backend ----------

    /**
     * Convert a raw session object from the API into our `Session` type.
     * This mostly involves turning the ISO date string into a `Date` object
     * and trimming the time string to HH:mm format (backend may return seconds).
     */
    private normalizeSession(raw: any): Session {
        if (!raw) {
            return raw;
        }
        const session: Session = { ...raw } as Session;

        // convert date string to Date instance if necessary
        if (session.date && !(session.date instanceof Date)) {
            session.date = new Date(session.date);
        }

        // ensure startTime is only hours/minutes (backend often sends H:mm:ss)
        if (typeof session.startTime === 'string') {
            session.startTime = session.startTime.substring(0, 5);
        }

        return session;
    }

    /**
     * Normalize a training object, applying normalization to nested sessions
     * (which may come back as an array of plain objects).
     */
    private normalizeTraining(raw: any): Training {
        if (!raw) {
            return raw;
        }
        const training: Training = { ...raw } as Training;
        if (Array.isArray(training.sessions)) {
            training.sessions = training.sessions.map(s => this.normalizeSession(s));
        }
        return training;
    }
}
