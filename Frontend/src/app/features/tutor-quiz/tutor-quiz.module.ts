import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { TutorQuizRoutingModule } from './tutor-quiz.routing';
import { TutorQuizComponent } from './tutor-quiz.component';

@NgModule({
  declarations: [TutorQuizComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, TutorQuizRoutingModule]
})
export class TutorQuizModule {}
