import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-games-home',
  templateUrl: './games-home.component.html',
  styleUrls: ['./games-home.component.css']
})
export class GamesHomeComponent implements OnInit {

  selectedLevel: string = 'EASY';

  constructor(private router: Router) { }

  ngOnInit(): void { }

  playGame(typeStr: string) {
    if (typeStr === 'QUIZ') {
      this.router.navigate(['/games/quiz'], { queryParams: { level: this.selectedLevel } });
    } else if (typeStr === 'WORD_SCRAMBLE') {
      this.router.navigate(['/games/word-scramble'], { queryParams: { level: this.selectedLevel } });
    } else if (typeStr === 'CROSSWORD') {
      this.router.navigate(['/games/crossword'], { queryParams: { level: this.selectedLevel } });
    }
  }
}
