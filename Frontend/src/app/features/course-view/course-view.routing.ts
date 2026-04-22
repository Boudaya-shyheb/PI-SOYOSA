import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CourseViewComponent } from './course-view.component';

const routes: Routes = [
  { path: '', component: CourseViewComponent },
  { path: ':courseId', component: CourseViewComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CourseViewRoutingModule {}
