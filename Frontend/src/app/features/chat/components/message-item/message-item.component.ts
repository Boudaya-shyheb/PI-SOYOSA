import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { Message } from '../../../../models/chat.model';


@Component({
  selector: 'app-message-item',
  templateUrl: './message-item.component.html',
  styleUrls: ['./message-item.component.css']
})
export class MessageItemComponent implements OnInit {
  @Input() message!: Message;
  @Input() isOwnMessage = false;
  @Input() canManageMessage = false;
  @Input() isEditing = false;
  @Input() editingContent = '';

  @Output() onEdit = new EventEmitter<Message>();
  @Output() onSaveEdit = new EventEmitter<void>();
  @Output() onCancelEdit = new EventEmitter<void>();
  @Output() onDelete = new EventEmitter<void>();
  @Output() onEditContentChange = new EventEmitter<string>();

  showActions = false;

  ngOnInit(): void {}

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${hours}:${minutes}`;
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString();
  }

  isMessageEdited(): boolean {
    return this.message.updatedAt !== this.message.dateSent && !!this.message.updatedAt;
  }

  onEditClick(): void {
    this.onEdit.emit(this.message);
  }

  onSaveEditClick(): void {
    this.onSaveEdit.emit();
  }

  onCancelEditClick(): void {
    this.onCancelEdit.emit();
  }

  onDeleteClick(): void {
    this.onDelete.emit();
  }

  toggleActions(): void {
    if (this.canManageMessage) {
      this.showActions = !this.showActions;
    }
  }

  onContentChange(newContent: string): void {
    this.onEditContentChange.emit(newContent);
  }
}