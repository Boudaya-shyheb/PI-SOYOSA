import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Conversation } from '../../../../models/chat.model';
import { ChatService } from '../../../../services/chat.service';
import { AuthService } from '../../../../services/auth.service';

@Component({
  selector: 'app-chat-list',
  templateUrl: './chat-list.component.html',
  styleUrls: ['./chat-list.component.css']
})
export class ChatListComponent implements OnInit, OnDestroy {
  conversations: Conversation[] = [];
  selectedConversationId: number | null = null;
  loading = true;
  canCreateGroup = false;
  searchQuery = '';
  onlineUsers: string[] = [];

  private destroy$ = new Subject<void>();

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadConversations();
    this.checkCanCreateGroup();
    const userRole = this.authService.getUserRole();
    this.canCreateGroup = userRole === 'TUTOR' || userRole === 'ADMIN';

    this.chatService.onlineUsers$
      .pipe(takeUntil(this.destroy$))
      .subscribe((users: string[]) => {
        this.onlineUsers = users;
      });
  }

  checkCanCreateGroup(): void {
    const userRole = this.authService.getUserRole();
    this.canCreateGroup = userRole === 'TUTOR' || userRole === 'ADMIN';
  }

  loadConversations(): void {
    this.loading = true;
    this.chatService
      .getUserConversations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (conversations) => {
          console.log("API RESPONSE:", conversations);

          this.conversations = conversations;
          this.loading = false;
        },
        error: (error) => {
          console.error('[ChatListComponent] Error loading conversations. This might be due to a JWT mismatch (userId claim) or backend microservice port (8083) being unreachable.', error);
          this.loading = false;
        }
      });
  }


  get filteredConversations(): Conversation[] {
    if (!this.searchQuery.trim()) {
      return this.conversations;
    }

    const query = this.searchQuery.toLowerCase();
    return this.conversations.filter(
      conv =>
        conv.name.toLowerCase().includes(query) ||
        conv.members.some(m => m.username.toLowerCase().includes(query))
    );
  }

  selectConversation(conversation: Conversation): void {
    this.selectedConversationId = conversation.id;
    this.chatService.setCurrentConversation(conversation);
    this.router.navigate(['/chat', conversation.id]);
  }

  getConversationName(conversation: Conversation): string {
    if (conversation.isGroup) {
      return conversation.name;
    }

    const userEmail = this.authService.getUserEmail();
    const otherMember = conversation.members.find(m => m.username !== userEmail);
    return otherMember ? otherMember.username : 'Unknown';
  }

  isConversationOnline(conversation: Conversation): boolean {
    if (conversation.isGroup) return false;
    const userEmail = this.authService.getUserEmail();
    const otherMember = conversation.members.find(m => m.username !== userEmail);
    if (otherMember) {
      return this.onlineUsers.includes(otherMember.username);
    }
    return false;
  }

  getLastMessagePreview(conversation: Conversation): string {
    if (!conversation.lastMessage) {
      return 'No messages yet';
    }

    const content = conversation.lastMessage.content;
    return content.length > 50 ? content.substring(0, 50) + '...' : content;
  }

  deleteConversation(conversationId: number, event: Event): void {
    event.stopPropagation();

    if (!confirm('Are you sure you want to delete this conversation?')) {
      return;
    }

    this.chatService.deleteConversation(conversationId).subscribe({
      next: () => {
        this.conversations = this.conversations.filter(c => c.id !== conversationId);
        if (this.selectedConversationId === conversationId) {
          this.selectedConversationId = null;
        }
      },
      error: (error) => {
        console.error('Error deleting conversation:', error);
        alert('Failed to delete conversation');
      }
    });
  }

  openCreateGroupDialog(): void {
    this.router.navigate(['/chat/create-group']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
