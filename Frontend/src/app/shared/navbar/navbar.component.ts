import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  constructor(public auth: AuthService, private router: Router) { }

  get hasSession(): boolean {
    return !!this.auth.getToken();
  }

  get roleLabel(): string {
    const role = this.auth.getUserRole();
    return (role === 'USER' || !role) ? 'Guest' : role;
  }

  goTo() {
    this.router.navigate(['/user/login']);
  }

  logout(): void {
    this.auth.logout();
  }

  profilePage(): void {
      if (this.auth.isAuthenticated()) {
          this.router.navigate(['/user/profile']);
      }else {
          this.goTo();
      }

  }

  adminDashboard(): void {
    if (this.auth.isAdmin()) {
      this.router.navigate(['/admin']);
    }
  }

}
