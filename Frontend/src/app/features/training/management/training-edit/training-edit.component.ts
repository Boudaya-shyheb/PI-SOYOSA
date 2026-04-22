import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { TrainingService } from '../../../../services/training.service';
import { Training, LEVELS, LEVEL_DESCRIPTIONS, SESSION_TYPES } from '../../../../models/training.model';
import { CustomValidators } from '../../../../utils/validators';

@Component({
    selector: 'app-training-edit',
    templateUrl: './training-edit.component.html',
    styleUrls: ['./training-edit.component.css']
})
export class TrainingEditComponent implements OnInit {

    trainingForm: FormGroup;
    levels = LEVELS;
    levelDescriptions = LEVEL_DESCRIPTIONS;
    sessionTypes = SESSION_TYPES;
    submitting = false;
    loading = true;
    errorMessage = '';
    uploading = false;
    trainingId!: number;

    constructor(
        private fb: FormBuilder,
        private trainingService: TrainingService,
        private route: ActivatedRoute,
        private router: Router
    ) {
        this.trainingForm = this.fb.group({
            title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
            description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
            level: ['A1', Validators.required],
            price: [0, [Validators.required, Validators.min(0), Validators.max(10000)]],
            imageUrl: [''],
            type: ['ONLINE', Validators.required],
            meetingLink: ['', [CustomValidators.url()]],
            location: [''],
            room: [''],
            latitude: [null],
            longitude: [null]
        });

        // Add conditional validation based on type
        this.trainingForm.get('type')?.valueChanges.subscribe(type => {
            this.updateConditionalValidators(type);
        });
    }

    private updateConditionalValidators(type: string): void {
        const meetingLink = this.trainingForm.get('meetingLink');
        const location = this.trainingForm.get('location');
        const room = this.trainingForm.get('room');

        if (type === 'ONLINE') {
            meetingLink?.setValidators([Validators.required, CustomValidators.url()]);
            location?.clearValidators();
            room?.clearValidators();
        } else {
            meetingLink?.clearValidators();
            location?.setValidators([Validators.required]);
            room?.setValidators([Validators.required]);
        }
        meetingLink?.updateValueAndValidity();
        location?.updateValueAndValidity();
        room?.updateValueAndValidity();
    }

    onFileSelected(event: any): void {
        const file = event.target.files[0];
        if (file) {
            // Check file size (5MB limit)
            const maxSize = 5 * 1024 * 1024;
            if (file.size > maxSize) {
                this.trainingForm.get('imageUrl')?.setErrors({ maxSize: true });
                return;
            }

            this.uploading = true;
            this.trainingService.uploadImage(file).subscribe({
                next: (res) => {
                    this.trainingForm.patchValue({
                        imageUrl: res.url
                    });
                    this.uploading = false;
                    this.trainingForm.get('imageUrl')?.setErrors(null);
                },
                error: (err) => {
                    this.uploading = false;
                    this.errorMessage = 'Failed to upload image back to Cloudinary. Please try again.';
                    console.error('Upload error:', err);
                }
            });
        }
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

    get f() { return this.trainingForm.controls; }

    loadTraining(): void {
        this.loading = true;
        this.trainingService.getTrainingById(this.trainingId).subscribe({
            next: (training) => {
                this.trainingForm.patchValue({
                    title: training.title,
                    description: training.description,
                    level: training.level,
                    price: training.price,
                    imageUrl: training.imageUrl || '',
                    type: training.type || 'ONLINE',
                    meetingLink: training.meetingLink || '',
                    location: training.location || '',
                    room: training.room || '',
                    latitude: training.latitude || null,
                    longitude: training.longitude || null
                });
                this.updateConditionalValidators(training.type || 'ONLINE');
                this.loading = false;
            },
            error: (error) => {
                this.errorMessage = 'Error loading training. Please try again.';
                this.loading = false;
                console.error('Error:', error);
            }
        });
    }

    onSubmit(): void {
        if (this.trainingForm.invalid) {
            Object.keys(this.trainingForm.controls).forEach(key => {
                this.trainingForm.get(key)?.markAsTouched();
            });
            return;
        }

        this.submitting = true;
        this.trainingService.updateTraining(this.trainingId, this.trainingForm.value).subscribe({
            next: () => {
                this.submitting = false;
                this.router.navigate(['/trainings']);
            },
            error: (error) => {
                this.submitting = false;
                this.errorMessage = 'Error updating training. Please try again.';
                console.error('Error:', error);
            }
        });
    }

    cancel(): void {
        this.router.navigate(['/trainings']);
    }
}
