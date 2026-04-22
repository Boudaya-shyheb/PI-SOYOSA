import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { EnrollmentApiService } from '../services/enrollment-api.service';

@Injectable({ providedIn: 'root' })
export class EnrollmentGuard implements CanActivate {
  constructor(
    private auth: AuthService,
    private enrollments: EnrollmentApiService,
    private router: Router
  ) { }

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> {
    if (!this.auth.isStudent()) {
      return of(this.router.parseUrl('/courses'));
    }

    const courseId = route.paramMap.get('courseId');
    if (!courseId) {
      return of(this.router.parseUrl('/courses'));
    }

    return this.enrollments.getEnrollment(courseId).pipe(
      map(enrollment => {
        if (!enrollment) {
          return this.router.parseUrl('/courses');
        }
        const allowed = enrollment.status === 'ACTIVE' || enrollment.status === 'COMPLETED';
        return allowed ? true : this.router.parseUrl('/courses');
      }),
      catchError(() => of(this.router.parseUrl('/courses')))
    );
  }
}
