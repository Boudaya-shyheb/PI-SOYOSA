import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { EventsRoutingModule } from './events.routing';
import { EventsComponent } from './events.component';
import { CreateEventComponent } from './create-event/create-event.component';
import { EventDetailsComponent } from './event-details/event-details.component';
import { ParticipateEventComponent } from './participate-event/participate-event.component';
import { EventParticipantDetailsComponent } from './event-participant-details/event-participant-details.component';
import { EventFeedbackComponent } from './event-feedback/event-feedback.component';

@NgModule({
  declarations: [EventsComponent, CreateEventComponent, EventDetailsComponent, ParticipateEventComponent, EventParticipantDetailsComponent],
  imports: [CommonModule, FormsModule, SharedModule, EventsRoutingModule, EventFeedbackComponent]
})
export class EventsModule {}
