import { Component, OnInit, OnDestroy, NgZone, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AiTutorService, ChatMessage } from '../../services/ai-tutor.service';

declare var webkitSpeechRecognition: any;

interface DisplayMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

@Component({
  selector: 'app-practice-english',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './practice-english.component.html',
  styleUrls: ['./practice-english.component.css']
})
export class PracticeEnglishComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('chatContainer') chatContainer!: ElementRef;

  recognition: any;
  isListening = false;
  isThinking = false;
  isSpeaking = false;
  isSupported = true;
  textInput = '';
  feedbackStatus = 'Ready to practice! Speak or type below.';

  displayMessages: DisplayMessage[] = [];
  chatHistory: ChatMessage[] = [];

  private shouldScrollToBottom = false;

  constructor(private aiTutorService: AiTutorService, private ngZone: NgZone) {}

  ngOnInit(): void {
    // Greeting message
    this.displayMessages.push({
      role: 'assistant',
      content: "Hello! I'm your AI English tutor powered by Mistral. You can speak to me using the microphone button, or type your message below. Let's practice English together! What would you like to talk about?",
      timestamp: new Date()
    });

    if ('webkitSpeechRecognition' in window) {
      this.recognition = new webkitSpeechRecognition();
      this.recognition.continuous = false;
      this.recognition.lang = 'en-US';

      this.recognition.onstart = () => {
        this.ngZone.run(() => {
          this.isListening = true;
          this.feedbackStatus = '🎤 Listening... Speak now!';
        });
      };

      this.recognition.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        this.ngZone.run(() => {
          this.sendMessage(transcript);
        });
      };

      this.recognition.onerror = (event: any) => {
        this.ngZone.run(() => {
          this.isListening = false;
          if (event.error === 'not-allowed' || event.error === 'service-not-allowed') {
            this.feedbackStatus = '🚫 Microphone access denied. Please allow microphone access.';
          } else if (event.error === 'network') {
            this.feedbackStatus = '⚠️ Network error during speech recognition.';
          } else if (event.error === 'no-speech') {
            this.feedbackStatus = '🔇 No speech detected. Click the microphone and try again.';
          } else {
            this.feedbackStatus = `Speech error: ${event.error}`;
          }
        });
      };

      this.recognition.onend = () => {
        this.ngZone.run(() => {
          this.isListening = false;
        });
      };
    } else {
      this.isSupported = false;
      this.feedbackStatus = '⚠️ Voice recognition is not supported in this browser. Please use Chrome or Edge.';
    }
  }

  ngAfterViewChecked(): void {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  toggleListen(): void {
    if (!this.isSupported || this.isThinking) {
      return;
    }
    if (this.isListening) {
      this.recognition.stop();
    } else {
      this.recognition.start();
    }
  }

  sendTextMessage(): void {
    const text = this.textInput.trim();
    if (!text || this.isThinking) {
      return;
    }
    this.textInput = '';
    this.sendMessage(text);
  }

  sendMessage(content: string): void {
    if (!content.trim() || this.isThinking) {
      return;
    }

    // Stop listening if active
    if (this.isListening && this.recognition) {
      this.recognition.stop();
    }

    // Add user message to display
    const userDisplay: DisplayMessage = { role: 'user', content, timestamp: new Date() };
    this.displayMessages.push(userDisplay);

    // Add to chat history for context
    const userMsg: ChatMessage = { role: 'user', content };
    this.chatHistory.push(userMsg);

    this.isThinking = true;
    this.feedbackStatus = '🤔 Mistral is thinking...';
    this.shouldScrollToBottom = true;

    this.aiTutorService.sendMessage(this.chatHistory).subscribe({
      next: response => {
        const aiMsg: ChatMessage | undefined = response?.choices?.[0]?.message;
        const aiContent = aiMsg?.content || response?.reply;

        if (!aiContent) {
          this.ngZone.run(() => {
            this.isThinking = false;
            this.feedbackStatus = '⚠️ Empty response received. Please try again.';
          });
          return;
        }

        this.ngZone.run(() => {
          // Add AI reply to display and history
          this.displayMessages.push({ role: 'assistant', content: aiContent, timestamp: new Date() });
          this.chatHistory.push({ role: 'assistant', content: aiContent });

          this.isThinking = false;
          this.feedbackStatus = '🔊 Speaking...';
          this.shouldScrollToBottom = true;
          this.speak(aiContent);
        });
      },
      error: err => {
        const errMsg = err?.error?.error || err?.message || 'Service unavailable. Is the Spring Boot server running?';
        this.ngZone.run(() => {
          this.isThinking = false;
          this.feedbackStatus = `❌ ${errMsg}`;
          this.displayMessages.push({
            role: 'assistant',
            content: `⚠️ Connection error: ${errMsg}`,
            timestamp: new Date()
          });
          this.shouldScrollToBottom = true;
        });
        console.error('AI tutor error:', err);
      }
    });
  }

  speak(text: string): void {
    if (!('speechSynthesis' in window)) {
      this.feedbackStatus = '✅ Response received! (Speech synthesis not available.)';
      return;
    }

    speechSynthesis.cancel();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    utterance.rate = 0.95;
    utterance.pitch = 1.05;

    // Try to pick a natural English voice
    const voices = speechSynthesis.getVoices();
    const preferred = voices.find(v =>
      v.lang.startsWith('en') && (v.name.includes('Google') || v.name.includes('Microsoft') || v.name.includes('Samantha'))
    );
    if (preferred) {
      utterance.voice = preferred;
    }

    utterance.onstart = () => {
      this.ngZone.run(() => {
        this.isSpeaking = true;
        this.feedbackStatus = '🔊 Mistral is speaking...';
      });
    };
    utterance.onend = () => {
      this.ngZone.run(() => {
        this.isSpeaking = false;
        this.feedbackStatus = "✅ Done! It's your turn — speak or type.";
      });
    };
    utterance.onerror = (event) => {
      this.ngZone.run(() => {
        this.isSpeaking = false;
        this.feedbackStatus = '✅ Response received. (Speech output error — check console.)';
      });
      console.error('Speech synthesis error:', event);
    };

    speechSynthesis.speak(utterance);
  }

  stopSpeaking(): void {
    speechSynthesis.cancel();
    this.isSpeaking = false;
    this.feedbackStatus = "🛑 Stopped. Your turn!";
  }

  clearChat(): void {
    speechSynthesis.cancel();
    this.displayMessages = [{
      role: 'assistant',
      content: "Chat cleared! Let's start fresh. What would you like to practice?",
      timestamp: new Date()
    }];
    this.chatHistory = [];
    this.isSpeaking = false;
    this.isThinking = false;
    this.feedbackStatus = 'Ready to practice! Speak or type below.';
    this.shouldScrollToBottom = true;
  }

  private scrollToBottom(): void {
    try {
      const el = this.chatContainer?.nativeElement;
      if (el) {
        el.scrollTop = el.scrollHeight;
      }
    } catch {}
  }

  ngOnDestroy(): void {
    if (this.recognition) {
      this.recognition.stop();
    }
    speechSynthesis.cancel();
  }
}
