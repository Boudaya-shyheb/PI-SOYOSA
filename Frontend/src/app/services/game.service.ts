import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Game, GameType } from '../models/game.model';
import { environment } from '../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class GameService {
    private apiUrl = `${environment.gameApiUrl}/games`;

    constructor(private http: HttpClient) { }

    getGamesByType(type: GameType): Observable<Game[]> {
        return this.http.get<Game[]>(`${this.apiUrl}/type/${type}`);
    }

    getGamesByTypeAndLevel(type: GameType, level: string): Observable<Game[]> {
        return this.http.get<Game[]>(`${this.apiUrl}/type/${type}?level=${level}`);
    }

    getGameById(id: number): Observable<Game> {
        return this.http.get<Game>(`${this.apiUrl}/${id}`);
    }
}
