import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Score } from '../models/game.model';
import { environment } from '../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ScoreService {
    private apiUrl = `${environment.gameApiUrl}/scores`;

    constructor(private http: HttpClient) { }

    submitScore(score: any) {
        return this.http.post(
            `http://localhost:8080/api/scores/game/${score.gameId}`,
            {
                score: score.score,
                studentId: score.studentId
            }
        );
    }

    getLeaderboard(): Observable<Score[]> {
        return this.http.get<Score[]>(`${this.apiUrl}/leaderboard`);
    }
}
