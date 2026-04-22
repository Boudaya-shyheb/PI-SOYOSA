import { Component, OnInit, OnDestroy } from '@angular/core';
import { AiTutorService, ChatMessage } from '../../services/ai-tutor.service';

@Component({
  selector: 'app-ai-tutor',
  templateUrl: './ai-tutor.component.html',
  styleUrls: ['./ai-tutor.component.css']
})
export class AiTutorComponent implements OnInit, OnDestroy {
  messages: ChatMessage[] = [];
  isRecording = false;
  userInput = '';
  private recognition: any;

  constructor(private aiTutorService: AiTutorService) {
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    if (SpeechRecognition) {
      this.recognition = new SpeechRecognition();
      this.recognition.continuous = false;
      this.recognition.lang = 'en-US';
      this.recognition.interimResults = false;
      this.recognition.maxAlternatives = 1;

      this.recognition.onresult = (event: any) => {
        this.userInput = event.results[0][0].transcript;
        this.sendMessage();
      };

      this.recognition.onend = () => {
        this.isRecording = false;
      };
    }
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    if (this.recognition) {
      this.recognition.stop();
    }
  }

  toggleVoiceRecognition(): void {
    if (this.isRecording) {
      this.recognition.stop();
      this.isRecording = false;
    } else {
      this.recognition.start();
      this.isRecording = true;
    }
  }

  sendMessage(): void {
    if (!this.userInput.trim()) {
      return;
    }

    const userMessage: ChatMessage = { role: 'user', content: this.userInput };
    this.messages.push(userMessage);
    this.userInput = '';

    this.aiTutorService.sendMessage(this.messages).subscribe(response => {
      const aiMessage: ChatMessage = response.choices[0].message;
      this.messages.push(aiMessage);
      this.speak(aiMessage.content);
    });
  }

  speak(text: string): void {
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    speechSynthesis.speak(utterance);
  }
}
