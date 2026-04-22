import { Component, OnInit } from '@angular/core';
import { TrainingService } from '../../services/training.service';
import { AuthService } from '../../services/auth.service';
import { Training, LEVEL_COLORS } from '../../models/training.model';

@Component({
    selector: 'app-training-list',
    templateUrl: './training-list.component.html',
    styleUrls: ['./training-list.component.css']
})
export class TrainingListComponent implements OnInit {
    trainings: Training[] = [];
    loading = true;
    errorMessage = '';
    levelColors = LEVEL_COLORS;
    searchTerm = '';

    // Recommendations
    recommendedTrainings: Training[] = [];
    showPlacementTest = false;
    userLevel: string | null = null;

    // Pagination
    currentPage = 0;
    pageSize = 3;
    totalPages = 0;
    totalElements = 0;

    constructor(
        private trainingService: TrainingService,
        public auth: AuthService
    ) { }

    ngOnInit(): void {
        this.loadTrainings();
        this.checkPlacementAndRecommendations();
    }

    checkPlacementAndRecommendations(): void {
        if (!this.auth.isLoggedIn()) return;

        this.trainingService.getPlacementResult().subscribe(result => {
            if (result) {
                this.userLevel = result.determinedLevel;
                this.loadRecommendations();
            } else if (this.auth.isStudent()) {
                // For new students, show the placement test option
                this.showPlacementTest = false; // We'll show a banner first
            }
        });
    }

    loadRecommendations(): void {
        this.trainingService.getRecommendations().subscribe(data => {
            this.recommendedTrainings = data;
        });
    }

    onTestCompleted(result: any): void {
        this.userLevel = result.determinedLevel;
        this.showPlacementTest = false;
        this.loadRecommendations();
    }

    startTest(): void {
        this.showPlacementTest = true;
    }

    loadTrainings(): void {
        this.loading = true;
        this.trainingService.getAllTrainings(this.currentPage, this.pageSize, this.searchTerm).subscribe({
            next: (data) => {
                console.log('[DEBUG] Trainings loaded:', data);
                this.trainings = data.content;
                this.totalPages = data.totalPages;
                this.totalElements = data.totalElements;
                this.loading = false;
            },
            error: (err) => {
                this.errorMessage = 'Error loading trainings';
                this.loading = false;
            }
        });
    }

    onSearch(): void {
        this.currentPage = 0;
        this.loadTrainings();
    }

    changePage(page: number): void {
        this.currentPage = page;
        this.loadTrainings();
        window.scrollTo(0, 0);
    }

    getLevelColor(level: string): string {
        return this.levelColors[level] || 'bg-gray-100 text-gray-800';
    }
}
