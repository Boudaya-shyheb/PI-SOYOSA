import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { loadStripe, Stripe } from '@stripe/stripe-js';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'root'
})
export class TrainingPaymentService {
    private apiUrl = 'http://localhost:8082/api/payment';

    // Public key provided by user
    private stripePromise = loadStripe('pk_test_51T6z0cB6s4Gt18t0rvSP1pjjlmiksQ4o6n43fnHz7etD8F4Ca6Sh2MbpklPd6BTwfwDLGvjpRRSXJNCu0FI8U6Rx00vKBF6lfP');

    constructor(private http: HttpClient, private authService: AuthService) { }

    async getStripe(): Promise<Stripe | null> {
        return this.stripePromise;
    }

    createPaymentIntent(trainingId: number): Observable<{ clientSecret: string }> {
        // Only students can create payment intents
        if (!this.isStudent()) {
            return throwError(() => new Error('Unauthorized: Only students can create payments'));
        }
        return this.http.post<{ clientSecret: string }>(`${this.apiUrl}/create-payment-intent/${trainingId}`, {});
    }

    // ========== ROLE-BASED ACCESS CONTROL ==========
    private isStudent(): boolean {
        const role = this.authService.getRole();
        return role === 'STUDENT' || role === 'USER';
    }
}
