import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { COURSE_ASSISTANT_API_URL } from './api.config';
import { ApiHeadersService } from './api-headers.service';

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

@Injectable({
  providedIn: 'root'
})
export class AiTutorService {
  private readonly baseUrl = (window as any).__env?.AI_TUTOR_URL || COURSE_ASSISTANT_API_URL;

  constructor(private http: HttpClient, private apiHeaders: ApiHeadersService) { }

  sendMessage(messages: ChatMessage[]): Observable<any> {
    const headers = this.apiHeaders.buildHeaders().set('Content-Type', 'application/json');
    return this.http.post<{ reply?: string; choices?: Array<{ message?: ChatMessage }> }>(this.baseUrl, { messages }, { headers }).pipe(
      map(response => {
        if (response?.choices?.length) {
          return response;
        }

        return {
          choices: [
            {
              message: {
                role: 'assistant',
                content: response?.reply || 'Sorry, I could not generate a reply.'
              }
            }
          ]
        };
      })
    );
  }
}
