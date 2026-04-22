import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventParticipationService } from '../../../services/event-participation.service';
import { EventParticipation } from '../../../models/event-participation.model';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-event-participant-details',
  templateUrl: './event-participant-details.component.html',
  styleUrls: ['./event-participant-details.component.css']
})
export class EventParticipantDetailsComponent implements OnInit {

  eventId?: number;
  participants: EventParticipation[] = [];
  pendingRequests: EventParticipation[] = [];
  loading = false;
  loadingPending = false;
  canManageRequests = false;
  errorMessage = '';
  requestMessage = '';
  processingRequestId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private participationService: EventParticipationService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {

    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam || isNaN(Number(idParam))) {
      this.errorMessage = 'Invalid event ID';
      return;
    }

    const id = Number(idParam);
    this.eventId = id;
    this.canManageRequests = this.auth.isAdmin();
    this.loadParticipants();
    if (this.canManageRequests) {
      this.loadPendingRequests();
      return;
    }
    this.pendingRequests = [];
    this.loadingPending = false;
    this.requestMessage = '';
  }

  loadParticipants(): void {
    if (!this.eventId) return;

    this.loading = true;
    this.errorMessage = '';

    this.participationService.getParticipantsByEvent(this.eventId).subscribe({
      next: (data) => {
        this.participants = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Impossible de charger les participants';
        this.loading = false;
      }
    });
  }

  loadPendingRequests(): void {
    if (!this.eventId || !this.canManageRequests) return;

    this.loadingPending = true;
    this.requestMessage = '';
    this.participationService.getPendingRequestsByEvent(this.eventId).subscribe({
      next: (data) => {
        this.pendingRequests = data;
        this.loadingPending = false;
      },
      error: (err) => {
        if (err?.status === 403) {
          this.canManageRequests = false;
          this.requestMessage = '';
          this.pendingRequests = [];
          this.loadingPending = false;
          return;
        }
        this.requestMessage = 'Impossible de charger les demandes en attente';
        this.loadingPending = false;
      }
    });
  }

  approveRequest(id?: number): void {
    if (!id || this.processingRequestId !== null) return;

    this.processingRequestId = id;
    this.requestMessage = '';
    this.participationService.approveRequest(id).subscribe({
      next: () => {
        this.processingRequestId = null;
        this.requestMessage = 'Demande approuvée.';
        this.loadPendingRequests();
        this.loadParticipants();
      },
      error: (err) => {
        this.processingRequestId = null;
        this.requestMessage = err?.error?.message ?? 'Impossible d’approuver la demande';
      }
    });
  }

  rejectRequest(id?: number): void {
    if (!id || this.processingRequestId !== null) return;

    this.processingRequestId = id;
    this.requestMessage = '';
    this.participationService.rejectRequest(id).subscribe({
      next: () => {
        this.processingRequestId = null;
        this.requestMessage = 'Demande refusée.';
        this.loadPendingRequests();
      },
      error: (err) => {
        this.processingRequestId = null;
        this.requestMessage = err?.error?.message ?? 'Impossible de refuser la demande';
      }
    });
  }

  cancelParticipation(id?: number): void {
    if (!id) return;

    const confirmDelete = confirm('Annuler cette participation ?');
    if (!confirmDelete) return;

    this.participationService.cancelParticipation(id).subscribe({
      next: () => this.loadParticipants(),
      error: () => {
        this.errorMessage = 'Annulation échouée';
      }
    });
  }

  goBack(): void {
    if (this.eventId) {
      this.router.navigate(['/events', this.eventId]);
      return;
    }
    this.router.navigate(['/events']);
  }
}
