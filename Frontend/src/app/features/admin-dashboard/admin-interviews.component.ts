import { Component, OnInit } from '@angular/core';
import Swal from 'sweetalert2';
import {InterviewService} from "../../services/interview.service";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-admin-interviews',
  templateUrl: './admin-interviews.component.html'
})
export class AdminInterviewsComponent implements OnInit {
  interviews: any[] = [];

  constructor(private interviewService: InterviewService, private authService: AuthService) {}

  ngOnInit(): void {
    this.loadInterviews();
  }

  loadInterviews(): void {
    this.interviewService.getAllInterviews().subscribe((data: any) => {
      this.interviews = data;
    });
  }

  schedule(interview: any): void {
      const adminUsername = this.authService.getUserEmail();

      Swal.fire({
          title: 'Schedule Interview',
          html: `
            <p class="mb-4 text-sm text-gray-500 text-left ml-2">Are you sure you want to schedule the interview for ${interview.user}?</p>
            <div class="text-left ml-2">
                <label class="block text-xs font-semibold text-gray-400 mb-1">CHOOSE TIME</label>
                <input type="time" id="swal-time" class="swal2-input !mt-0 !mx-0 w-full rounded-xl border-gray-200">
            </div>
          `,
          icon: 'question',
          showCancelButton: true,
          confirmButtonText: 'Yes, Schedule',
          preConfirm: () => {
              const time = (document.getElementById('swal-time') as HTMLInputElement).value;
              if (!time) {
                  Swal.showValidationMessage('Please select a time for the interview');
                  return false;
              }
              return { time };
          }
      }).then((result) => {
          if (result.isConfirmed && result.value) {
              const updated = {
                  ...interview,
                  status: 'SCHEDULED',
                  time: result.value.time,
                  administrator: adminUsername
              };
              this.interviewService.updateInterview(interview.id, updated).subscribe(() => {
                  this.loadInterviews();
                  Swal.fire('Scheduled!', 'The interview has been scheduled.', 'success');
              });
          }
      });
  }

  pass(interview: any): void {
      Swal.fire({
          title: 'Mark as Passed',
          text: `Did ${interview.user} pass the interview?`,
          icon: 'success',
          showCancelButton: true,
          confirmButtonText: 'Yes, Passed'
      }).then((result) => {
          if (result.isConfirmed) {
              const updated = { ...interview, status: 'PASSED' };
              this.interviewService.passInterview(interview.id).subscribe(() => {
                  this.loadInterviews();
                  Swal.fire('Passed!', 'The candidate has been marked as passed.', 'success');
              });
          }
      });
  }

  fail(interview: any): void {
      Swal.fire({
          title: 'Mark as Failed',
          text: `Are you sure ${interview.user} failed the interview?`,
          icon: 'error',
          showCancelButton: true,
          confirmButtonText: 'Yes, Failed'
      }).then((result) => {
          if (result.isConfirmed) {
              const updated = { ...interview, status: 'FAILED' };
              this.interviewService.failInterview(interview.id).subscribe(() => {
                  this.loadInterviews();
                  Swal.fire('Failed', 'The candidate has been marked as failed.', 'error');
              });
          }
      });
  }
}
