import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubActivityService } from '../../../../../services/club-activity.service';
import { ClubActivity } from '../../../../../models/activity.model';
import { AuthService } from '../../../../../services/auth.service';

@Component({
  selector: 'app-activity-list',
  templateUrl: './activity-list.component.html',
  styleUrls: ['./activity-list.component.css']
})
export class ActivityListComponent implements OnInit {

  activities: ClubActivity[] = [];
  clubId!: number;
  loading = false;
  errorMessage = '';

  constructor(
    private activityService: ClubActivityService,
    private route: ActivatedRoute,
    private router: Router,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    const clubParam = this.route.snapshot.paramMap.get('clubId');
    if (clubParam) {
      this.clubId = +clubParam;
      this.loadActivities();
    }
  }

  loadActivities(): void {
    this.loading = true;

    this.activityService.getByClubId(this.clubId).subscribe({
      next: (data: ClubActivity[]) => {
        this.activities = data;
        this.loading = false;
      },
      error: (err: any) => {
        this.errorMessage = 'Error loading activities';
        console.error(err);
        this.loading = false;
      }
    });
  }

  createActivity(): void {
    this.router.navigate([`/clubs/${this.clubId}/activities/create`]);
  }

  editActivity(id: number): void {
    this.router.navigate([`/clubs/${this.clubId}/activities/edit/${id}`]);
  }

  deleteActivity(id: number): void {
    if (confirm('Are you sure you want to delete this activity?')) {
      this.activityService.delete(id).subscribe({
        next: () => this.loadActivities(),
        error: (err: any) => console.error('Delete error', err)
      });
    }
  }
}
