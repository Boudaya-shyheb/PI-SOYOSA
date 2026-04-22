export enum GameType {
    QUIZ = 'QUIZ',
    CROSSWORD = 'CROSSWORD',
    WORD_SCRAMBLE = 'WORD_SCRAMBLE'
}

export interface GameContent {
    id?: number;
    // QUIZ
    question?: string;
    optionA?: string;
    optionB?: string;
    optionC?: string;
    optionD?: string;
    correctAnswer?: string;
    // WORD SCRAMBLE
    word?: string;

    // CROSSWORD
    clue?: string;
    answer?: string;

    level: string;
}

export interface Score {
    id?: number;
    studentId: string;
    gameId: number;
    score: number;
    playedAt: string;
}

export interface Game {
    id: number;
    title: string;
    type: GameType;
    level: string;
    createdAt: string;
    contents: GameContent[];
}
