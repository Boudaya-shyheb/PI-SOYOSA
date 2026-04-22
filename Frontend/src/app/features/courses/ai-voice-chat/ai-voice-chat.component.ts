import { Component, OnInit, OnDestroy } from '@angular/core';
import { AiTutorService, ChatMessage } from '../../../services/ai-tutor.service';

declare var webkitSpeechRecognition: any;

@Component({
  selector: 'app-ai-voice-chat',
  templateUrl: './ai-voice-chat.component.html',
  styleUrls: ['./ai-voice-chat.component.css']
})
export class AiVoiceChatComponent implements OnInit, OnDestroy {
  recognition: any;
  isListening = false;
  feedback = 'Click the button and speak.';
  isSupported = true;
  messages: ChatMessage[] = [];

  constructor(private aiTutorService: AiTutorService) { }

  ngOnInit(): void {
    if ('webkitSpeechRecognition' in window) {
      this.recognition = new webkitSpeechRecognition();
      this.recognition.continuous = false;
      this.recognition.lang = 'en-US';

      this.recognition.onstart = () => {
        this.isListening = true;
        this.feedback = 'Listening...';
      };

      this.recognition.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        this.feedback = `You said: "${transcript}". Contacting AI...`;
        const userMessage: ChatMessage = { role: 'user', content: transcript };
        this.messages.push(userMessage);
        this.aiTutorService.sendMessage(this.messages).subscribe({
          next: response => {
            const aiMessage: ChatMessage = response.choices[0].message;
            this.messages.push(aiMessage);
            this.feedback = 'AI responded. Now speaking...';
            this.speak(aiMessage.content);
          },
          error: err => {
            this.feedback = 'Error from AI service. Check console for details.';
            console.error('Error sending message to AI tutor service:', err);
          }
        });
      };

      this.recognition.onerror = (event: any) => {
        if (event.error === 'not-allowed' || event.error === 'service-not-allowed') {
          this.feedback = 'Microphone access denied. Please allow microphone access in your browser settings and try again.';
        } else if (event.error === 'network') {
            this.feedback = 'Network error. Please ensure you have an internet connection and that microphone access is not blocked by your network or browser.';
        } 
        else {
          this.feedback = 'Error occurred in recognition: ' + event.error;
        }
      };

      this.recognition.onend = () => {
        this.isListening = false;
      };
    } else {
      this.isSupported = false;
      this.feedback = 'Speech recognition not supported in this browser. Please try Chrome or Edge.';
    }
  }

  speak(text: string): void {
    if ('speechSynthesis' in window) {
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'en-US';
      utterance.onstart = () => {
        this.feedback = 'Speaking...';
      };
      utterance.onend = () => {
        this.feedback = 'Finished speaking. Click the button to speak again.';
      };
      utterance.onerror = (event) => {
        this.feedback = 'Error occurred during speech synthesis. Check console.';
        console.error('Speech synthesis error:', event);
      };
      speechSynthesis.speak(utterance);
    } else {
      this.feedback = 'Speech synthesis not supported in this browser.';
    }
  }

  toggleListen(): void {
    if (!this.isSupported) {
        return;
    }
    if (this.isListening) {
      this.recognition.stop();
    } else {
      this.recognition.start();
    }
  }

  ngOnDestroy(): void {
    if (this.recognition) {
      this.recognition.stop();
    }
  }
}
