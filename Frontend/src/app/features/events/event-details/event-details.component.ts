import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../../services/event.service';
import { EventParticipationService } from '../../../services/event-participation.service';
import { Event } from '../../../models/event.model';
import { EventParticipation } from '../../../models/event-participation.model';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-event-details',
  templateUrl: './event-details.component.html',
  styleUrls: ['./event-details.component.css']
})
export class EventDetailsComponent implements OnInit {

  event?: Event;
  participants: EventParticipation[] = [];

  loading = true;
  errorMessage = '';
  myRequestStatus: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private participationService: EventParticipationService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {

    const idParam = this.route.snapshot.paramMap.get('id');

    if (!idParam) {
      this.errorMessage = 'Invalid event ID';
      this.loading = false;
      return;
    }

    const id = Number(idParam);

    if (isNaN(id)) {
      this.errorMessage = 'Invalid event ID';
      this.loading = false;
      return;
    }

    this.eventService.getEventById(id).subscribe({
      next: (data) => {
        this.event = data;
        this.loading = false;
        this.loadParticipants();
        this.loadMyRequestStatus();
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Event not found';
        this.loading = false;
      }
    });
  }

  // 🔥 LOAD PARTICIPANTS
  loadParticipants(): void {
    if (!this.event?.id) return;

    this.participationService
      .getParticipantsByEvent(this.event.id)
      .subscribe({
        next: (data) => {
          this.participants = data;
        },
        error: (err) => {
          console.error('Failed to load participants', err);
        }
      });
  }

  private loadMyRequestStatus(): void {
    if (!this.auth.isClient() || !this.event?.id) {
      this.myRequestStatus = null;
      return;
    }

    this.participationService.getMyRequests().subscribe({
      next: (requests) => {
        const matched = requests.find((request) => request.eventId === this.event?.id);
        this.myRequestStatus = matched?.status ?? null;
      },
      error: () => {
        this.myRequestStatus = null;
      }
    });
  }

  get maxParticipantsSafe(): number {
    const value = this.event?.maxParticipants;
    return typeof value === 'number' && value > 0 ? value : 0;
  }

  get remainingSeats(): number {
    const max = this.maxParticipantsSafe;
    return Math.max(max - this.participants.length, 0);
  }

  get isEventFull(): boolean {
    const max = this.maxParticipantsSafe;
    return max > 0 && this.participants.length >= max;
  }

  get registrationPercentage(): number {
    const max = this.maxParticipantsSafe;
    if (max <= 0) return 0;
    return Math.min((this.participants.length / max) * 100, 100);
  }

  get countdownText(): string {
    const status = this.dateStatus;
    if (status === 'future') {
      const days = this.daysUntilEvent;
      const label = days === 1 ? 'day' : 'days';
      return `⏳ Starts in ${days} ${label}`;
    }
    if (status === 'today') {
      return '🔥 Starting today';
    }
    if (status === 'past') {
      return '📁 Event ended';
    }
    return '';
  }

  get dateBadgeLabel(): string {
    const status = this.dateStatus;
    if (status === 'future') return 'Upcoming';
    if (status === 'today') return 'Ongoing';
    if (status === 'past') return 'Past Event';
    return '';
  }

  get dateBadgeClass(): string {
    const status = this.dateStatus;
    if (status === 'future') return 'bg-emerald-100 text-emerald-700';
    if (status === 'today') return 'bg-blue-100 text-blue-700';
    if (status === 'past') return 'bg-gray-200 text-gray-700';
    return 'bg-gray-100 text-gray-500';
  }

  exportParticipants(): void {
    const headers = ['Full Name', 'Email', 'Phone', 'University', 'Level', 'Status'];
    const rows = this.participants.map(participant => [
      participant.fullName,
      participant.email,
      participant.phone ?? '',
      participant.university ?? '',
      participant.level ?? '',
      participant.status ?? ''
    ]);

    const csv = [headers, ...rows]
      .map(row => row.map(value => this.csvEscape(value)).join(','))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const fileName = this.buildCsvFileName();

    link.href = URL.createObjectURL(blob);
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(link.href);
  }

  private get dateStatus(): 'future' | 'today' | 'past' | 'unknown' {
    const eventDate = this.eventDateValue;
    if (!eventDate) return 'unknown';
    const today = this.stripTime(new Date());
    const eventDay = this.stripTime(eventDate);
    if (eventDay.getTime() === today.getTime()) return 'today';
    if (eventDay.getTime() > today.getTime()) return 'future';
    return 'past';
  }

  private get daysUntilEvent(): number {
    const eventDate = this.eventDateValue;
    if (!eventDate) return 0;
    const today = this.stripTime(new Date()).getTime();
    const eventDay = this.stripTime(eventDate).getTime();
    const diffMs = Math.max(eventDay - today, 0);
    return Math.ceil(diffMs / (1000 * 60 * 60 * 24));
  }

  private get eventDateValue(): Date | null {
    const dateStr = this.event?.date;
    if (!dateStr) return null;
    const parsed = new Date(dateStr);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  private stripTime(date: Date): Date {
    return new Date(date.getFullYear(), date.getMonth(), date.getDate());
  }

  private csvEscape(value: string): string {
    const safe = value ?? '';
    const escaped = safe.replace(/"/g, '""');
    return `"${escaped}"`;
  }

  private buildCsvFileName(): string {
    const rawTitle = (this.event?.title ?? 'event').trim().toLowerCase();
    const safeTitle = rawTitle ? rawTitle.replace(/[^a-z0-9]+/gi, '-') : 'event';
    return `${safeTitle}-participants.csv`;
  }

  // 🔥 REDIRECT TO REGISTRATION FORM
  goToParticipate(): void {
    if (!this.event?.id) return;

    this.router.navigate(['/events', this.event.id, 'participate']);
  }

  // 🔥 CANCEL PARTICIPATION
  cancelParticipation(id: number): void {
    this.participationService.cancelParticipation(id)
      .subscribe({
        next: () => {
          this.loadParticipants();
        },
        error: (err) => {
          console.error('Cancel failed', err);
        }
      });
  }

  goBack(): void {
    this.router.navigate(['/events']);
  }
}
