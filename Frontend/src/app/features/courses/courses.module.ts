import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CoursesRoutingModule } from './courses.routing';
import { CourseListComponent } from './course-list.component';
import { SharedModule } from '../../shared/shared.module';
import { AiVoiceChatModule } from './ai-voice-chat/ai-voice-chat.module';
import { StudentChatbotModule } from './student-chatbot/student-chatbot.module';

@NgModule({
  declarations: [CourseListComponent],
  imports: [CommonModule, FormsModule, CoursesRoutingModule, SharedModule, AiVoiceChatModule, StudentChatbotModule]
})
export class CoursesModule {}
