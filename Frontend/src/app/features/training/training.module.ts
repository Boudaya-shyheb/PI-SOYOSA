import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { TrainingListComponent } from './training-list.component';
import { TrainingDetailsComponent } from './details/training-details.component';
import { TrainingAddComponent } from './management/training-add/training-add.component';
import { TrainingEditComponent } from './management/training-edit/training-edit.component';
import { SessionAddComponent } from './session/session-add/session-add.component';
import { SessionEditComponent } from './session/session-edit/session-edit.component';
import { MyEnrollmentsComponent } from './student/my-enrollments/my-enrollments.component';
import { StudentScheduleComponent } from './student/student-schedule/student-schedule.component';
import { StudentCertificatesComponent } from './student/student-certificates/student-certificates.component';
import { PlacementTestComponent } from './placement-test/placement-test.component';

import { FullCalendarModule } from '@fullcalendar/angular';
import { authGuard } from '../../guards/auth.guard';
import { RoleGuard } from '../../guards/role.guard';
import { SharedModule } from '../../shared/shared.module';

const routes: Routes = [
    { path: '', component: TrainingListComponent },

    // Tutor Routes - Only TUTOR and TEACHER can access
    { 
        path: 'management/add', 
        component: TrainingAddComponent,
        canActivate: [authGuard, RoleGuard],
        data: { roles: ['TUTOR', 'TEACHER', 'ADMIN'] }
    },
    { 
        path: 'management/edit/:id', 
        component: TrainingEditComponent,
        canActivate: [authGuard, RoleGuard],
        data: { roles: ['TUTOR', 'TEACHER', 'ADMIN'] }
    },

    // Student Routes - Only STUDENT can access
    { 
        path: 'student/my-enrollments', 
        component: MyEnrollmentsComponent,
        canActivate: [authGuard, RoleGuard],
        data: { roles: ['STUDENT', 'USER'] }
    },
    { 
        path: 'student/schedule', 
        component: StudentScheduleComponent,
        canActivate: [authGuard, RoleGuard],
        data: { roles: ['STUDENT', 'USER'] }
    },
    { 
        path: 'student/certificates', 
        component: StudentCertificatesComponent,
        canActivate: [authGuard, RoleGuard],
        data: { roles: ['STUDENT', 'USER'] }
    },

    // Session Routes (nested under training) - Only TUTOR can manage
    { 
        path: ':id/sessions/add', 
        component: SessionAddComponent,
        canActivate: [authGuard, RoleGuard],
        data: { roles: ['TUTOR', 'TEACHER', 'ADMIN'] }
    },
    { 
        path: ':id/sessions/edit/:sessionId', 
        component: SessionEditComponent,
        canActivate: [authGuard, RoleGuard],
        data: { roles: ['TUTOR', 'TEACHER', 'ADMIN'] }
    },

    // Training Details - Public access
    { path: ':id', component: TrainingDetailsComponent },
];

@NgModule({
    declarations: [
        TrainingListComponent,
        TrainingDetailsComponent,
        TrainingAddComponent,
        TrainingEditComponent,
        SessionAddComponent,
        SessionEditComponent,
        MyEnrollmentsComponent,
        StudentScheduleComponent,
        StudentCertificatesComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        RouterModule.forChild(routes),
        FullCalendarModule,
        SharedModule,
        PlacementTestComponent
    ]
})
export class TrainingModule { }
