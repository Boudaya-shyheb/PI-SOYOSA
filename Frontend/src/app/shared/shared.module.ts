import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from './navbar/navbar.component';
import { FooterComponent } from './footer/footer.component';
import { EventCardComponent } from './event-card/event-card.component';
import { NotificationContainerComponent } from './notification-container/notification-container.component';
import { LoadingSpinnerComponent } from './loading-spinner/loading-spinner.component';
import { EnrollmentModalComponent } from './enrollment-modal/enrollment-modal.component';
import { ConfirmDialogComponent } from './confirm-dialog/confirm-dialog.component';
import { NotificationDropdownComponent } from './notification-dropdown/notification-dropdown.component';
import { MapLocationComponent } from './components/map-location/map-location.component';

@NgModule({
  declarations: [
    MapLocationComponent,
    NavbarComponent,
    FooterComponent,
    EventCardComponent,
    NotificationContainerComponent,
    LoadingSpinnerComponent,
    EnrollmentModalComponent,
    ConfirmDialogComponent
  ],
  imports: [CommonModule, RouterModule, NotificationDropdownComponent],
  exports: [
    NavbarComponent,
    FooterComponent,
    EventCardComponent,
    NotificationContainerComponent,
    LoadingSpinnerComponent,
    EnrollmentModalComponent,
    ConfirmDialogComponent,
    NotificationDropdownComponent,
    MapLocationComponent
  ]
})
export class SharedModule {}
