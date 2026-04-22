import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChatListComponent } from './components/chat-list/chat-list.component';
import { ChatWindowComponent } from './components/chat-window/chat-window.component';
import { CreateGroupDialogComponent } from './components/create-group-dialog/create-group-dialog.component';


const routes: Routes = [
  {
    path: '',
    component: ChatListComponent,
  },
  {
    path: 'create-group',
    component: CreateGroupDialogComponent
  },
  {
    path: ':id',
    component: ChatWindowComponent
  }

];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ChatRoutingModule { }