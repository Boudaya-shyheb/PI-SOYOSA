import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CourseListComponent } from './course-list.component';
import { EnrollmentGuard } from '../../guards/enrollment.guard';

const routes: Routes = [
  { path: '', component: CourseListComponent },
  {
    path: ':courseId/lessons/:lessonId/quiz',
    loadChildren: () => import('../quiz/quiz.module').then(m => m.QuizModule)
  },
  {
    path: ':courseId',
    loadChildren: () => import('../course-view/course-view.module').then(m => m.CourseViewModule)
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CoursesRoutingModule {}
