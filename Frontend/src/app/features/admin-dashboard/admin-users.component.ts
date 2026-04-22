import { Component, OnInit } from '@angular/core';
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html'
})
export class AdminUsersComponent implements OnInit {
  tutors: any[] = [];
  students: any[] = [];

  tutorPage: number = 0;
  tutorTotalPages: number = 0;
  tutorSearch: string = '';

  studentPage: number = 0;
  studentTotalPages: number = 0;
  studentSearch: string = '';

  pageSize: number = 5;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.loadTutors();
    this.loadStudents();
  }

  loadTutors(): void {
    this.authService.getAllUsersByRole('TUTOR', this.tutorPage, this.pageSize, this.tutorSearch).subscribe(data => {
      this.tutors = data.content;
      this.tutorTotalPages = data.totalPages;
    });
  }

  loadStudents(): void {
    this.authService.getAllUsersByRole('STUDENT', this.studentPage, this.pageSize, this.studentSearch).subscribe(data => {
      this.students = data.content;
      this.studentTotalPages = data.totalPages;
    });
  }

  onTutorSearch(): void {
    this.tutorPage = 0;
    this.loadTutors();
  }

  onStudentSearch(): void {
    this.studentPage = 0;
    this.loadStudents();
  }

  nextTutorPage(): void {
    if (this.tutorPage < this.tutorTotalPages - 1) {
      this.tutorPage++;
      this.loadTutors();
    }
  }

  prevTutorPage(): void {
    if (this.tutorPage > 0) {
      this.tutorPage--;
      this.loadTutors();
    }
  }

  nextStudentPage(): void {
    if (this.studentPage < this.studentTotalPages - 1) {
      this.studentPage++;
      this.loadStudents();
    }
  }

  prevStudentPage(): void {
    if (this.studentPage > 0) {
      this.studentPage--;
      this.loadStudents();
    }
  }
}
