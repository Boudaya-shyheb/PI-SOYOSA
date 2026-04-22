import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { InterviewRoutingModule } from './interview-routing.module';

import { InterviewDashboardComponent } from './interview-dashboard/interview-dashboard.component';
import { InterviewFormComponent } from './interview-form/interview-form.component';
import { InterviewPendingComponent } from './interview-pending/interview-pending.component';
import { InterviewMeetingComponent } from './interview-meeting/interview-meeting.component';

@NgModule({
  declarations: [
    InterviewDashboardComponent,
    InterviewFormComponent,
    InterviewPendingComponent,
    InterviewMeetingComponent
  ],
  imports: [
    CommonModule,
    InterviewRoutingModule,
    ReactiveFormsModule,
    FormsModule
  ]
})
export class InterviewModule { }
