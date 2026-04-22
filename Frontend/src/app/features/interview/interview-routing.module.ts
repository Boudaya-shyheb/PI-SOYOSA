import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InterviewDashboardComponent } from './interview-dashboard/interview-dashboard.component';
import { InterviewFormComponent } from './interview-form/interview-form.component';
import { InterviewPendingComponent } from './interview-pending/interview-pending.component';
import { InterviewMeetingComponent } from './interview-meeting/interview-meeting.component';

const routes: Routes = [
  {
    path: '',
    component: InterviewDashboardComponent,
    children: [
      { path: 'form', component: InterviewFormComponent },
      { path: 'pending', component: InterviewPendingComponent },
      { path: 'meeting', component: InterviewMeetingComponent },
      { path: '', redirectTo: 'form', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class InterviewRoutingModule { }
