import {
  Component,
  OnInit,
  OnDestroy,
  ViewChild,
  ElementRef,
  AfterViewInit,
  AfterViewChecked
} from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Conversation, Message, TypingEvent } from '../../../../models/chat.model';
import { ChatService } from '../../../../services/chat.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-chat-window',
  templateUrl: './chat-window.component.html',
  styleUrls: ['./chat-window.component.css']
})
export class ChatWindowComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('messagesContainer') messagesContainer!: ElementRef;

  conversation: Conversation | null = null;

  messages: Message[] = [];

  newMessageContent = '';
  loading = true;
  sendingMessage = false;
  editingMessageId: number | null = null;
  editingMessageContent = '';
  currentUserEmail: string | null = null;
  canCreateGroup = false;

  private conversationId: number | null = null;
  private destroy$ = new Subject<void>();
  isTyping = false;
  typingUser = '';
  private typingTimeout: any;
  private lastTypingTime = 0;
  private observer: IntersectionObserver | null = null;

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {

    // SUBSCRIBE AUX MESSAGES (TEMPS RÉEL)
    this.chatService.messages$
      .pipe(takeUntil(this.destroy$))
      .subscribe((messages: Message[]) => {
        const isInitialLoad = this.messages.length === 0 && messages.length > 0;
        this.messages = messages;

        setTimeout(() => {
          if (isInitialLoad) {
            this.scrollToBottom();
          } else {
            this.scrollToBottomIfNeeded();
          }
        }, 50);
      });

    this.chatService.typing$
      .pipe(takeUntil(this.destroy$))
      .subscribe((event: TypingEvent) => {
        if (event.username !== this.currentUserEmail) {
          this.isTyping = true;
          this.typingUser = event.username;
          clearTimeout(this.typingTimeout);
          this.typingTimeout = setTimeout(() => this.isTyping = false, 3000);
        }
      });

    this.currentUserEmail = this.authService.getUserEmail();
    const userRole = this.authService.getUserRole();
    this.canCreateGroup = userRole === 'TUTOR' || userRole === 'ADMIN';

    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const id = params['id'];

      if (id) {
        this.conversationId = parseInt(id, 10);

        this.loadConversation();

        this.chatService.getMessages(this.conversationId).subscribe({
          next: () => this.loading = false,
          error: () => this.loading = false
        });

        this.chatService.subscribeToConversation(this.conversationId);
      }
    });
  }

  loadConversation(): void {
    if (!this.conversationId) return;

    this.chatService
      .getConversation(this.conversationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (conversation) => {
          this.conversation = conversation;
        },
        error: (error) => {
          console.error('Error loading conversation:', error);
        }
      });
  }

  sendMessage(): void {
    if (!this.newMessageContent.trim() || !this.conversationId || this.sendingMessage) {
      return;
    }

    this.sendingMessage = true;
    const content = this.newMessageContent.trim();
    this.newMessageContent = '';

    this.chatService
      .sendMessage(this.conversationId, content)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.sendingMessage = false;
        },
        error: (error) => {
          console.error('Error sending message:', error);
          this.newMessageContent = content;
          this.sendingMessage = false;
          alert('Failed to send message');
        }
      });
  }

  startEditMessage(message: Message): void {
    this.editingMessageId = message.id;
    this.editingMessageContent = message.content;
  }

  cancelEditMessage(): void {
    this.editingMessageId = null;
    this.editingMessageContent = '';
  }

  saveEditMessage(messageId: number): void {
    if (!this.editingMessageContent.trim()) return;

    this.chatService
      .editMessage(messageId, this.editingMessageContent.trim())
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.cancelEditMessage();
        },
        error: (error) => {
          console.error('Error editing message:', error);
          alert('Failed to edit message');
        }
      });
  }

  deleteMessage(messageId: number): void {
    if (!confirm('Are you sure you want to delete this message?')) return;

    this.chatService
      .deleteMessage(messageId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        error: (error) => {
          console.error('Error deleting message:', error);
          alert('Failed to delete message');
        }
      });
  }

  canManageMessage(message: Message): boolean {
    return message.senderUsername === this.currentUserEmail;
  }

  isOwnMessage(message: Message): boolean {
    return message.senderUsername === this.currentUserEmail;
  }

  ngAfterViewInit(): void {
    const options = {
      root: this.messagesContainer.nativeElement,
      rootMargin: '0px',
      threshold: 0.5
    };

    this.observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          const msgIdStr = entry.target.getAttribute('data-message-id');
          if (msgIdStr) {
            const messageId = parseInt(msgIdStr, 10);
            const message = this.messages.find(m => m.id === messageId);

          }
        }
      });
    }, options);
  }

  private scrollToBottomIfNeeded(): void {
    if (!this.messagesContainer) return;
    const el = this.messagesContainer.nativeElement;
    if (el.scrollHeight - el.scrollTop - el.clientHeight < 150) {
      this.scrollToBottom();
    }
  }

  private scrollToBottom(): void {
    try {
      if (this.messagesContainer) {
        this.messagesContainer.nativeElement.scrollTop =
          this.messagesContainer.nativeElement.scrollHeight;
      }
    } catch (err) {
      console.error('Scroll error:', err);
    }
  }

  onKeyPress(event: KeyboardEvent): void {
    const now = Date.now();
    if (now - this.lastTypingTime > 2000) {
      if (this.conversationId) {
        this.chatService.sendTypingEvent(this.conversationId);
      }
      this.lastTypingTime = now;
    }

    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  ngOnDestroy(): void {
    if (this.conversationId) {
      this.chatService.unsubscribeFromConversation(this.conversationId);
    }
    this.destroy$.next();
    this.destroy$.complete();
  }
}