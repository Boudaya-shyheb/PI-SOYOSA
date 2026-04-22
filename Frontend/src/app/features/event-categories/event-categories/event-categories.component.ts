import { Component, OnInit } from '@angular/core';
import { EventCategoryService } from '../../../services/event-category.service';
import { AuthService } from '../../../services/auth.service';
import { EventCategory } from '../../../models/event-category.model';

@Component({
  selector: 'app-event-categories',
  templateUrl: './event-categories.component.html'
})
export class EventCategoriesComponent implements OnInit {

  categories: EventCategory[] = [];

  constructor(
    private categoryService: EventCategoryService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories()
      .subscribe(data => this.categories = data);
  }

  deleteCategory(id: number): void {

    const confirmDelete = confirm('Delete this category?');

    if (!confirmDelete) return;

    this.categoryService.deleteCategory(id).subscribe(() => {
      this.loadCategories();
    });
  }
}
