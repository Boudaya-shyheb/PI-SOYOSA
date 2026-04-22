import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TrainingService } from '../../../../services/training.service';
import { SESSION_TYPES, STATUSES, Training } from '../../../../models/training.model';
import { CustomValidators } from '../../../../utils/validators';

@Component({
    selector: 'app-session-add',
    templateUrl: './session-add.component.html',
    styleUrls: ['./session-add.component.css']
})
export class SessionAddComponent implements OnInit {

    sessionForm: FormGroup;
    sessionTypes = SESSION_TYPES;
    statuses = STATUSES;
    submitting = false;
    loading = true;
    errorMessage = '';
    trainingId!: number;
    training!: Training;

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
            maxParticipants: [20, [Validators.required, Validators.min(1), Validators.max(100)]]
        });
    }

    ngOnInit(): void {
        this.trainingId = Number(this.route.snapshot.paramMap.get('id'));
        if (this.trainingId) {
            this.loadTraining();
        } else {
            this.errorMessage = 'Training ID not found';
            this.loading = false;
        }
    }

    get f() { return this.sessionForm.controls; }

    loadTraining(): void {
        this.loading = true;
        this.trainingService.getTrainingById(this.trainingId).subscribe({
            next: (training: Training) => {
                this.training = training;
                this.loading = false;
            },
            error: (error: any) => {
                this.errorMessage = 'Error loading training. Please try again.';
                this.loading = false;
                console.error('Error:', error);
            }
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

        // Get form values
        const formValue = this.sessionForm.value;

        // Format time to include seconds
        let timeString = formValue.startTime;
        if (timeString && timeString.length === 5) {
            timeString = timeString + ':00';
        }

        // Create session data
        const sessionData: any = {
            date: formValue.date,
            startTime: timeString,
            duration: formValue.duration,
            maxParticipants: formValue.maxParticipants
        };

        // LOG THE EXACT DATA BEING SENT
        console.log('=== SESSION DATA BEING SENT ===');
        console.log('PAYLOAD:', JSON.stringify(sessionData, null, 2));

        this.trainingService.addSessionToTraining(this.trainingId, sessionData).subscribe({
            next: (response: any) => {
                console.log('SUCCESS:', response);
                this.submitting = false;
                this.router.navigate(['/trainings', this.trainingId]);
            },
            error: (error: any) => {
                console.error('=== ERROR DETAILS ===');
                this.submitting = false;
                this.errorMessage = 'Error adding session. Please try again.';
            }
        });
    }
    cancel(): void {
        this.router.navigate(['/trainings', this.trainingId]); // Note: changed from /training to /trainings matching new routes
    }
}
