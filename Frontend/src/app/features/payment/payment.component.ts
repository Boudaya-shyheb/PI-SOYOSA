import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PaymentApiService } from '../../services/payment-api.service';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css']
})
export class PaymentComponent implements OnInit {
  courseId = '';
  courseTitle = 'Course';
  amount = 0;
  returnUrl = '/courses';

  paying = false;
  paymentSuccess = false;
  transactionId = '';
  errorMessage = '';

  cardName = '';
  cardNumber = '';
  expiry = '';
  cvv = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private paymentApi: PaymentApiService
  ) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.courseId = params.get('courseId') ?? '';
    this.courseTitle = params.get('title') ?? 'Course';
    this.amount = Number(params.get('amount') ?? 0);
    this.returnUrl = params.get('returnUrl') ?? '/courses';

    if (!this.courseId) {
      this.router.navigate(['/courses']);
    }
  }

  payNow(): void {
    if (this.paying || !this.courseId) {
      return;
    }

    this.errorMessage = '';
    this.paying = true;

    this.paymentApi.processCoursePayment(this.courseId).subscribe({
      next: paymentResult => {
        // Redirect directly to Stripe Checkout URL
        if (paymentResult.sessionUrl) {
          window.location.href = paymentResult.sessionUrl;
        } else {
          this.transactionId = paymentResult.paymentId;
          this.paymentSuccess = true;
          this.paying = false;
        }
      },
      error: (err) => {
        this.paying = false;
        this.errorMessage = err?.error?.message || 'Payment service is currently unavailable. Please try again later.';
      }
    });
  }

  continueToCourse(): void {
    if (this.returnUrl.startsWith('/')) {
      this.router.navigateByUrl(this.returnUrl);
      return;
    }
    this.router.navigate(['/courses', this.courseId]);
  }

  backToCourses(): void {
    this.router.navigate(['/courses']);
  }

  private isFormValid(): boolean {
    return this.cardName.trim().length > 1
      && this.cardNumber.replace(/\s+/g, '').length >= 12
      && this.expiry.trim().length >= 4
      && this.cvv.trim().length >= 3;
  }
}
