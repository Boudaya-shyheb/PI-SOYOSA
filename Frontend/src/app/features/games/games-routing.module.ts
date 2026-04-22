import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';

import { GamesHomeComponent } from './pages/games-home/games-home.component';
import { QuizComponent } from './pages/quiz/quiz.component';
import { WordScrambleComponent } from './pages/word-scramble/word-scramble.component';
import { CrosswordComponent } from './pages/crossword/crossword.component';

const routes: Routes = [
    { path: '', component: GamesHomeComponent },
    { path: 'quiz', component: QuizComponent },
    { path: 'word-scramble', component: WordScrambleComponent },
    { path: 'crossword', component: CrosswordComponent }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class GamesRoutingModule { }
