import { Component, OnInit } from '@angular/core';
import { TrainingEnrollmentService, Enrollment } from '../../../../services/training-enrollment.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-my-enrollments',
    templateUrl: './my-enrollments.component.html',
    styleUrls: ['./my-enrollments.component.css']
})
export class MyEnrollmentsComponent implements OnInit {

    enrollments: Enrollment[] = [];
    loading = true;
    errorMessage = '';

    // Pagination
    page = 0;
    size = 5;
    totalPages = 0;
    totalElements = 0;

    constructor(
        private enrollmentService: TrainingEnrollmentService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.loadMyEnrollments();
    }

    loadMyEnrollments(): void {
        this.loading = true;
        this.enrollmentService.getMyEnrollments(this.page, this.size).subscribe({
            next: (response) => {
                this.enrollments = response.content;
                this.totalPages = response.totalPages;
                this.totalElements = response.totalElements;
                this.loading = false;
            },
            error: (err) => {
                this.errorMessage = 'Failed to load your enrollments.';
                this.loading = false;
                console.error(err);
            }
        });
    }

    onPageChange(page: number): void {
        this.page = page;
        this.loadMyEnrollments();
    }

    viewTraining(trainingId: number): void {
        this.router.navigate(['/trainings', trainingId]);
    }
}
