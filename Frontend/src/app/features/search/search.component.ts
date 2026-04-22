import { Component } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { DataService } from '../../services/data.service';
import { Course } from '../../models/models';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent {
  form: FormGroup;
  courses$!: Observable<Course[]>;

  constructor(private fb: FormBuilder, private data: DataService) {
    this.form = this.fb.group({
      searchTerm: ['']
    });
    this.courses$ = this.data.getCourses();
  }
}
