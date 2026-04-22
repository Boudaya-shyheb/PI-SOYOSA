import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { map } from 'rxjs/operators';
import { Client, IMessage } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import {
    Conversation,
    Message,
    CreateGroupConversationRequest,
    UpdateMessageRequest,
    AddGroupMemberRequest,
    TypingEvent
} from '../models/chat.model';
import { AuthService } from './auth.service';
import { environment } from '../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ChatService {

    // TODO: Chat service backend not yet implemented (would be port 8083)
    private apiUrl = `${environment.chatApiUrl}/chat`;
    private wsUrl = environment.webSocketUrl;

    private conversationsSubject = new BehaviorSubject<Conversation[]>([]);
    public conversations$ = this.conversationsSubject.asObservable();

    private currentConversationSubject = new BehaviorSubject<Conversation | null>(null);
    public currentConversation$ = this.currentConversationSubject.asObservable();

    private messagesSubject = new BehaviorSubject<Message[]>([]);
    messages$ = this.messagesSubject.asObservable(); currentMessages: Message[] = [];
    private wsConnected = new BehaviorSubject<boolean>(false);
    public wsConnected$ = this.wsConnected.asObservable();

    private messageReceivedSubject = new Subject<Message>();
    public messageReceived$ = this.messageReceivedSubject.asObservable();

    private typingSubject = new Subject<TypingEvent>();
    public typing$ = this.typingSubject.asObservable();

    private onlineUsersSubject = new BehaviorSubject<string[]>([]);
    public onlineUsers$ = this.onlineUsersSubject.asObservable();

    private subscriptions = new Map<number, any[]>();

    private stompClient: Client | null = null;

    constructor(
        private http: HttpClient,
        private authService: AuthService
    ) {
        this.initializeWebSocket();
    }

    getUserConversations() {
        const token = this.authService.getToken();
        const headers = new HttpHeaders({
            Authorization: `Bearer ${token}`
        });

        return this.http.get<Conversation[]>(`${this.apiUrl}/conversations`, { headers });
    }

    createGroupConversation(name: string, memberIds: number[]): Observable<Conversation> {
        const creatorUserId = Number(this.authService.getUserId());
        const request: CreateGroupConversationRequest = { name, creatorUserId, memberIds };
        return this.http.post<Conversation>(`${this.apiUrl}/conversations/group`, request);
    }

    getConversation(conversationId: number): Observable<Conversation> {
        return this.http.get<Conversation>(`${this.apiUrl}/${conversationId}`).pipe(
            map(conversation => {
                this.currentConversationSubject.next(conversation);
                return conversation;
            })
        );
    }

    deleteConversation(conversationId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${conversationId}`);
    }

    renameGroup(conversationId: number, newName: string): Observable<Conversation> {
        return this.http.patch<Conversation>(
            `${this.apiUrl}/${conversationId}/rename`,
            { name: newName }
        );
    }

    addGroupMember(conversationId: number, userId: number): Observable<Conversation> {
        const request: AddGroupMemberRequest = { userId };
        return this.http.post<Conversation>(
            `${this.apiUrl}/${conversationId}/members`,
            request
        );
    }

    removeGroupMember(conversationId: number, userId: number): Observable<void> {
        return this.http.delete<void>(
            `${this.apiUrl}/conversations/${conversationId}/members/${userId}`
        );
    }

    getMessages(conversationId: number, pageSize: number = 50): Observable<Message[]> {
        return this.http
            .get<Message[]>(`${this.apiUrl}/${conversationId}/messages`, {
                params: { pageSize: pageSize.toString() }
            })
            .pipe(
                map(messages => {
                    this.messagesSubject.next(messages);
                    return messages;
                })
            );
    }

    sendMessage(conversationId: number, content: string): Observable<Message> {
        return this.http.post<Message>(
            `${this.apiUrl}/messages`,
            { conversationId, content }
        );
    }


    deleteMessage(messageId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/messages/${messageId}`);
    }

    editMessage(messageId: number, content: string): Observable<Message> {
        const request: UpdateMessageRequest = { content };
        return this.http.patch<Message>(`${this.apiUrl}/messages/${messageId}`, request);
    }


    sendTypingEvent(conversationId: number): void {
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.publish({
                destination: `/app/typing/${conversationId}`,
                body: JSON.stringify({})
            });
        }
    }

    private initializeWebSocket(): void {
        const token = this.authService.getToken();

        if (!token) {
            console.warn('No token available for WebSocket connection');
            return;
        }

        this.stompClient = new Client({
            webSocketFactory: () => new SockJS(`${this.wsUrl}?token=${token}`),
            reconnectDelay: 3000,
            debug: (str) => console.log('STOMP:', str)
        });

        this.stompClient.onConnect = () => {
            console.log('STOMP connected');
            this.wsConnected.next(true);

            if (this.stompClient) {
                this.stompClient.subscribe('/topic/presence', (message: IMessage) => {
                    const onlineUsers: string[] = JSON.parse(message.body);
                    this.onlineUsersSubject.next(onlineUsers);
                });
            }
        };

        this.stompClient.onDisconnect = () => {
            console.log('STOMP disconnected');
            this.wsConnected.next(false);
        };

        this.stompClient.onStompError = (frame) => {
            console.error('Broker error:', frame.headers['message']);
            console.error('Details:', frame.body);
        };

        this.stompClient.activate();
    }

    subscribeToConversation(conversationId: number): void {

        const trySubscribe = () => {
            if (!this.stompClient || !this.stompClient.connected) {
                setTimeout(trySubscribe, 500);
                return;
            }

            const subscription = this.stompClient.subscribe(
                `/topic/conversation/${conversationId}`,
                (message: IMessage) => {

                    const receivedMessage: Message = JSON.parse(message.body);

                    console.log("🔥 WS RECEIVED:", receivedMessage);

                    this.updateLocalMessages(receivedMessage);
                }
            );

            const typingSubscription = this.stompClient.subscribe(
                `/topic/typing/${conversationId}`,
                (msg: IMessage) => {
                    const typingEvent: TypingEvent = JSON.parse(msg.body);
                    this.typingSubject.next(typingEvent);
                }
            );

            this.subscriptions.set(conversationId, [subscription, typingSubscription]);
        };

        trySubscribe();
    }

    unsubscribeFromConversation(conversationId: number): void {
        const subs = this.subscriptions.get(conversationId);

        if (subs && subs.length > 0) {
            subs.forEach(s => s.unsubscribe());
            this.subscriptions.delete(conversationId);
            console.log(`Unsubscribed from conversation ${conversationId}`);
        }
    }

    disconnectWebSocket(): void {
        if (this.stompClient) {
            this.stompClient.deactivate();
            this.stompClient = null;
        }
    }

    public updateLocalMessages(newMessage: Message): void {
        const currentMessages = this.messagesSubject.getValue() || [];
        if (newMessage.isDeleted) {
            this.messagesSubject.next(
                currentMessages.filter(m => m.id !== newMessage.id)
            );
            return;
        }
        const updated = currentMessages.map(m => {
            if (String(m.id) === String(newMessage.id)) {
                return { ...newMessage }; // 🔥 EDIT
            }
            return m;
        });
        const exists = currentMessages.some(m => String(m.id) === String(newMessage.id));

        if (!exists) {
            updated.push(newMessage);
        }
        this.messagesSubject.next([...updated]);
    }

    setCurrentConversation(conversation: Conversation): void {
        this.currentConversationSubject.next(conversation);
    }

    getConversationsSync(): Conversation[] {
        return this.conversationsSubject.getValue();
    }

    getMessagesSync(): Message[] {
        return this.messagesSubject.getValue();
    }
}
