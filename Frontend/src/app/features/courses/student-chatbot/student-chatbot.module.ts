import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StudentChatbotComponent } from './student-chatbot.component';

@NgModule({
  declarations: [StudentChatbotComponent],
  imports: [
    CommonModule,
    FormsModule
  ],
  exports: [StudentChatbotComponent]
})
export class StudentChatbotModule { }
