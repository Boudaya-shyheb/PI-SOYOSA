import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GamesRoutingModule } from './games-routing.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { GamesHomeComponent } from './pages/games-home/games-home.component';
import { QuizComponent } from './pages/quiz/quiz.component';
import { WordScrambleComponent } from './pages/word-scramble/word-scramble.component';
import { CrosswordComponent } from './pages/crossword/crossword.component';
@NgModule({
    declarations: [
        GamesHomeComponent,
        QuizComponent,
        WordScrambleComponent,
        CrosswordComponent
    ],
    imports: [
        CommonModule,
        GamesRoutingModule,
        FormsModule,
        ReactiveFormsModule
    ]
})
export class GamesModule { }
