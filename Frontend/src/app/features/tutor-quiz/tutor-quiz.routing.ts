import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TutorQuizComponent } from './tutor-quiz.component';
import { authGuard } from '../../guards/auth.guard';

const routes: Routes = [
  { path: '', component: TutorQuizComponent, canActivate: [authGuard] }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TutorQuizRoutingModule {}
