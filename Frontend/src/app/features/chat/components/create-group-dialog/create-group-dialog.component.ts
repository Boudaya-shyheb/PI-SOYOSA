import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ChatService } from '../../../../services/chat.service';
import { AuthService } from '../../../../services/auth.service';
import { UserService, UserDto } from '../../../../services/user.service';

@Component({
  selector: 'app-create-group-dialog',
  templateUrl: './create-group-dialog.component.html',
  styleUrls: ['./create-group-dialog.component.css']
})
export class CreateGroupDialogComponent implements OnInit, OnDestroy {
  groupName = '';
  selectedMemberIds: number[] = [];
  allUsers: UserDto[] = [];
  loading = false;
  creating = false;
  errorMessage = '';
  canCreateGroup = false;

  private destroy$ = new Subject<void>();

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private userService: UserService,
    private router: Router
  ) { }

  ngOnInit(): void {
    const userRole = this.authService.getUserRole();
    this.canCreateGroup = userRole === 'TUTOR' || userRole === 'ADMIN';

    if (!this.canCreateGroup) {
      this.errorMessage = 'You do not have permission to create groups';
      return;
    }

    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    const currentUserId = Number(this.authService.getUserId());

    this.userService.getAllUsers()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (users: UserDto[]) => {
          this.allUsers = users.filter((u: UserDto) => u.userId !== currentUserId);
          this.loading = false;
        },
        error: (err: any) => {
          console.error('Failed to load users', err);
          this.errorMessage = 'Failed to load users';
          this.loading = false;
        }
      });
  }

  toggleUserSelection(userId: number): void {
    const index = this.selectedMemberIds.indexOf(userId);
    if (index > -1) {
      this.selectedMemberIds.splice(index, 1);
    } else {
      this.selectedMemberIds.push(userId);
    }
  }

  isUserSelected(userId: number): boolean {
    return this.selectedMemberIds.includes(userId);
  }

  createGroup(): void {
    this.errorMessage = '';

    if (!this.groupName.trim()) {
      this.errorMessage = 'Group name is required';
      return;
    }

    if (this.selectedMemberIds.length === 0) {
      this.errorMessage = 'Select at least one member';
      return;
    }

    this.creating = true;

    this.chatService
      .createGroupConversation(this.groupName.trim(), this.selectedMemberIds)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (conversation) => {
          this.router.navigate(['/chat', conversation.id]);
        },
        error: (error) => {
          console.error('Error creating group:', error);
          this.errorMessage = error.error?.message || 'Failed to create group';
          this.creating = false;
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/chat']);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
