export interface Message {
    id: number;
    content: string;
    dateSent: string;
    messageType: MessageType;
    senderId: number;
    senderUsername: string;
    messageRoomId: number;
    updatedAt?: string;
    isDeleted: boolean;
    seen?: boolean;
    seenAt?: string;
}

export interface TypingEvent {
    username: string;
    typing: string;
}

export enum MessageType {
    TEXT = 'TEXT',
    FILE = 'FILE',
    IMAGE = 'IMAGE'
}

export interface Conversation {
    id: number;
    name: string;
    isGroup: boolean;
    createdAt: string;
    createdById: number;
    createdByUsername: string;
    members: ConversationMember[];
    lastMessage?: Message;
}

export interface ConversationMember {
    id: number;
    userId: number;
    username: string;
    isAdmin: boolean;
    lastSeen?: string;
}

export interface CreatePrivateConversationRequest {
    participantUserId: number;
}

export interface CreateGroupConversationRequest {
    name: string;
    creatorUserId: number;
    memberIds: number[];
}

export interface UpdateMessageRequest {
    content: string;
}

export interface AddGroupMemberRequest {
    userId: number;
}

export type UserRole = 'STUDENT' | 'TUTOR' | 'ADMIN';
