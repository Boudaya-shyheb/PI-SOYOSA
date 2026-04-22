import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { GameService } from '../../../../services/game.service';
import { ScoreService } from '../../../../services/score.service';
import { Game, GameType } from '../../../../models/game.model';

@Component({
  selector: 'app-word-scramble',
  templateUrl: './word-scramble.component.html',
  styleUrls: ['./word-scramble.component.css']
})
export class WordScrambleComponent implements OnInit {

  game: Game | null = null;
  loading = true;

  selectedLevel = 'EASY';

  currentIndex = 0;
  scrambledLetters: string[] = [];
  userAnswer = '';
  score = 0;
  gameFinished = false;

  feedbackMsg = '';
  isCorrect = false;

  constructor(
      private route: ActivatedRoute,
      private gameService: GameService,
      private scoreService: ScoreService
  ) {}

  ngOnInit(): void {
    this.selectedLevel =
        this.route.snapshot.queryParamMap.get('level') || 'EASY';

    this.loadGame();
  }

  loadGame(): void {
    this.gameService
        .getGamesByTypeAndLevel(GameType.WORD_SCRAMBLE, this.selectedLevel)
        .subscribe({
          next: (games) => {
            if (games && games.length > 0) {
              this.game = games[0];
              this.generateScramble();
            }
            this.loading = false;
          },
          error: () => this.loading = false
        });
  }

  get currentWord(): string {
    return this.game?.contents?.[this.currentIndex]?.word?.toUpperCase() || '';
  }

  get totalWords(): number {
    return this.game?.contents?.length || 0;
  }

  generateScramble(): void {
    if (!this.currentWord) {
      this.scrambledLetters = [];
      return;
    }

    const letters = this.currentWord.split('');

    for (let i = letters.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [letters[i], letters[j]] = [letters[j], letters[i]];
    }

    this.scrambledLetters = letters;
  }

  checkAnswer(): void {
    if (!this.userAnswer) return;

    if (this.userAnswer.trim().toUpperCase() === this.currentWord) {
      this.isCorrect = true;
      this.feedbackMsg = 'Correct!';
      this.score++;

      setTimeout(() => this.nextWord(), 1000);
    } else {
      this.isCorrect = false;
      this.feedbackMsg = 'Try again!';
    }
  }

  nextWord(): void {
    this.userAnswer = '';
    this.feedbackMsg = '';

    if (this.currentIndex === this.totalWords - 1) {
      this.finishGame();
    } else {
      this.currentIndex++;
      this.generateScramble();
    }
  }

  finishGame(): void {
    this.gameFinished = true;

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
}