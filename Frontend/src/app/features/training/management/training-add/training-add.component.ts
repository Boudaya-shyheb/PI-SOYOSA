import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { TrainingService } from '../../../../services/training.service';
import { LEVELS, LEVEL_DESCRIPTIONS, SESSION_TYPES } from '../../../../models/training.model';
import { CustomValidators } from '../../../../utils/validators';

@Component({
    selector: 'app-training-add',
    templateUrl: './training-add.component.html',
    styleUrls: ['./training-add.component.css']
})
export class TrainingAddComponent {

    trainingForm: FormGroup;
    levels = LEVELS;
    levelDescriptions = LEVEL_DESCRIPTIONS;
    sessionTypes = SESSION_TYPES;
    submitting = false;
    uploading = false;
    errorMessage = '';

    constructor(
        private fb: FormBuilder,
        private trainingService: TrainingService,
        public router: Router
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
        });
    }

    onFileSelected(event: any): void {
        const file = event.target.files[0];
        if (file) {
            // Check file size (5MB limit)
            const maxSize = 5 * 1024 * 1024;
            if (file.size > maxSize) {
                this.trainingForm.get('imageUrl')?.setErrors({ maxSize: true });
                this.trainingForm.patchValue({ imageUrl: '' });
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
                    this.errorMessage = 'Failed to upload image to Cloudinary. Please try again.';
                    console.error('Upload error:', err);
                }
            });
        }
    }

    get f() { return this.trainingForm.controls; }

    onLocationSelected(location: {lat: number, lng: number}): void {
        this.trainingForm.patchValue({
            latitude: location.lat,
            longitude: location.lng
        });
        // Mark location as touched to update validation visuals if needed
        this.trainingForm.get('location')?.markAsTouched();
    }

    onSubmit(): void {
        if (this.trainingForm.invalid) {
            Object.keys(this.trainingForm.controls).forEach(key => {
                this.trainingForm.get(key)?.markAsTouched();
            });
            return;
        }

        this.submitting = true;
        this.trainingService.createTraining(this.trainingForm.value).subscribe({
            next: () => {
                this.submitting = false;
                this.router.navigate(['/trainings']);
            },
            error: (error) => {
                this.submitting = false;
                this.errorMessage = 'Error creating training. Please try again.';
                console.error('Error:', error);
            }
        });
    }

    cancel(): void {
        this.router.navigate(['/trainings']);
    }
}
