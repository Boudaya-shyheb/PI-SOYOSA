import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QuizRoutingModule } from './quiz.routing';
import { QuizComponent } from './quiz.component';

@NgModule({
  declarations: [QuizComponent],
  imports: [CommonModule, FormsModule, QuizRoutingModule]
})
export class QuizModule {}
