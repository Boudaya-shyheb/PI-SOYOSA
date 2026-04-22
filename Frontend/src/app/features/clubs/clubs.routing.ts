import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { ClubsComponent } from './clubs.component';
import { ClubFormComponent } from './components/club-form/club-form.component';
import { ClubDetailsComponent } from './components/club-details/club-details.component';

// Activities
import { ActivityListComponent } from './activities/components/activity-list/activity-list.component';
import { ActivityFormComponent } from './activities/components/activity-form/activity-form.component';

// Members
import { MemberListComponent } from './members/components/member-list/member-list.component';
import { MemberFormComponent } from './members/components/member-form/member-form.component';
import { MemberStatsComponent } from './members/components/member-stats/member-stats.component';
import { RoleGuard } from '../../guards/role.guard';

const routes: Routes = [
  { path: '', component: ClubsComponent, pathMatch: 'full' },
  { path: 'create', component: ClubFormComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'edit/:id', component: ClubFormComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
  { path: ':id', component: ClubDetailsComponent },

  { path: ':clubId/activities/create', component: ActivityFormComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
  { path: ':clubId/activities/edit/:activityId', component: ActivityFormComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
  { path: ':clubId/activities', component: ActivityListComponent, pathMatch: 'full' },

  { path: ':clubId/members', component: MemberListComponent },
  { path: ':clubId/members/stats', component: MemberStatsComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
  { path: ':clubId/members/create', component: MemberFormComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
  { path: ':clubId/members/edit/:id', component: MemberFormComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ClubsRoutingModule {}
