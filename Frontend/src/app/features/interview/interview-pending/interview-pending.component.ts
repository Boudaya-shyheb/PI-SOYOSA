import { Component, OnInit } from '@angular/core';
import {InterviewService} from "../../../services/interview.service";
import {AuthService} from "../../../services/auth.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-interview-pending',
  templateUrl: './interview-pending.component.html'
})
export class InterviewPendingComponent implements OnInit {

    interview: any;

    constructor(
        private service: InterviewService,
        private auth: AuthService,
        private router: Router
    ) { }

    ngOnInit(){
        const userId = this.auth.getUserEmail();
        this.service.getInterviewByUsername(userId).subscribe(data => {
            this.interview = data;
            if (this.interview?.status === 'PASSED' && this.auth.getUserStatus() !== 'PENDING') {
                setTimeout(() => {
                    this.router.navigate(['/']);
                }, 5000);
            }
        })
    }

}
