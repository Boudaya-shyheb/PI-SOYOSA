import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LessonDetailsRoutingModule } from './lesson-details.routing';
import { LessonDetailsComponent } from './lesson-details.component';

@NgModule({
  declarations: [
    LessonDetailsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    LessonDetailsRoutingModule
  ]
})
export class LessonDetailsModule { }
