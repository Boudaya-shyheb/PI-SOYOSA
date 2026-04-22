import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { AdminInterviewsComponent } from './admin-interviews.component';
import { AdminUsersComponent } from './admin-users.component';
import { adminGuard } from '../../guards/admin.guard';
import {FormsModule} from "@angular/forms";

const routes: Routes = [
  {
    path: '',
    component: AdminDashboardComponent,
    canActivate: [adminGuard],
    children: [
      { path: '', redirectTo: 'interviews', pathMatch: 'full' },
      { path: 'interviews', component: AdminInterviewsComponent },
      { path: 'users', component: AdminUsersComponent }
    ]
  }
];

@NgModule({
  declarations: [
    AdminDashboardComponent,
    AdminInterviewsComponent,
    AdminUsersComponent
  ],
    imports: [
        CommonModule,
        RouterModule.forChild(routes),
        FormsModule
    ]
})
export class AdminDashboardModule { }
