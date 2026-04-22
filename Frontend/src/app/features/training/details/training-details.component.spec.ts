import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TrainingDetailsComponent } from './training-details.component';
import { TrainingService } from '../../../services/training.service';
import { AuthService } from '../../../services/auth.service';
import { TrainingEnrollmentService } from '../../../services/training-enrollment.service';
import { TrainingPaymentService } from '../../../services/training-payment.service';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';

describe('TrainingDetailsComponent', () => {
  let component: TrainingDetailsComponent;
  let fixture: ComponentFixture<TrainingDetailsComponent>;
  let trainingServiceSpy: jasmine.SpyObj<TrainingService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let enrollmentServiceSpy: jasmine.SpyObj<TrainingEnrollmentService>;
  let paymentServiceSpy: jasmine.SpyObj<TrainingPaymentService>;
  let router: Router;

  beforeEach(async () => {
    const tSpy = jasmine.createSpyObj('TrainingService', ['getTrainingById', 'getSessionsByTraining', 'getReviewsForTraining', 'deleteTraining']);
    const aSpy = jasmine.createSpyObj('AuthService', ['getRole', 'getUserDisplayName', 'isStudent', 'isAuthenticated', 'getUserId']);
    const eSpy = jasmine.createSpyObj('TrainingEnrollmentService', ['isStudentEnrolledInTraining', 'getMyEnrollments', 'enrollInSession']);
    const pSpy = jasmine.createSpyObj('TrainingPaymentService', ['createPaymentIntent', 'getStripe']);

    await TestBed.configureTestingModule({
      declarations: [ TrainingDetailsComponent ],
      imports: [ FormsModule, RouterTestingModule ],
      providers: [
        { provide: TrainingService, useValue: tSpy },
        { provide: AuthService, useValue: aSpy },
        { provide: TrainingEnrollmentService, useValue: eSpy },
        { provide: TrainingPaymentService, useValue: pSpy },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: { paramMap: { get: () => '1' } }
          }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    trainingServiceSpy = TestBed.inject(TrainingService) as jasmine.SpyObj<TrainingService>;
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    enrollmentServiceSpy = TestBed.inject(TrainingEnrollmentService) as jasmine.SpyObj<TrainingEnrollmentService>;
    paymentServiceSpy = TestBed.inject(TrainingPaymentService) as jasmine.SpyObj<TrainingPaymentService>;
    router = TestBed.inject(Router);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TrainingDetailsComponent);
    component = fixture.componentInstance;
    
    // Default mock returns
    trainingServiceSpy.getTrainingById.and.returnValue(of({ id: 1, title: 'Java' } as any));
    trainingServiceSpy.getSessionsByTraining.and.returnValue(of({ content: [] }));
    trainingServiceSpy.getReviewsForTraining.and.returnValue(of([]));
    authServiceSpy.isStudent.and.returnValue(false);
  });

  it('should load training data on init', () => {
    fixture.detectChanges(); // ngOnInit

    expect(trainingServiceSpy.getTrainingById).toHaveBeenCalledWith(1);
    expect(component.training.title).toBe('Java');
    expect(component.loading).toBeFalse();
  });

  it('should check enrollment if user is student', () => {
    authServiceSpy.isStudent.and.returnValue(true);
    enrollmentServiceSpy.isStudentEnrolledInTraining.and.returnValue(of({ enrolled: true, eligibleToReview: true }));
    enrollmentServiceSpy.getMyEnrollments.and.returnValue(of({ content: [] }));

    fixture.detectChanges();

    expect(enrollmentServiceSpy.isStudentEnrolledInTraining).toHaveBeenCalledWith(1);
    expect(component.isEnrolledInTraining).toBeTrue();
  });

  it('should navigate back to trainings list', () => {
    const navigateSpy = spyOn(router, 'navigate');
    component.goBack();
    expect(navigateSpy).toHaveBeenCalledWith(['/trainings']);
  });

  it('should call delete training when confirmed', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    trainingServiceSpy.deleteTraining.and.returnValue(of(undefined));
    const navigateSpy = spyOn(router, 'navigate');
    component.training = { id: 1, title: 'Java' } as any;

    component.deleteTraining();

    expect(trainingServiceSpy.deleteTraining).toHaveBeenCalledWith(1);
    expect(navigateSpy).toHaveBeenCalledWith(['/trainings']);
  });
});
