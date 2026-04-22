import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TrainingListComponent } from './training-list.component';
import { TrainingService } from '../../services/training.service';
import { AuthService } from '../../services/auth.service';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';

describe('TrainingListComponent', () => {
  let component: TrainingListComponent;
  let fixture: ComponentFixture<TrainingListComponent>;
  let trainingServiceSpy: jasmine.SpyObj<TrainingService>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;

  beforeEach(async () => {
    const tSpy = jasmine.createSpyObj('TrainingService', ['getAllTrainings']);
    const aSpy = jasmine.createSpyObj('AuthService', ['getRole']);

    await TestBed.configureTestingModule({
      declarations: [ TrainingListComponent ],
      imports: [ FormsModule, RouterTestingModule ],
      providers: [
        { provide: TrainingService, useValue: tSpy },
        { provide: AuthService, useValue: aSpy }
      ]
    }).compileComponents();

    trainingServiceSpy = TestBed.inject(TrainingService) as jasmine.SpyObj<TrainingService>;
    authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TrainingListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load trainings on init', () => {
    const mockData = {
      content: [{ id: 1, title: 'Test Training' }],
      totalPages: 1,
      totalElements: 1
    };
    trainingServiceSpy.getAllTrainings.and.returnValue(of(mockData));

    component.ngOnInit();

    expect(trainingServiceSpy.getAllTrainings).toHaveBeenCalled();
    expect(component.trainings.length).toBe(1);
    expect(component.loading).toBeFalse();
  });

  it('should handle error when loading trainings', () => {
    trainingServiceSpy.getAllTrainings.and.returnValue(throwError(() => new Error('Error')));

    component.ngOnInit();

    expect(component.errorMessage).toBe('Error loading trainings');
    expect(component.loading).toBeFalse();
  });

  it('should reset page and reload on search', () => {
    const mockData = { content: [], totalPages: 0, totalElements: 0 };
    trainingServiceSpy.getAllTrainings.and.returnValue(of(mockData));
    component.currentPage = 5;
    component.searchTerm = 'java';

    component.onSearch();

    expect(component.currentPage).toBe(0);
    expect(trainingServiceSpy.getAllTrainings).toHaveBeenCalledWith(0, 3, 'java');
  });

  it('should change page', () => {
    const mockData = { content: [], totalPages: 0, totalElements: 0 };
    trainingServiceSpy.getAllTrainings.and.returnValue(of(mockData));

    component.changePage(2);

    expect(component.currentPage).toBe(2);
    expect(trainingServiceSpy.getAllTrainings).toHaveBeenCalledWith(2, 3, '');
  });
});
