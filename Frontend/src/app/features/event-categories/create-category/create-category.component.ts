import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventCategoryService } from '../../../services/event-category.service';
import { EventCategory } from '../../../models/event-category.model';

@Component({
  selector: 'app-create-category',
  templateUrl: './create-category.component.html'
})
export class CreateCategoryComponent implements OnInit {

  category: EventCategory = {
    name: '',
    description: ''
  };

  isEditMode = false;
  categoryId?: number;

  constructor(
    private categoryService: EventCategoryService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.isEditMode = true;
      this.categoryId = Number(id);

      this.categoryService.getCategoryById(this.categoryId)
        .subscribe((data: EventCategory) => {
          this.category = data;
        });
    }
  }

  submit(): void {

    if (!this.category.name.trim()) return;

    if (this.isEditMode && this.categoryId) {

      this.categoryService.updateCategory(this.categoryId, this.category)
        .subscribe(() => this.router.navigate(['/categories']));

    } else {

      this.categoryService.createCategory(this.category)
        .subscribe(() => this.router.navigate(['/categories']));
    }
  }
}