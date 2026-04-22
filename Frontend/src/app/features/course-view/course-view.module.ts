import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CourseViewRoutingModule } from './course-view.routing';
import { CourseViewComponent } from './course-view.component';
import { CourseSidebarComponent } from './course-sidebar/course-sidebar.component';
import { CourseContentComponent } from './course-content/course-content.component';
import { CertificateComponent } from './certificate/certificate.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [
    CourseViewComponent,
    CourseSidebarComponent,
    CourseContentComponent,
    CertificateComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    SharedModule,
    CourseViewRoutingModule
  ]
})
export class CourseViewModule {}
