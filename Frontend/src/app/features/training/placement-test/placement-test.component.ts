import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TrainingService } from '../../../services/training.service';

@Component({
  selector: 'app-placement-test',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './placement-test.component.html',
  styleUrls: ['./placement-test.component.css']
})
export class PlacementTestComponent implements OnInit {
  questions: any[] = [];
  currentIndex: number = 0;
  answers: Map<number, number> = new Map();
  isFinished: boolean = false;
  result: any = null;
  loading: boolean = true;

  @Output() testCompleted = new EventEmitter<any>();

  constructor(private trainingService: TrainingService) {}

  ngOnInit(): void {
    this.loadQuestions();
  }

  loadQuestions(): void {
    this.loading = true;
    this.trainingService.getPlacementQuestions().subscribe({
      next: (qs) => {
        this.questions = qs;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  selectOption(optionIndex: number): void {
    const questionId = this.questions[this.currentIndex].id;
    this.answers.set(questionId, optionIndex);
    
    if (this.currentIndex < this.questions.length - 1) {
      this.currentIndex++;
    } else {
      this.submit();
    }
  }

  submit(): void {
    const submission: any = {};
    this.answers.forEach((val, key) => submission[key] = val);
    
    this.loading = true;
    this.trainingService.submitPlacementTest(submission).subscribe({
      next: (res) => {
        this.result = res;
        this.isFinished = true;
        this.loading = false;
        this.testCompleted.emit(res);
      },
      error: () => this.loading = false
    });
  }
}
