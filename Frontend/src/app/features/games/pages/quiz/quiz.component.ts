import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GameService } from '../../../../services/game.service';
import { ScoreService } from '../../../../services/score.service';
import { Game, GameType, Score } from '../../../../models/game.model';

@Component({
  selector: 'app-quiz',
  templateUrl: './quiz.component.html',
  styleUrls: ['./quiz.component.css']
})
export class QuizComponent implements OnInit {
  game: Game | null = null;
  loading = true;

  currentQuestionIndex = 0;
  selectedAnswer: string | null = null;
  score = 0;
  quizFinished = false;
  selectedLevel: string = '';

  constructor(
    private route: ActivatedRoute,
    private gameService: GameService,
    private scoreService: ScoreService
  ) { }

  ngOnInit(): void {
    this.selectedLevel = this.route.snapshot.queryParamMap.get('level') || 'EASY';
    this.loadGame();
  }

  loadGame(): void {
    this.gameService.getGamesByTypeAndLevel(GameType.QUIZ, this.selectedLevel).subscribe({
      next: (games) => {
        if (games && games.length > 0) {
          this.game = games[0];
        } else {
          this.game = null;
        }

        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  get currentQuestion() { return this.game?.contents?.[this.currentQuestionIndex]; }
  get totalQuestions() { return this.game?.contents?.length || 0; }
  get isLastQuestion() { return this.currentQuestionIndex === this.totalQuestions - 1; }

  nextQuestion(): void {
    if (this.selectedAnswer === this.currentQuestion?.correctAnswer) {
      this.score++;
    }

    if (this.isLastQuestion) {
      this.finishQuiz();
    } else {
      this.currentQuestionIndex++;
      this.selectedAnswer = null;
    }
  }

  finishQuiz(): void {
    this.quizFinished = true;

    if (this.game) {
      const userStr = localStorage.getItem('user');
      const studentId = userStr ? JSON.parse(userStr).id : 1;

      this.scoreService.submitScore({
        studentId: studentId,
        gameId: this.game.id,
        score: this.score
      }).subscribe({
        next: () => console.log("Score saved ✅"),
        error: (err) => console.error(err)
      });
    }
  }

  getOptions(question: any): string[] {
    if (!question) return [];

    return [
      question.optionA,
      question.optionB,
      question.optionC,
      question.optionD
    ].filter(Boolean);
  }
}
