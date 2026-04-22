import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { EventCategoriesRoutingModule } from './event-categories-routing.module';

import { EventCategoriesComponent } from './event-categories/event-categories.component';
import { CreateCategoryComponent } from './create-category/create-category.component';

@NgModule({
  declarations: [
    EventCategoriesComponent,
    CreateCategoryComponent
  ],
  imports: [
    CommonModule,
    FormsModule,                // 🔥 IMPORTANT pour ngModel
    EventCategoriesRoutingModule
  ]
})
export class EventCategoriesModule { }