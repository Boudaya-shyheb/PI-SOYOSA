import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ClubService } from '../../../../services/club.service';
import { ClubActivityService } from '../../../../services/club-activity.service';
import { Club } from '../../../../models/club.model';
import { ClubActivity } from '../../../../models/activity.model';
import { AuthService } from '../../../../services/auth.service';
import { ClubMemberService } from '../../../../services/club-member.service';
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas';

@Component({
  selector: 'app-club-details',
  templateUrl: './club-details.component.html'
})
export class ClubDetailsComponent implements OnInit {
  club: Club | null = null;
  activities: ClubActivity[] = [];
  loading = false;
  activitiesLoading = false;
  errorMessage = '';
  alreadyJoined = false;
  cardClub: any = null;
  currentYear = new Date().getFullYear();
  generatedCardId = Math.floor(1000 + Math.random() * 9000);

  constructor(
    private clubService: ClubService,
    private activityService: ClubActivityService,
    private route: ActivatedRoute,
    private router: Router,
    public auth: AuthService,
    private clubMemberService: ClubMemberService
  ) { }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.errorMessage = 'Club not found';
      return;
    }

    const clubId = Number(idParam);
    if (Number.isNaN(clubId)) {
      this.errorMessage = 'Club not found';
      return;
    }

    this.loading = true;
    this.clubService.getById(clubId).subscribe({
      next: (club) => {
        this.club = club;
        this.loading = false;
        this.loadActivities(clubId);
        this.checkMembership(clubId);
      },
      error: () => {
        this.errorMessage = 'Error loading club';
        this.loading = false;
      }
    });
  }

  private loadActivities(clubId: number): void {
    this.activitiesLoading = true;
    this.activityService.getByClubId(clubId).subscribe({
      next: (activities) => {
        this.activities = activities;
        this.activitiesLoading = false;
      },
      error: () => {
        this.activitiesLoading = false;
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/clubs']);
  }

  addActivity(): void {
    this.router.navigate(['/clubs', this.club?.id, 'activities', 'create']);
  }

  private checkMembership(clubId: number): void {
    const currentEmail = this.auth.getEmail().toLowerCase();
    this.clubMemberService.getAll().subscribe(members => {
      this.alreadyJoined = members.some(m => m.clubId === clubId && m.email?.toLowerCase() === currentEmail);
    });
  }

  async downloadMembershipCard(club: any) {
    this.cardClub = club;
    this.generatedCardId = Math.floor(1000 + Math.random() * 9000);
    
    // Wait for view update
    setTimeout(async () => {
      const element = document.getElementById('membershipCardCanvas');
      if (element) {
        try {
          const canvas = await html2canvas(element, { scale: 3, useCORS: true, backgroundColor: null });
          const imgData = canvas.toDataURL('image/png');
          
          const pdf = new jsPDF('landscape', 'mm', [85.6, 53.98]); // CR-80 card standard
          pdf.addImage(imgData, 'PNG', 0, 0, 85.6, 53.98);
          let safeName = club.name.replace(/\s+/g, '_');
          pdf.save(`Carte_Membre_${safeName}.pdf`);
        } catch (error) {
          console.error('Erreur durant la création du PDF:', error);
        }
      }
    }, 100);
  }
}
