import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubActivityService } from '../../../../../services/club-activity.service';
import { ClubActivity } from '../../../../../models/activity.model';

@Component({
  selector: 'app-activity-form',
  templateUrl: './activity-form.component.html',
  styleUrls: ['./activity-form.component.css']
})
export class ActivityFormComponent implements OnInit {

  activityForm!: FormGroup;
  clubId!: number;
  activityId?: number;
  isEditMode = false;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private activityService: ClubActivityService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.activityForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      activityDate: ['', Validators.required],
      location: ['', Validators.required]   // 🔥 AJOUT IMPORTANT
    });

    const clubParam = this.route.snapshot.paramMap.get('clubId');
    if (clubParam) {
      this.clubId = +clubParam;
    }

    const actParam = this.route.snapshot.paramMap.get('activityId');
    if (actParam) {
      this.activityId = +actParam;
      this.isEditMode = true;
      this.loadActivity(this.activityId);
    }
  }

  private loadActivity(id: number): void {
    this.loading = true;
    this.activityService.getById(id).subscribe({
      next: (activity: ClubActivity) => {
        this.activityForm.patchValue({
          title: activity.title,
          description: activity.description,
          activityDate: activity.activityDate,
          location: activity.location
        });
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading activity', err);
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.activityForm.invalid) {
      this.activityForm.markAllAsTouched();
      return;
    }

    const data: ClubActivity = {
      ...this.activityForm.value,
      clubId: this.clubId
    };

    this.loading = true;

    if (this.isEditMode && this.activityId) {
      this.activityService.update(this.activityId, data).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate([`/clubs/${this.clubId}/activities`]);
        },
        error: (err: any) => {
          console.error('Update error', err);
          this.loading = false;
        }
      });
    } else {
      this.activityService.create(data).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate([`/clubs/${this.clubId}/activities`]);
        },
        error: (err: any) => {
          console.error('Create error', err);
          this.loading = false;
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate([`/clubs/${this.clubId}/activities`]);
  }
}
