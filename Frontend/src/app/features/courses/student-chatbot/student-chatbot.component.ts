import { Component, OnInit } from '@angular/core';

interface Message {
  text: string;
  sender: 'bot' | 'user';
}

@Component({
  selector: 'app-student-chatbot',
  templateUrl: './student-chatbot.component.html',
  styleUrls: ['./student-chatbot.component.css']
})
export class StudentChatbotComponent implements OnInit {
  messages: Message[] = [];
  userInput = '';

  constructor() { }

  ngOnInit(): void {
    this.messages.push({ text: 'Hello! How can I help you with our courses today?', sender: 'bot' });
  }

  sendMessage(): void {
    if (this.userInput.trim()) {
      this.messages.push({ text: this.userInput, sender: 'user' });
      this.getBotResponse(this.userInput);
      this.userInput = '';
    }
  }

  getBotResponse(question: string): void {
    setTimeout(() => {
      const lowerCaseQuestion = question.toLowerCase();
      let response = "I'm not sure how to answer that. Can you try asking another way?";

      if (lowerCaseQuestion.includes('price') || lowerCaseQuestion.includes('cost')) {
        response = 'Our courses have different prices. You can see the price on each course card. We have both free and paid courses.';
      } else if (lowerCaseQuestion.includes('level')) {
        response = 'We offer courses for all levels, from A1 (Beginner) to C2 (Proficient). You can filter by level on this page.';
      } else if (lowerCaseQuestion.includes('how to enroll') || lowerCaseQuestion.includes('buy')) {
        response = 'To enroll in a course, simply click the "Enroll" or "Buy Now" button on the course you are interested in.';
      } else if (lowerCaseQuestion.includes('hello') || lowerCaseQuestion.includes('hi')) {
        response = 'Hello there! How can I assist you?';
      }

      this.messages.push({ text: response, sender: 'bot' });
    }, 500);
  }
}
