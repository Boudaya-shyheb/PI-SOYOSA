import { Component, Input } from '@angular/core';
import { SchoolEvent } from '../../models/models';

@Component({
  selector: 'app-event-card',
  templateUrl: './event-card.component.html',
  styleUrls: ['./event-card.component.css']
})
export class EventCardComponent {
  @Input() event!: SchoolEvent;

  get isPast(): boolean {
    return this.event.status === 'Past event';
  }
}
