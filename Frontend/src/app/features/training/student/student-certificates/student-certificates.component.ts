import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { TrainingEnrollmentService, Enrollment } from '../../../../services/training-enrollment.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-student-certificates',
  templateUrl: './student-certificates.component.html',
  styleUrl: './student-certificates.component.css'
})
export class StudentCertificatesComponent implements OnInit {
  certificates: Enrollment[] = [];
  loading = true;
  errorMessage = '';

  // Modal state
  selectedCertificate: Enrollment | null = null;
  showCertificateModal = false;
  userDisplayName: string | null = null;

  constructor(
    private enrollmentService: TrainingEnrollmentService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.userDisplayName = this.authService.getUserDisplayName();
    this.loadCertificates();
  }

  loadCertificates(): void {
    this.loading = true;
    this.enrollmentService.getMyCertificates().subscribe({
      next: (certs) => {
        this.certificates = certs;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading certificates', err);
        this.errorMessage = 'Failed to load certificates. Please try again later.';
        this.loading = false;
      }
    });
  }

  viewCertificate(cert: Enrollment): void {
    this.selectedCertificate = cert;
    this.showCertificateModal = true;
  }

  closeCertificateModal(): void {
    this.showCertificateModal = false;
    this.selectedCertificate = null;
  }

  printCertificate(): void {
    window.print();
  }

  goBack(): void {
    this.router.navigate(['/trainings']);
  }
}
