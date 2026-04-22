import { Component } from '@angular/core';
import { DataService } from '../../services/data.service';
import { SchoolEvent, Instructor } from '../../models/models';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent {
  events$!: Observable<SchoolEvent[]>;
  instructors$!: Observable<Instructor[]>;

  constructor(private data: DataService) {
    this.events$ = this.data.getEvents();
    this.instructors$ = this.data.getInstructors();
  }
}
