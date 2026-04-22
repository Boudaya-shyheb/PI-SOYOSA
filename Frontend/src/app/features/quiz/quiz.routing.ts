import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { QuizComponent } from './quiz.component';
import { EnrollmentGuard } from '../../guards/enrollment.guard';

const routes: Routes = [
  { path: '', component: QuizComponent, canActivate: [EnrollmentGuard] }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class QuizRoutingModule {}
