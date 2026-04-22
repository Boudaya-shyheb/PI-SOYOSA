import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ScoreService } from '../../../../services/score.service';
import { GameService } from '../../../../services/game.service';
import { Game, GameType } from '../../../../models/game.model';
import {TimerService} from "../../../../services/timer.service";

interface Cell {
  r: number;
  c: number;
  ans: string;
  num?: number;
  val: string;
}

@Component({
  selector: 'app-crossword',
  templateUrl: './crossword.component.html',
  styleUrls: ['./crossword.component.css']
})
export class CrosswordComponent implements OnInit {

  cells: Cell[] = [];
  gridRows = 0;
  gridCols = 0;

  gameFinished = false;
  score = 0;
  totalCells = 0;
  selectedLevel = 'EASY';
  game: Game | null = null;

  attemptsLeft = 3;
  gameOver = false;

  timeLeft = 0;
  isTimeUp = false;

  constructor(
      private route: ActivatedRoute,
      private scoreService: ScoreService,
      private gameService: GameService,
      private timerService: TimerService
  ) {}
  ngOnDestroy(): void {
    this.timerService.stop();
  }
  ngOnInit(): void {
    this.selectedLevel =
        this.route.snapshot.queryParamMap.get('level') || 'EASY';

    this.loadGame();
    let duration = 30;

    if (this.selectedLevel === 'MEDIUM') duration = 45;
    if (this.selectedLevel === 'HARD') duration = 60;

    this.timerService.start(duration);

    this.timerService.timeLeft$.subscribe(time => {
      this.timeLeft = time;

      if (time <= 0 && !this.gameFinished) {
        this.isTimeUp = true;
        this.onTimeUp();
      }
    });
  }
  onTimeUp(): void {
    if (this.gameFinished) return; // 🔥 éviter double appel

    this.gameFinished = true;
    this.gameOver = true;

    this.timerService.stop(); // ✅ important

    this.saveScore();
  }
  loadGame(): void {
    this.gameService
        .getGamesByTypeAndLevel(GameType.CROSSWORD, this.selectedLevel)
        .subscribe(games => {
          if (!games || games.length === 0) return;
          this.game = games[0];
          this.buildGrid(this.game.contents || []);
        });
  }

  // 🧠 VALIDATION
  canPlaceVertical(grid: Map<string, any>, word: string, startRow: number, col: number): boolean {
    for (let k = 0; k < word.length; k++) {
      const r = startRow + k;
      const key = `${r},${col}`;
      const existing = grid.get(key);

      if (existing && existing.letter !== word[k]) return false;

      const left = grid.get(`${r},${col - 1}`);
      const right = grid.get(`${r},${col + 1}`);

      if (!existing && (left || right)) return false;
    }

    if (grid.get(`${startRow - 1},${col}`)) return false;
    if (grid.get(`${startRow + word.length},${col}`)) return false;

    return true;
  }

  canPlaceHorizontal(grid: Map<string, any>, word: string, row: number, startCol: number): boolean {
    for (let k = 0; k < word.length; k++) {
      const c = startCol + k;
      const key = `${row},${c}`;
      const existing = grid.get(key);

      if (existing && existing.letter !== word[k]) return false;

      const up = grid.get(`${row - 1},${c}`);
      const down = grid.get(`${row + 1},${c}`);

      if (!existing && (up || down)) return false;
    }

    if (grid.get(`${row},${startCol - 1}`)) return false;
    if (grid.get(`${row},${startCol + word.length}`)) return false;

    return true;
  }

  // 🚀 BUILD GRID FINAL
  buildGrid(contents: any[]): void {
    this.cells = [];
    this.totalCells = 0;

    if (!contents.length) return;

    contents.sort((a, b) => b.answer.length - a.answer.length);

    const grid = new Map<string, any>();

    // 🟩 premier mot
    const firstWord = contents[0].answer.toUpperCase();
    for (let i = 0; i < firstWord.length; i++) {
      grid.set(`0,${i}`, { letter: firstWord[i], num: i === 0 ? 1 : undefined });
    }

    // 🟦 autres mots
    contents.slice(1).forEach((content, index) => {
      const word = content.answer.toUpperCase();
      let placed = false;

      for (let i = 0; i < word.length && !placed; i++) {
        for (const key of grid.keys()) {
          const [r, c] = key.split(',').map(Number);

          if (grid.get(key).letter === word[i]) {
            const startRow = r - i;

            if (this.canPlaceVertical(grid, word, startRow, c)) {
              for (let k = 0; k < word.length; k++) {
                grid.set(`${startRow + k},${c}`, {
                  letter: word[k],
                  num: k === 0 ? index + 2 : undefined
                });
              }
              placed = true;
              break;
            }
          }
        }
      }

      // fallback horizontal propre
      if (!placed) {
        let row = index + 2;

        for (let col = 0; col < 20; col++) {
          if (this.canPlaceHorizontal(grid, word, row, col)) {
            for (let k = 0; k < word.length; k++) {
              grid.set(`${row},${col + k}`, {
                letter: word[k],
                num: k === 0 ? index + 2 : undefined
              });
            }
            break;
          }
        }
      }
    });

    // 🔥 crop automatique
    let minRow = Infinity, maxRow = -Infinity;
    let minCol = Infinity, maxCol = -Infinity;

    grid.forEach((_, key) => {
      const [r, c] = key.split(',').map(Number);
      minRow = Math.min(minRow, r);
      maxRow = Math.max(maxRow, r);
      minCol = Math.min(minCol, c);
      maxCol = Math.max(maxCol, c);
    });

    this.gridRows = maxRow - minRow + 1;
    this.gridCols = maxCol - minCol + 1;

    grid.forEach((cell, key) => {
      const [r, c] = key.split(',').map(Number);

      this.cells.push({
        r: r - minRow + 1,
        c: c - minCol + 1,
        ans: cell.letter,
        num: cell.num,
        val: ''
      });

      this.totalCells++;
    });
  }

  checkAnswers(): void {
    if (this.gameOver) return;

    let currentScore = 0;
    let hasError = false;

    for (const cell of this.cells) {

      if (cell.val === cell.ans) {
        currentScore++;
        (cell as any).locked = true;

      } else {
        if (cell.val) hasError = true;
        cell.val = '';
        (cell as any).locked = false;
      }
    }

    this.score = currentScore;

    // ✅ parfait
    if (currentScore === this.totalCells) {
      this.gameFinished = true;

      this.timerService.stop(); // 🔥 ici
      this.saveScore();
      return;
    }

    // ❌ erreurs → diminuer tentatives
    if (hasError) {
      this.attemptsLeft--;

      // 💥 GAME OVER
      if (this.attemptsLeft <= 0) {
        this.gameOver = true;
        this.gameFinished = true;

        this.timerService.stop(); // 🔥 ici
        this.saveScore();
      }
    }
  }
  saveScore(): void {
    if (!this.game) return;

    const userStr = localStorage.getItem('user');
    const studentId = userStr ? JSON.parse(userStr).id : 1;

    this.scoreService.submitScore({
      studentId,
      gameId: this.game.id,
      score: this.score
    }).subscribe();
  }

}