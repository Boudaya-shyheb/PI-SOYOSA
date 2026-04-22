import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AiVoiceChatComponent } from './ai-voice-chat.component';

@NgModule({
  declarations: [AiVoiceChatComponent],
  imports: [
    CommonModule
  ],
  exports: [AiVoiceChatComponent]
})
export class AiVoiceChatModule { }
