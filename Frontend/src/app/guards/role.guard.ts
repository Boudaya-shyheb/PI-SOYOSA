import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/api-models';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const allowedRoles = route.data['roles'] as Role[] | undefined;
    
    // If no roles specified, allow access
    if (!allowedRoles) {
      return true;
    }

    const userRole = this.auth.getRole();
    
    // Check if user's role is in allowed roles
    if (allowedRoles.includes(userRole)) {
      return true;
    }

    // Unauthorized - redirect to home or unauthorized page
    console.warn(`Access denied for role: ${userRole}. Allowed roles: ${allowedRoles.join(', ')}`);
    return this.router.parseUrl('/');
  }
}
