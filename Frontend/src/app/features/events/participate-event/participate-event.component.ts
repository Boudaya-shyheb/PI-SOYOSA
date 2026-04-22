import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventParticipationService } from '../../../services/event-participation.service';
import { EventParticipation } from '../../../models/event-participation.model';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-participate-event',
  templateUrl: './participate-event.component.html',
  styleUrls: ['./participate-event.component.css']
})
export class ParticipateEventComponent implements OnInit {

  eventId?: number;

  loading = false;
  errorMessage = '';
  successMessage = '';
  successVisible = false;
  blockedMessage = '';
  canSubmit = true;
  private redirectTimer?: ReturnType<typeof setTimeout>;

  form: EventParticipation = {
    fullName: '',
    email: '',
    phone: '',
    university: '',
    level: '',
    motivation: ''
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private participationService: EventParticipationService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const parsed = Number(idParam);

    if (!idParam || isNaN(parsed)) {
      this.errorMessage = 'Invalid event ID';
      return;
    }

    this.eventId = parsed;
    const profile = this.auth.getProfile();
    this.form.fullName = `${profile.firstName} ${profile.lastName}`.trim();
    this.form.email = profile.email;
    this.form.phone = profile.phoneNumber;
    this.form.level = profile.educationLevel;

    if (!this.auth.isAuthenticated()) {
      this.canSubmit = false;
      this.blockedMessage = 'Session expirée. Merci de vous reconnecter.';
      return;
    }

    this.participationService.getMyRequests().subscribe({
      next: (requests) => {
        const current = requests.find((request) => request.eventId === this.eventId);
        if (current?.status === 'PENDING') {
          this.canSubmit = false;
          this.blockedMessage = 'Votre demande est déjà en attente de validation admin.';
          return;
        }
        if (current?.status === 'APPROVED') {
          this.canSubmit = false;
          this.blockedMessage = 'Votre participation est déjà validée.';
          return;
        }
        this.canSubmit = true;
        this.blockedMessage = '';
      },
      error: (err) => {
        if (err?.status === 403) {
          this.canSubmit = false;
          this.blockedMessage = 'Session expirée. Merci de vous reconnecter.';
          return;
        }
        this.canSubmit = true;
        this.blockedMessage = '';
      }
    });
  }

  submit(): void {

    if (!this.eventId) {
      this.errorMessage = 'Invalid event ID';
      return;
    }
    if (!this.canSubmit) {
      this.errorMessage = this.blockedMessage || 'Demande déjà envoyée';
      return;
    }

    if (!this.form.fullName.trim()) {
      this.errorMessage = 'Profile full name is required';
      return;
    }

    if (!this.form.email.trim()) {
      this.errorMessage = 'Profile email is required';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.successVisible = false;
    this.loading = true;

    const payload: EventParticipation = {
      ...this.form,
      eventId: this.eventId,
      status: 'PENDING',
      participationDate: new Date().toISOString()
    };

    this.participationService
      .participate(this.eventId, payload)
      .subscribe({
        next: () => {
          this.loading = false;
          this.successMessage = 'Demande envoyée. Elle sera validée par l’admin.';
          this.successVisible = true;
          window.scrollTo({ top: 0, behavior: 'smooth' });

          if (this.redirectTimer) {
            clearTimeout(this.redirectTimer);
          }
          this.redirectTimer = setTimeout(() => {
            this.router.navigate(['/events', this.eventId]);
          }, 2000);
        },
        error: (err) => {
          this.loading = false;
          this.successVisible = false;

          if (err.error?.message) {
            this.errorMessage = err.error.message;
          } else {
            this.errorMessage = 'Registration failed';
          }
        }
      });
  }
}
