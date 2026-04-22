import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

import { ChatRoutingModule } from './chat-routing.module';
import { ChatListComponent } from './components/chat-list/chat-list.component';
import { ChatWindowComponent } from './components/chat-window/chat-window.component';
import { MessageItemComponent } from './components/message-item/message-item.component';
import { CreateGroupDialogComponent } from './components/create-group-dialog/create-group-dialog.component';



@NgModule({
  declarations: [
    ChatListComponent,
    ChatWindowComponent,
    MessageItemComponent,
    CreateGroupDialogComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    HttpClientModule,
    ChatRoutingModule
  ],
  providers: []
})
export class ChatModule { }