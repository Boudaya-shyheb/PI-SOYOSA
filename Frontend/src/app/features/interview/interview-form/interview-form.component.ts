import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {AuthService} from "../../../services/auth/auth.service";
import {InterviewService} from "../../../services/interview.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-interview-form',
  templateUrl: './interview-form.component.html',
  styleUrls: ['./interview-form.component.css']
})
export class InterviewFormComponent implements OnInit {
  interviewForm!: FormGroup;
  selectedFile: File | null = null;
  fileError: string | null = null;

  constructor(private fb: FormBuilder, private authService: AuthService, private interviewService: InterviewService, private router: Router) {
    this.interviewForm = this.fb.group({
      interviewDate: ['', Validators.required]
    });
  }
  minDate: string = '';

  ngOnInit(): void {
    const today = new Date();
    today.setDate(today.getDate() + 1); // tomorrow

    this.minDate = today.toISOString().split('T')[0];
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.type !== 'application/pdf') {
        this.fileError = 'Please upload a PDF file only.';
        this.selectedFile = null;
      } else {
        this.fileError = null;
        this.selectedFile = file;
      }
    }
  }

  onSubmit(): void {
    if (this.selectedFile && this.interviewForm.valid) {
      const token = this.authService.getToken();
      let username: string | null = null;

      if (token) {
        username = this.authService.getUserEmail();
      }

      const formData = new FormData();
      formData.append('file', this.selectedFile);
      formData.append('date', this.interviewForm.value.interviewDate);
      if (username) {
        formData.append('user', username);
      }

      this.interviewService.createInterview(formData).subscribe({
          next: (data) => {
          console.log(data);
          this.router.navigate(['/interview/pending']);
          }
      })

    } else {
      if (!this.selectedFile) {
        this.fileError = 'Please upload your CV as a PDF.';
      }
    }
    

  }
}
