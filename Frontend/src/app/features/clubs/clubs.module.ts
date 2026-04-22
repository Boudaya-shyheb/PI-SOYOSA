import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ClubsRoutingModule } from './clubs.routing';

import { ClubsComponent } from './clubs.component';
import { ClubListComponent } from './components/club-list/club-list.component';
import { ClubFormComponent } from './components/club-form/club-form.component';
import { ClubDetailsComponent } from './components/club-details/club-details.component';
import { ActivitiesModule } from './activities/activities.module';
import { MembersModule } from './members/members.module';

@NgModule({
  declarations: [
    ClubsComponent,
    ClubListComponent,
    ClubFormComponent,
    ClubDetailsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ClubsRoutingModule,
    ActivitiesModule,
    MembersModule
  ]
})
export class ClubsModule {}
