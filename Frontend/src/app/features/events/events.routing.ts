import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CreateEventComponent } from './create-event/create-event.component';
import { EventDetailsComponent } from './event-details/event-details.component';
import { ParticipateEventComponent } from './participate-event/participate-event.component';
import { EventsComponent } from './events.component';
import { EventParticipantDetailsComponent } from './event-participant-details/event-participant-details.component';
import { RoleGuard } from '../../guards/role.guard';

const routes: Routes = [

  { path: '', component: EventsComponent },

  { path: 'create', component: CreateEventComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },

  { path: ':id/edit', component: CreateEventComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },

  { path: ':id/participate', component: ParticipateEventComponent, canActivate: [RoleGuard], data: { roles: ['CLIENT', 'ADMIN'] } },
  { path: ':id/participants', component: EventParticipantDetailsComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },

  // ⚠️ ALWAYS KEEP THIS LAST
  { path: ':id', component: EventDetailsComponent }

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EventsRoutingModule {}
