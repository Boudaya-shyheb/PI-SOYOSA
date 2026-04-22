import { Component } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {HttpClient} from "@angular/common/http";
import {FormsModule} from "@angular/forms";
import Swal from 'sweetalert2';

@Component({
  selector: 'app-reset-password',
  standalone: true,
    imports: [
        FormsModule
    ],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.css'
})
export class ResetPasswordComponent {

    token!: string;
    newPassword: string = '';
    confirmPassword: string = '';
    message: string = '';

    constructor(
        private route: ActivatedRoute,
        private http: HttpClient,
        private router: Router
    ) {}

    ngOnInit(): void {
        this.token = this.route.snapshot.queryParamMap.get('token')!;
        console.log(this.token);
    }

    resetPassword() {

        if (this.newPassword !== this.confirmPassword) {
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Passwords do not match'
            });
            return;
        }

        const body = {
            token: this.token,
            newPassword: this.newPassword
        };

        this.http.post('http://localhost:8080/api/user/reset-password', body)
            .subscribe({
                next: (res :any) => {
                    Swal.fire({
                        icon: 'success',
                        title: 'Password Reset Successful!',
                        text: 'You can now login with your new password.',
                        confirmButtonText: 'Go to Login'
                    }).then(() => {
                        this.router.navigate(['/user/login']);
                    });

                },
                error: (e :any) => {
                    console.log(e)
                    Swal.fire({
                        icon: 'error',
                        title: 'Error',
                        text: 'Invalid or expired reset token'
                    });
                }
            });
    }

}
