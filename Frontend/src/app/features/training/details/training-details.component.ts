import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TrainingService } from '../../../services/training.service';
import { Training, Session, LEVEL_DESCRIPTIONS, LEVEL_COLORS, Review } from '../../../models/training.model';
import { AuthService } from '../../../services/auth.service';
import { TrainingEnrollmentService, Enrollment } from '../../../services/training-enrollment.service';
import { TrainingPaymentService } from '../../../services/training-payment.service';
import { StripeElements, StripePaymentElement } from '@stripe/stripe-js';

@Component({
    selector: 'app-training-details',
    templateUrl: './training-details.component.html',
    styleUrls: ['./training-details.component.css']
})
export class TrainingDetailsComponent implements OnInit {

    training!: Training;
    sessions: Session[] = [];
    levelDescriptions = LEVEL_DESCRIPTIONS;
    levelColors = LEVEL_COLORS;
    loading = true;
    loadingSessions = false;
    errorMessage = '';

    // Session Pagination
    sessionPage = 0;
    sessionSize = 3;
    sessionTotalPages = 0;
    sessionTotalItems = 0;
    sessionStartDate: string = '';
    sessionEndDate: string = '';

    isEnrolledInTraining = false;
    isEligibleToReview = false;
    enrolledSessionId: number | null = null;
    enrolling = false; // loader for enrollment
    intendedSessionId: number | null = null;

    // Stripe Payment Modal state
    showPaymentModal = false;
    paymentProcessing = false;
    stripeElements: StripeElements | null = null;
    paymentElement: StripePaymentElement | null = null;
    clientSecret: string = '';

    // Attendance tracking
    showAttendanceForSessionId: number | null = null;
    sessionAttendees: Enrollment[] = [];
    loadingAttendees = false;
    myEnrollments: Enrollment[] = [];

    // Certificate tracking
    selectedCertificate: Enrollment | null = null;
    showCertificateModal = false;

    // Review tracking
    reviews: Review[] = [];
    loadingReviews = false;
    averageRating = 0;
    reviewForm = {
        rating: 5,
        comment: ''
    };
    hasUserReviewed = false;
    userDisplayName: string | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private trainingService: TrainingService,
        public authService: AuthService,
        private enrollmentService: TrainingEnrollmentService,
        private paymentService: TrainingPaymentService
    ) { }

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        if (id) {
            this.userDisplayName = this.authService.getUserDisplayName();
            this.loadTraining(id);
            this.loadSessions(id);
            this.loadReviews(id);
            if (this.authService.isStudent()) {
                console.log('[DEBUG] User is a student. Checking enrollment...');
                this.checkEnrollmentStatus(id);
                this.fetchStudentSession(id);
            }
        } else {
            this.errorMessage = 'Training ID not found';
            this.loading = false;
        }
    }

    loadTraining(id: number): void {
        this.loading = true;
        this.trainingService.getTrainingById(id).subscribe({
            next: (training) => {
                console.log('[DEBUG] Training data received from API:', training);
                console.log('[DEBUG] Object keys:', Object.keys(training));
                this.training = training;
                this.loading = false;
            },
            error: (error) => {
                this.errorMessage = 'Error loading training details';
                this.loading = false;
                console.error('Error:', error);
            }
        });
    }

    loadSessions(id: number): void {
        this.loadingSessions = true;
        this.trainingService.getSessionsByTraining(id, this.sessionPage, this.sessionSize, this.sessionStartDate, this.sessionEndDate).subscribe({
            next: (response) => {
                console.log('[DEBUG] Sessions loaded:', response);
                this.sessions = response.content;
                this.sessionTotalPages = response.totalPages;
                this.sessionTotalItems = response.totalElements;
                this.loadingSessions = false;
            },
            error: (error) => {
                console.error('Error loading sessions:', error);
                this.loadingSessions = false;
            }
        });
    }

    onDateFilterChange(): void {
        this.sessionPage = 0; // Reset to first page when filtering
        this.loadSessions(this.training.id!);
    }

    clearDateFilter(): void {
        this.sessionStartDate = '';
        this.sessionEndDate = '';
        this.onDateFilterChange();
    }

    onSessionPageChange(page: number): void {
        this.sessionPage = page;
        this.loadSessions(this.training.id!);
    }

    getLevelDescription(level: string): string {
        return LEVEL_DESCRIPTIONS[level] || level;
    }

    getLevelColor(level: string): string {
        switch (level.charAt(0)) {
            case 'A': return 'bg-green-100 text-green-800';
            case 'B': return 'bg-blue-100 text-blue-800';
            case 'C': return 'bg-yellow-100 text-yellow-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    }

    getStatusColor(status: string): string {
        switch (status) {
            case 'PLANNED': return 'bg-yellow-100 text-yellow-800';
            case 'COMPLETED': return 'bg-green-100 text-green-800';
            case 'CANCELLED': return 'bg-red-100 text-red-800';
            default: return 'bg-gray-100 text-gray-800';
        }
    }

    getSessionTypeIcon(type: string | undefined): string {
        return type === 'OFFLINE' ? '📍' : '🌐';
    }

    formatTime(time: string): string {
        if (!time) return '';
        return time.substring(0, 5); // Returns "HH:MM"
    }

    formatDate(date: Date | string): string {
        return new Date(date).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }

    goBack(): void {
        this.router.navigate(['/trainings']);
    }

    editTraining(): void {
        if (this.training && this.training.id) {
            this.router.navigate(['/trainings/management/edit', this.training.id]);
        } else {
            console.error('[ERROR] Cannot navigate to edit: Training ID is missing', this.training);
            alert('Error: Training details not fully loaded.');
        }
    }

    deleteTraining(): void {
        if (this.training && this.training.id) {
            if (confirm(`Are you sure you want to delete the training "${this.training.title}"? This will also delete all associated sessions, enrollments, and reviews.`)) {
                this.trainingService.deleteTraining(this.training.id).subscribe({
                    next: () => {
                        alert('Training deleted successfully!');
                        this.router.navigate(['/trainings']);
                    },
                    error: (error) => {
                        console.error('Error deleting training:', error);
                        alert('Error deleting training. Please try again.');
                    }
                });
            }
        }
    }

    addSession(): void {
        if (this.training && this.training.id) {
            this.router.navigate(['/trainings', this.training.id, 'sessions', 'add']);
        } else {
            console.error('[ERROR] Cannot navigate to add session: Training ID is missing', this.training);
            alert('Error: Training details not fully loaded.');
        }
    }

    editSession(sessionId: number): void {
        if (this.training && this.training.id) {
            this.router.navigate(['/trainings', this.training.id, 'sessions', 'edit', sessionId]);
        } else {
            console.error('[ERROR] Cannot navigate to edit session: Training ID is missing', this.training);
        }
    }

    deleteSession(sessionId: number): void {
        if (confirm('Are you sure you want to delete this session?')) {
            this.trainingService.deleteSession(this.training.id!, sessionId).subscribe({
                next: () => {
                    this.sessions = this.sessions.filter(s => s.id !== sessionId);
                },
                error: (error) => {
                    console.error('Error deleting session:', error);
                    alert('Error deleting session');
                }
            });
        }
    }

    markSessionCompleted(sessionId: number): void {
        if (confirm('Are you sure you want to mark this session as COMPLETED? This action cannot be undone.')) {
            this.trainingService.markSessionCompleted(this.training.id!, sessionId).subscribe({
                next: (updatedSession: Session) => {
                    // Update the session in the local list
                    const index = this.sessions.findIndex(s => s.id === sessionId);
                    if (index !== -1) {
                        this.sessions[index] = updatedSession;
                    }
                    alert('Session marked as completed successfully!');
                },
                error: (error: any) => {
                    console.error('Error marking session as completed:', error);
                    alert('Error marking session as completed');
                }
            });
        }
    }

    checkEnrollmentStatus(trainingId: number): void {
        console.log('[DEBUG] Checking enrollment status for training ID:', trainingId);
        this.enrollmentService.isStudentEnrolledInTraining(trainingId).subscribe({
            next: (response) => {
                console.log('[DEBUG] Enrollment status response:', response);
                this.isEnrolledInTraining = response.enrolled;
                this.isEligibleToReview = response.eligibleToReview;
            },
            error: (err) => console.error('[DEBUG] Error checking status', err)
        });
    }

    fetchStudentSession(trainingId: number): void {
        console.log('[DEBUG] Fetching student session for training ID:', trainingId);
        this.enrollmentService.getMyEnrollments(0, 1000).subscribe({ // Fetch many to find the one for this training
            next: (response) => {
                const enrollments: Enrollment[] = response.content;
                console.log('[DEBUG] My enrollments:', enrollments);
                this.myEnrollments = enrollments;
                const enrollment = enrollments.find((e: Enrollment) => e.training.id === trainingId);
                if (enrollment && enrollment.session) {
                    this.enrolledSessionId = enrollment.session.id;
                    console.log('[DEBUG] Enrolled in session ID:', this.enrolledSessionId);
                } else {
                    console.log('[DEBUG] Not enrolled in any session for this training');
                }
            },
            error: (err) => console.error('[DEBUG] Error fetching student session', err)
        });
    }

    async initiatePayment(sessionId?: number): Promise<void> {
        // Check if user is logged in
        if (!this.authService.isAuthenticated()) {
            // Redirect guest to register
            alert('Please register or login to buy this training');
            this.router.navigate(['/user/register']);
            return;
        }

        // Check if user is a student
        if (!this.authService.isStudent()) {
            alert('Only students can enroll in trainings');
            return;
        }

        if (sessionId) {
            this.intendedSessionId = sessionId;
        } else {
            this.intendedSessionId = null;
        }
        this.enrolling = true;
        this.paymentService.createPaymentIntent(this.training.id!).subscribe({
            next: async (res) => {
                this.clientSecret = res.clientSecret;
                this.showPaymentModal = true;

                const stripe = await this.paymentService.getStripe();
                if (stripe) {
                    this.stripeElements = stripe.elements({ clientSecret: this.clientSecret });
                    this.paymentElement = this.stripeElements.create('payment');

                    // allow DOM to render modal first
                    setTimeout(() => {
                        this.paymentElement?.mount('#payment-element');
                        this.enrolling = false;
                    });
                }
            },
            error: (err) => {
                this.enrolling = false;
                alert(err.error || 'Error initiating payment');
            }
        });
    }

    async confirmPayment(): Promise<void> {
        this.paymentProcessing = true;
        const stripe = await this.paymentService.getStripe();
        if (!stripe || !this.stripeElements) return;

        const { error, paymentIntent } = await stripe.confirmPayment({
            elements: this.stripeElements,
            confirmParams: {
                // Not using automatic redirect
            },
            redirect: 'if_required'
        });

        if (error) {
            alert(error.message);
            this.paymentProcessing = false;
        } else if (paymentIntent && paymentIntent.status === 'succeeded') {
            this.completeEnrollment();
        }
    }

    completeEnrollment(): void {
        console.log('[DEBUG] Calling buyTraining endpoint...');
        this.enrollmentService.buyTraining(this.training.id!).subscribe({
            next: (res) => {
                console.log('[DEBUG] Successfully bought training:', res);
                this.isEnrolledInTraining = true;
                this.showPaymentModal = false;
                this.paymentProcessing = false;

                // Force a refresh of the UI statuses
                this.checkEnrollmentStatus(this.training.id!);
                this.fetchStudentSession(this.training.id!);

                if (this.intendedSessionId) {
                    this.enrollInSession(this.intendedSessionId, true);
                } else {
                    alert('Successfully bought training! Now you can choose a session.');
                }
            },
            error: (err) => {
                console.error('[DEBUG] Error buying training:', err);
                this.paymentProcessing = false;
                alert(err.error || 'Error buying training');
            }
        });
    }

    closePaymentModal(): void {
        this.showPaymentModal = false;
        this.paymentElement?.destroy();
        this.paymentElement = null;
        this.stripeElements = null;
    }

    enrollInSession(sessionId: number, fromPayment: boolean = false): void {
        this.enrolling = true;
        this.enrollmentService.enrollInSession(this.training.id!, sessionId).subscribe({
            next: () => {
                this.enrolledSessionId = sessionId;
                this.enrolling = false;

                if (fromPayment) {
                    alert('Successfully paid and enrolled in the session!');
                } else {
                    alert('Successfully enrolled in session!');
                }

                this.loadSessions(this.training.id!); // refresh spots
            },
            error: (err) => {
                this.enrolling = false;
                const msg = err.error?.message || err.error || 'Error enrolling in session';
                alert(msg);
            }
        });
    }

    // Attendance Methods

    toggleAttendanceView(sessionId: number): void {
        if (this.showAttendanceForSessionId === sessionId) {
            this.showAttendanceForSessionId = null;
            this.sessionAttendees = [];
        } else {
            this.showAttendanceForSessionId = sessionId;
            this.loadSessionAttendees(sessionId);
        }
    }

    loadSessionAttendees(sessionId: number): void {
        this.loadingAttendees = true;
        console.log('[DEBUG] Loading attendees for session:', sessionId);
        this.enrollmentService.getSessionEnrollments(sessionId).subscribe({
            next: (attendees) => {
                console.log('[DEBUG] Attendees received:', attendees);
                this.sessionAttendees = attendees;
                this.loadingAttendees = false;
            },
            error: (err) => {
                console.error('[ERROR] Error loading attendees', err);
                this.loadingAttendees = false;
            }
        });
    }

    togglePresence(enrollment: Enrollment, event: any): void {
        const isPresent = event.target.checked;
        console.log('[DEBUG] Toggling presence for enrollment:', enrollment.id, 'to:', isPresent);
        this.enrollmentService.updatePresence(enrollment.id!, isPresent).subscribe({
            next: (updated) => {
                console.log('[DEBUG] Presence updated successfully:', updated);
                enrollment.present = updated.present;
            },
            error: (err) => {
                console.error('[ERROR] Error updating presence', err);
                alert('Failed to update attendance status');
                // Revert UI if error
                event.target.checked = !isPresent;
            }
        });
    }

    getStudentPresenceStatus(sessionId: number): string | null {
        if (this.authService.isStudent() && this.enrolledSessionId === sessionId) {
            // Find enrollment for this session
            const enrollment = this.myEnrollments.find(e => e.session?.id === sessionId);
            if (enrollment) {
                return enrollment.present ? 'Present' : 'Awaiting';
            }
            // Fallback: check if we have the enrolledSessionId but haven't loaded myEnrollments fully yet
            if (this.enrolledSessionId === sessionId) return 'Awaiting';
        }
        return null;
    }

    issueCertificate(attendee: Enrollment): void {
        if (!attendee.present) {
            alert('Student must be marked as present before issuing a certificate.');
            return;
        }

        if (confirm(`Are you sure you want to issue a certificate to ${attendee.student.name}?`)) {
            this.enrollmentService.issueCertificate(attendee.id!).subscribe({
                next: (updated) => {
                    attendee.certificateIssued = true;
                    attendee.certificateIssuedDate = updated.certificateIssuedDate;
                },
                error: (err) => {
                    console.error('Error issuing certificate', err);
                    alert('Failed to issue certificate');
                }
            });
        }
    }

    viewCertificate(attendee: any): void {
        this.selectedCertificate = attendee;
        this.showCertificateModal = true;
    }

    viewMyCertificate(sessionId: number): void {
        const enrollment = this.myEnrollments.find(e => e.session?.id === sessionId);
        if (enrollment && enrollment.certificateIssued) {
            this.viewCertificate(enrollment);
        }
    }

    closeCertificateModal(): void {
        this.showCertificateModal = false;
        this.selectedCertificate = null;
    }

    printCertificate(): void {
        window.print();
    }

    // REVIEW METHODS

    loadReviews(trainingId: number): void {
        this.loadingReviews = true;
        this.trainingService.getReviewsForTraining(trainingId).subscribe({
            next: (reviews: Review[]) => {
                this.reviews = reviews;
                this.calculateAverageRating();
                this.checkIfUserReviewed();
                this.loadingReviews = false;
            },
            error: (err) => {
                console.error('Error loading reviews', err);
                this.loadingReviews = false;
            }
        });
    }

    calculateAverageRating(): void {
        if (this.reviews.length === 0) {
            this.averageRating = 0;
            return;
        }
        const sum = this.reviews.reduce((acc, r) => acc + r.rating, 0);
        this.averageRating = sum / this.reviews.length;
    }

    checkIfUserReviewed(): void {
        if (this.authService.isStudent()) {
            const userId = this.authService.getUserId();
            if (userId) {
                this.hasUserReviewed = this.reviews.some(r => String(r.studentId) === String(userId));
            }
        }
    }

    submitReview(): void {
        if (this.reviewForm.rating < 1 || this.reviewForm.rating > 5) {
            alert('Please select a rating between 1 and 5');
            return;
        }

        this.trainingService.addReview(this.training.id!, {
            rating: this.reviewForm.rating,
            comment: this.reviewForm.comment
        }).subscribe({
            next: () => {
                alert('Thank you for your review!');
                this.reviewForm = { rating: 5, comment: '' };
                this.loadReviews(this.training.id!);
            },
            error: (err: any) => {
                console.error('Error submitting review', err);
                alert(err.error?.message || 'Error submitting review');
            }
        });
    }

    getStarArray(rating: number): number[] {
        return Array(Math.round(rating)).fill(0);
    }

    getEmptyStarArray(rating: number): number[] {
        return Array(5 - Math.round(rating)).fill(0);
    }
}
