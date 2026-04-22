import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ClubService } from '../../../../services/club.service';
import { Club } from '../../../../models/club.model';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-club-list',
  templateUrl: './club-list.component.html'
})
export class ClubListComponent implements OnInit {

  clubs: Club[] = [];
  loading = false;
  errorMessage = '';

  constructor(
    private clubService: ClubService,
    private router: Router,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadClubs();
  }

  loadClubs(): void {
    this.loading = true;
    this.clubService.getAll().subscribe({
      next: (data) => {
        this.clubs = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Error loading clubs';
        console.error(err);
        this.loading = false;
      }
    });
  }

  createClub(): void {
    this.router.navigate(['/clubs/create']);
  }

  editClub(id: number): void {
    this.router.navigate(['/clubs/edit', id]);
  }

  deleteClub(id: number): void {
    if (confirm('Are you sure you want to delete this club?')) {
      this.clubService.delete(id).subscribe({
        next: () => this.loadClubs(),
        error: (err) => console.error('Delete error', err)
      });
    }
  }
}
