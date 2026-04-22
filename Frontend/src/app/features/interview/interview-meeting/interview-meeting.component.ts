import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-interview-meeting',
  templateUrl: './interview-meeting.component.html'
})
export class InterviewMeetingComponent implements OnInit {
  meetingLink: string = 'https://meet.google.com/new'; // This would normally come from an email or API
  safeUrl: SafeResourceUrl | null = null;

  constructor(private sanitizer: DomSanitizer) {}

  ngOnInit(): void {
    // In a real app, we'd get the link from a route param or service
    this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.meetingLink);
  }

  openMeeting(): void {
    window.open(this.meetingLink, '_blank');
  }
}
