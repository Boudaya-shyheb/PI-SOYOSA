import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';

import { ActivityListComponent } from './components/activity-list/activity-list.component';
import { ActivityFormComponent } from './components/activity-form/activity-form.component';
@NgModule({
  declarations: [
    ActivityListComponent,
    ActivityFormComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule
  ]
})
export class ActivitiesModule { }
