import { Component, OnInit } from '@angular/core';
import { Observable, combineLatest, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ClubService } from '../../services/club.service';
import { ClubMemberService } from '../../services/club-member.service';
import { Club } from '../../models/club.model';
import { AuthService } from '../../services/auth.service';
import { ClubJoinRequest, ClubJoinRequestStatus, ClubMember } from '../../models/member.model';
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas';

type ClubCardViewModel = {
  club: Club;
  memberCount: number;
  fillPercent: number;
  alreadyJoined: boolean;
  joinRequestStatus: ClubJoinRequestStatus | null;
};

type QuizQuestion = {
  id: number;
  text: string;
  options: string[];
};

@Component({
  selector: 'app-clubs',
  templateUrl: './clubs.component.html',
  styleUrls: ['./clubs.component.css']
})
export class ClubsComponent implements OnInit {
  clubs$!: Observable<ClubCardViewModel[]>;
  joiningClubId: number | null = null;
  joinErrorMessage = '';
  joinSuccessMessage = '';

  quizQuestions: QuizQuestion[] = [];
  quizQuestionIds: number[] = [];
  quizAnswers: number[] = [];
  currentQuestionIndex = 0;
  showQuizModal = false;
  quizResult = '';
  passingScore = 7;
  passingScoreInput = 7;
  passingScoreLoading = false;

  cardClub: any = null;
  currentYear = new Date().getFullYear();
  generatedCardId = Math.floor(1000 + Math.random() * 9000);

  currentPage = 1;
  itemsPerPage = 3;

  constructor(
    private clubService: ClubService,
    private clubMemberService: ClubMemberService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadClubsWithProgress();
    if (this.auth.isAdmin()) {
      this.loadQuizPassingScore();
    }
  }

  deleteClub(id?: number): void {
    if (!id) {
      return;
    }
    if (!confirm('Are you sure you want to delete this club?')) {
      return;
    }
    this.clubService.delete(id).subscribe({
      next: () => {
        this.loadClubsWithProgress();
      },
      error: () => {
        this.loadClubsWithProgress();
      }
    });
  }

  openJoinQuiz(clubId?: number): void {
    if (!clubId || this.joiningClubId !== null) {
      return;
    }
    this.joinErrorMessage = '';
    this.joinSuccessMessage = '';
    this.quizResult = '';
    this.joiningClubId = clubId;

    this.clubMemberService.getJoinQuiz(clubId).subscribe({
      next: (questions) => {
        this.quizQuestions = questions;
        this.quizQuestionIds = questions.map((question) => question.id);
        this.quizAnswers = new Array(questions.length).fill(-1);
        this.currentQuestionIndex = 0;
        this.showQuizModal = true;
      },
      error: () => {
        this.joiningClubId = null;
        this.joinErrorMessage = 'Unable to load the join quiz for this club.';
      }
    });
  }

  selectQuizAnswer(index: number) {
    this.quizAnswers[this.currentQuestionIndex] = index;
    this.quizResult = '';
  }

  nextQuizQuestion() {
    if (!this.isCurrentQuestionAnswered()) {
      this.quizResult = 'Please answer this question before going to the next one.';
      return;
    }
    if (this.currentQuestionIndex < this.quizQuestions.length - 1) {
      this.currentQuestionIndex++;
      this.quizResult = '';
    }
  }

  previousQuizQuestion() {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
    }
  }

  submitJoinQuiz() {
    const firstUnansweredIndex = this.quizAnswers.findIndex((answer) => answer === -1);
    if (firstUnansweredIndex !== -1) {
      this.currentQuestionIndex = firstUnansweredIndex;
      this.quizResult = 'Please answer all questions.';
      return;
    }
    const email = this.auth.getEmail();
    this.clubMemberService.evaluateJoinQuiz(this.joiningClubId!, this.quizAnswers, this.quizQuestionIds, email).subscribe({
      next: (res) => {
        this.showQuizModal = false;
        const clubIdToJoin = this.joiningClubId;
        this.joiningClubId = null;
        if (clubIdToJoin) {
          this.clubService.getById(clubIdToJoin).subscribe(club => this.downloadMembershipCard(club));
        }
        this.joinSuccessMessage = res.message;
        this.loadClubsWithProgress();
      },
      error: (err) => {
        this.showQuizModal = false;
        this.joiningClubId = null;
        this.joinErrorMessage = err?.error?.message ?? 'You failed the quiz.';
        this.loadClubsWithProgress();
      }
    });
  }

  closeQuizModal() {
    this.showQuizModal = false;
    this.joiningClubId = null;
    this.quizQuestions = [];
    this.quizQuestionIds = [];
    this.quizAnswers = [];
    this.currentQuestionIndex = 0;
  }

  isCurrentQuestionAnswered(): boolean {
    return this.quizAnswers[this.currentQuestionIndex] !== -1;
  }

  getPaginated(clubs: ClubCardViewModel[]): ClubCardViewModel[] {
    const totalPages = Math.ceil(clubs.length / this.itemsPerPage);
    const safePage = Math.min(this.currentPage, Math.max(1, totalPages));
    const start = (safePage - 1) * this.itemsPerPage;
    return clubs.slice(start, start + this.itemsPerPage);
  }

  getPageNumbers(totalItems: number): number[] {
    const totalPages = Math.ceil(totalItems / this.itemsPerPage);
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  }

  nextPage(totalItems: number): void {
    const totalPages = Math.ceil(totalItems / this.itemsPerPage);
    if (this.currentPage < totalPages) {
      this.currentPage++;
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  goToPage(page: number): void {
    this.currentPage = page;
  }

  savePassingScore(): void {
    if (this.passingScoreLoading) {
      return;
    }
    this.joinErrorMessage = '';
    this.joinSuccessMessage = '';
    const score = Number(this.passingScoreInput);
    this.passingScoreLoading = true;
    this.clubMemberService.updateQuizPassingScore(score).subscribe({
      next: (response) => {
        this.passingScoreLoading = false;
        this.passingScore = response.passingScore;
        this.passingScoreInput = response.passingScore;
        this.joinSuccessMessage = `Passing score updated to ${response.passingScore}/10.`;
      },
      error: (err) => {
        this.passingScoreLoading = false;
        this.joinErrorMessage = err?.error?.message ?? 'Unable to update passing score.';
      }
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

  private loadClubsWithProgress(): void {
    const clubs$ = this.clubService.getAll().pipe(
      catchError(() => {
        this.joinErrorMessage = 'Impossible de charger les clubs.';
        return of([]);
      })
    );

    const members$ = this.clubMemberService.getAll().pipe(
      catchError(() => of([]))
    );

    const requests$ = this.auth.isClient()
      ? this.clubMemberService.getMyJoinRequests().pipe(catchError(() => of([])))
      : of([]);

    this.clubs$ = combineLatest([
      clubs$,
      members$,
      requests$
    ]).pipe(
      map(([clubs, members, requests]) => {
        const currentEmail = this.auth.getEmail().toLowerCase();
        const countsByClub = new Map<number, number>();
        const joinedClubIds = new Set<number>();
        const latestRequestStatusByClub = new Map<number, ClubJoinRequestStatus>();

        (requests as ClubJoinRequest[]).forEach((request) => {
          if (!request.clubId) {
            return;
          }
          if (!latestRequestStatusByClub.has(request.clubId)) {
            latestRequestStatusByClub.set(request.clubId, request.status);
          }
        });

        (members as ClubMember[]).forEach((member) => {
          if (!member.clubId) {
            return;
          }
          const currentCount = countsByClub.get(member.clubId) ?? 0;
          countsByClub.set(member.clubId, currentCount + 1);
          if (member.email?.toLowerCase() === currentEmail) {
            joinedClubIds.add(member.clubId);
          }
        });

        return clubs.map((club) => {
          const clubId = club.id ?? 0;
          const memberCount = countsByClub.get(clubId) ?? 0;
          const maxMembers = club.maxMembers ?? 0;
          const fillPercent = maxMembers > 0
            ? Math.min(100, Math.round((memberCount / maxMembers) * 100))
            : 0;

          return {
            club,
            memberCount,
            fillPercent,
            alreadyJoined: joinedClubIds.has(clubId),
            joinRequestStatus: latestRequestStatusByClub.get(clubId) ?? null
          };
        });
      })
    );
  }

  private loadQuizPassingScore(): void {
    this.clubMemberService.getQuizPassingScore().subscribe({
      next: (response) => {
        this.passingScore = response.passingScore;
        this.passingScoreInput = response.passingScore;
      },
      error: () => {}
    });
  }
}
