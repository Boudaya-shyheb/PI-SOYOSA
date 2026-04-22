import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { EventService } from '../../../services/event.service';
import { EventCategoryService } from '../../../services/event-category.service';
import { EventCategory } from '../../../models/event-category.model';
import { Event } from '../../../models/event.model';

@Component({
  selector: 'app-create-event',
  templateUrl: './create-event.component.html',
  styleUrls: ['./create-event.component.css']
})
export class CreateEventComponent implements OnInit {

  categories: EventCategory[] = [];

  event: Event = {
    title: '',
    description: '',
    date: '',
    startTime: '',
    endTime: '',
    location: '',
    maxParticipants: 1,
    categoryId: null
  };

  loading = false;
  errorMessage = '';

  isEditMode = false;
  eventId?: number;

  constructor(
    private eventService: EventService,
    private categoryService: EventCategoryService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {

    // Load categories
    this.categoryService.getAllCategories().subscribe({
      next: (data) => this.categories = data,
      error: (err) => console.error('Category load error', err)
    });

    // Detect edit mode
    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam) {
      this.isEditMode = true;
      this.eventId = Number(idParam);

      this.eventService.getEventById(this.eventId).subscribe({
        next: (data) => this.event = data,
        error: () => this.errorMessage = 'Failed to load event'
      });
    }
  }

  submit(): void {

    this.errorMessage = '';

    if (!this.event.title.trim()) {
      this.errorMessage = 'Title is required';
      return;
    }

    if (!this.event.description.trim()) {
      this.errorMessage = 'Description is required';
      return;
    }

    if (!this.event.date) {
      this.errorMessage = 'Date is required';
      return;
    }

    if (!this.event.startTime || !this.event.endTime) {
      this.errorMessage = 'Start and End time are required';
      return;
    }

    const startTime = new Date(`${this.event.date}T${this.event.startTime}`);
    const endTime = new Date(`${this.event.date}T${this.event.endTime}`);

    if (startTime >= endTime) {
      this.errorMessage = 'Start time must be before end time';
      return;
    }

    if (!this.event.categoryId) {
      this.errorMessage = 'Please select a category';
      return;
    }

    if (this.event.maxParticipants < 1) {
      this.errorMessage = 'Max participants must be at least 1';
      return;
    }

    this.loading = true;

    if (this.isEditMode && this.eventId) {

      this.eventService.updateEvent(this.eventId, this.event).subscribe({
        next: () => {
          this.loading = false;
          this.router.navigate(['/events']);
        },
        error: () => {
          this.errorMessage = 'Failed to update event';
          this.loading = false;
        }
      });

    } else {

    this.eventService.createEvent(this.event).subscribe({
      next: (created) => {
        this.loading = false;
        const createdId = created?.id;
        this.router.navigate(createdId ? ['/events', createdId] : ['/events']);
      },
        error: () => {
          this.errorMessage = 'Failed to create event';
          this.loading = false;
        }
      });
    }
  }

  deleteEvent(): void {

    if (!this.eventId) return;

    const confirmDelete = confirm('Are you sure you want to delete this event?');

    if (!confirmDelete) return;

    this.loading = true;

    this.eventService.deleteEvent(this.eventId).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/events']);
      },
      error: () => {
        this.errorMessage = 'Failed to delete event';
        this.loading = false;
      }
    });
  }
}
