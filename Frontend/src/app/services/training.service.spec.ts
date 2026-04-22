import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TrainingService } from './training.service';
import { AuthService } from './auth.service';
import { of } from 'rxjs';

describe('TrainingService', () => {
  let service: TrainingService;
  let httpMock: HttpTestingController;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('AuthService', ['getRole', 'getId']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        TrainingService,
        { provide: AuthService, useValue: spy }
      ]
    });

    service = TestBed.inject(TrainingService);
    httpMock = TestBed.inject(HttpTestingController);
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch all trainings with normalization', () => {
    const mockPage = {
      content: [
        { id: 1, title: 'Test Training', sessions: [{ id: 101, startTime: '10:00:00' }] }
      ],
      totalElements: 1
    };

    service.getAllTrainings(0, 3).subscribe(result => {
      expect(result.content.length).toBe(1);
      expect(result.content[0].sessions[0].startTime).toBe('10:00'); // Normalized
    });

    const req = httpMock.expectOne(req => req.url === 'http://localhost:8082/api/training' && req.params.has('page'));
    expect(req.request.method).toBe('GET');
    req.flush(mockPage);
  });

  it('should block creation if not a tutor', () => {
    authServiceSpy.getRole.and.returnValue('STUDENT');
    
    service.createTraining({ title: 'New' } as any).subscribe({
      error: (err) => {
        expect(err.message).toContain('Unauthorized');
      }
    });

    httpMock.expectNone('http://localhost:8082/api/training');
  });

  it('should allow creation if tutor', () => {
    authServiceSpy.getRole.and.returnValue('TUTOR');
    const mockTraining = { id: 1, title: 'New' };

    service.createTraining(mockTraining as any).subscribe(result => {
      expect(result.id).toBe(1);
    });

    const req = httpMock.expectOne('http://localhost:8082/api/training');
    expect(req.request.method).toBe('POST');
    req.flush(mockTraining);
  });
});
