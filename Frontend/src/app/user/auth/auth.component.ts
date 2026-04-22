import { Component, OnInit } from '@angular/core';
import { FormsModule } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { CommonModule } from "@angular/common";
import { countries } from './countries.data';
import { AuthService } from "../../services/auth.service";
import Swal from 'sweetalert2';

@Component({
    selector: 'app-auth',
    standalone: true,
    imports: [FormsModule, CommonModule],
    templateUrl: './auth.component.html',
    styleUrls: ['./auth.component.css']
})
export class AuthComponent implements OnInit {

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        const params = new URLSearchParams(window.location.search);
        const token = params.get('token');
            console.log(token);
            if (token) {
                console.log('Token found in URL, setting session...');
                this.authService.setSession(token);
                
                // Clear query params and navigate to returnUrl or home
                const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
                this.router.navigateByUrl(returnUrl, { replaceUrl: true });
                
                Swal.fire({
                    icon: 'success',
                    title: 'Login Successful',
                    text: 'Welcome back!',
                    timer: 2000,
                    showConfirmButton: false
                });
            }
    }

    isRegisterMode = false;

    toggleToRegister(): void {
        this.isRegisterMode = true;
    }

    toggleToLogin(): void {
        this.isRegisterMode = false;
    }

    login = {
        username: '',
        password: ''
    }

    register = {
        firstName: '',
        lastName: '',
        email: '',
        user: {
            username: '',
            password: '',
            role: ''
        },
        phoneNumber: '',
        address: ''
    }

    countries = countries;

    selectedCountry = this.countries.find(c => c.name === 'Tunisia') || this.countries[0];

    loginAccount(): void {
        this.authService.login(this.login).subscribe({
            next: (res: any) => {
                const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
                this.router.navigateByUrl(returnUrl);
            },
            error: (err: any) => {
                console.error('Login failed', err);
                const backendMessage = err?.error?.token || err?.error?.message;
                Swal.fire('Error', backendMessage || 'Login failed. Please check your credentials.', 'error');
            }
        });
    }

    registerAccount(): void {
        if (!this.register.firstName || !this.register.lastName || !this.register.email ||
            !this.register.user.password || !this.register.phoneNumber || this.register.phoneNumber.length !== 8 ||
            !this.register.address || !this.register.user.role) {
            Swal.fire('Error', 'Please fill out all required fields correctly.', 'error');
            return;
        }

        this.register.user.username = this.register.email;

        const payload = {
            firstName: this.register.firstName,
            lastName: this.register.lastName,
            phoneNumber: Number(this.register.phoneNumber),
            address: this.register.address,
            mail: this.register.email,
            user: {
                username: this.register.user.username,
                password: this.register.user.password,
                role: this.register.user.role
            }
        };


        this.authService.register(payload).subscribe({
            next: (res: any) => {
                console.log('Registration successful', res);
                Swal.fire('Success', 'Registration successful! Please login.', 'success');
                this.isRegisterMode = false;
                this.register = {
                    firstName: '',
                    lastName: '',
                    email: '',
                    user: {
                        username: '',
                        password: '',
                        role: ''
                    },
                    phoneNumber: '',
                    address: ''
                };
            },
            error: (err: any) => {
                console.error('Registration failed', err);
                if (err?.status === 409) {
                    Swal.fire('Error', 'This email is already registered. Please login or use a different email.', 'error');
                    return;
                }
                const backendMessage = err?.error?.message || err?.error?.token;
                Swal.fire('Error', backendMessage || 'Registration failed. Please try again.', 'error');
            }
        });
    }

    capitalizeFirst(field: 'firstName' | 'lastName') {
        if (this.register[field]) {
            const val = this.register[field];
            this.register[field] = val.charAt(0).toUpperCase() + val.slice(1).toLowerCase();
        }
    }

    resetPwd(): void {
        this.router.navigate(['/user/forgot-password']);
    }

    loginWithGoogle(): void {
        this.authService.loginWithGoogle();
    }

    loginWithLinkedIn(): void {
        this.authService.loginWithLinkedIn();
    }
}
