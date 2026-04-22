import { Component, OnInit } from '@angular/core';
import { EventService } from '../../services/event.service';
import { EventCategoryService } from '../../services/event-category.service';
import { Event } from '../../models/event.model';
import { EventCategory } from '../../models/event-category.model';
import { AuthService } from '../../services/auth.service';

type Filter = 'All' | 'Today' | 'Past event' | 'Next event';

@Component({
  selector: 'app-events',
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.css']
})
export class EventsComponent implements OnInit {

  events: Event[] = [];
  filteredEvents: Event[] = [];
  categories: EventCategory[] = [];
  searchTerm = '';
  pageSize = 3;
  currentPage = 1;

  filter: Filter = 'All';
  tabs: Filter[] = ['All', 'Today', 'Past event', 'Next event'];

  loading = false;
  errorMessage = '';

  constructor(
    private eventService: EventService,
    private categoryService: EventCategoryService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadEvents();
    this.loadCategories();
  }

  // 🔥 LOAD EVENTS FROM BACKEND
  loadEvents(): void {
    this.loading = true;
    this.eventService.getAllEvents().subscribe({
      next: (data) => {
        this.events = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Failed to load events';
        this.loading = false;
      }
    });
  }

  // 🔥 LOAD CATEGORIES
  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (data) => {
        this.categories = data;
      },
      error: (err) => {
        console.error('Category load error', err);
      }
    });
  }

  // 🔥 FILTER LOGIC
  filtered(): Event[] {
    return this.filteredEvents;
  }

  get paginatedEvents(): Event[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredEvents.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredEvents.length / this.pageSize));
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index + 1);
  }

  applyFilters(): void {
    const filteredByTab = this.filterByTab(this.events);
    const term = this.searchTerm.trim().toLowerCase();

    if (!term) {
      this.filteredEvents = filteredByTab;
      this.currentPage = 1;
      return;
    }

    this.filteredEvents = filteredByTab.filter(event => {
      const title = event.title?.toLowerCase() ?? '';
      const location = event.location?.toLowerCase() ?? '';
      const categoryName = event.categoryName?.toLowerCase() ?? '';
      return title.includes(term) || location.includes(term) || categoryName.includes(term);
    });
    this.currentPage = 1;
  }

  private filterByTab(events: Event[]): Event[] {
    const today = new Date();

    if (this.filter === 'All') {
      return events;
    }

    if (this.filter === 'Today') {
      return events.filter(e =>
        new Date(e.date).toDateString() === today.toDateString()
      );
    }

    if (this.filter === 'Past event') {
      return events.filter(e =>
        new Date(e.date) < today
      );
    }

    if (this.filter === 'Next event') {
      return events.filter(e =>
        new Date(e.date) > today
      );
    }

    return events;
  }

  setFilter(tab: Filter): void {
    this.filter = tab;
    this.applyFilters();
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.currentPage = page;
  }

  goToPreviousPage(): void {
    this.goToPage(this.currentPage - 1);
  }

  goToNextPage(): void {
    this.goToPage(this.currentPage + 1);
  }

  // 🔥 DELETE EVENT
  deleteEvent(id: number): void {

    const confirmDelete = confirm('Are you sure you want to delete this event?');

    if (!confirmDelete) return;

    this.eventService.deleteEvent(id).subscribe({
      next: () => {
        this.loadEvents(); // refresh list
      },
      error: (err) => {
        console.error(err);
        alert('Failed to delete event');
      }
    });
  }

}
