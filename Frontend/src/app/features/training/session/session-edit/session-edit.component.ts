import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TrainingService } from '../../../../services/training.service';
import { SESSION_TYPES, STATUSES, Session } from '../../../../models/training.model';
import { CustomValidators } from '../../../../utils/validators';

@Component({
    selector: 'app-session-edit',
    templateUrl: './session-edit.component.html',
    styleUrls: ['./session-edit.component.css']
})
export class SessionEditComponent implements OnInit {

    sessionForm: FormGroup;
    sessionTypes = SESSION_TYPES;
    statuses = STATUSES;
    submitting = false;
    loading = true;
    errorMessage = '';
    trainingId!: number;
    sessionId!: number;
    session!: Session;

    constructor(
        private fb: FormBuilder,
        private trainingService: TrainingService,
        private route: ActivatedRoute,
        private router: Router
    ) {
        this.sessionForm = this.fb.group({
            date: ['', [Validators.required, CustomValidators.futureDate()]],
            startTime: ['', Validators.required],
            duration: [60, [Validators.required, Validators.min(1)]],
            maxParticipants: ['', [Validators.required, Validators.min(1), Validators.max(100)]]
        });
    }

    ngOnInit(): void {
        this.trainingId = Number(this.route.snapshot.paramMap.get('id'));
        this.sessionId = Number(this.route.snapshot.paramMap.get('sessionId'));

        if (this.trainingId && this.sessionId) {
            this.loadSession();
        } else {
            this.errorMessage = 'Session ID not found';
            this.loading = false;
        }
    }

    get f() { return this.sessionForm.controls; }

    loadSession(): void {
        this.loading = true;
        // We fetch a larger page to ensure we find the session being edited
        this.trainingService.getSessionsByTraining(this.trainingId, 0, 100).subscribe({
            next: (response) => {
                const sessions: Session[] = response.content;
                const session = sessions.find((s: Session) => s.id === this.sessionId);
                if (session) {
                    this.session = session;
                    this.populateForm(session);
                } else {
                    this.errorMessage = 'Session not found';
                }
                this.loading = false;
            },
            error: (error) => {
                this.errorMessage = 'Error loading session. Please try again.';
                this.loading = false;
                console.error('Error:', error);
            }
        });
    }

    populateForm(session: Session): void {
        const dateValue = new Date(session.date).toISOString().split('T')[0];
        const timeValue = session.startTime.substring(0, 5);

        this.sessionForm.patchValue({
            date: dateValue,
            startTime: timeValue,
            duration: session.duration,
            maxParticipants: session.maxParticipants
        });
    }


    onSubmit(): void {
        if (this.sessionForm.invalid) {
            Object.keys(this.sessionForm.controls).forEach(key => {
                this.sessionForm.get(key)?.markAsTouched();
            });
            return;
        }

        this.submitting = true;
        const formValue = this.sessionForm.value;

        let timeString = formValue.startTime;
        if (timeString && timeString.length === 5) {
            timeString = timeString + ':00';
        }

        const sessionData: any = {
            date: formValue.date,
            startTime: timeString,
            duration: Number(formValue.duration),
            maxParticipants: Number(formValue.maxParticipants)
        };

        this.trainingService.updateSession(this.trainingId, this.sessionId, sessionData).subscribe({
            next: (response) => {
                this.submitting = false;
                this.router.navigate(['/trainings', this.trainingId]); // Changed routing prefix to match Frontend layout
            },
            error: (error) => {
                this.submitting = false;
                this.errorMessage = 'Error updating session. Please try again.';
            }
        });
    }

    cancel(): void {
        this.router.navigate(['/trainings', this.trainingId]); // Changed routing prefix to match Frontend layout
    }
}
