export interface Training {
    id?: number;
    title: string;
    description: string;
    level: 'A1' | 'A2' | 'A3' | 'B1' | 'B2' | 'B3' | 'C1' | 'C2';
    price: number;
    sessions?: Session[];
    reviews?: Review[];
    averageRating?: number;
    imageUrl?: string;
    createdByUserId?: number;
    type?: 'ONLINE' | 'OFFLINE';
    meetingLink?: string;
    location?: string;
    room?: string;
    latitude?: number;
    longitude?: number;
}

export interface Review {
    id?: number;
    rating: number;
    comment: string;
    trainingId: number;
    studentId?: number;
    studentName?: string;
    createdAt?: string;
}

export interface Session {
    id?: number;
    /**
     * The backend returns a string (ISO date) but we often convert it to a
     * `Date` object when normalizing responses. Allow both types here.
     */
    date: string | Date;
    startTime: string;
    duration: number;
    status: 'PLANNED' | 'COMPLETED' | 'CANCELLED';
    maxParticipants: number;
    availableSpots: number;
    training?: Training;
}

export const SESSION_TYPES = ['ONLINE', 'OFFLINE'];
export const STATUSES = ['PLANNED', 'COMPLETED', 'CANCELLED'];
export const LEVELS = ['A1', 'A2', 'A3', 'B1', 'B2', 'B3', 'C1', 'C2'];
export const LEVEL_DESCRIPTIONS: { [key: string]: string } = {
    'A1': 'Beginner - Discovery',
    'A2': 'Beginner - Survival',
    'B1': 'Intermediate - Threshold',
    'B2': 'Intermediate - Vantage',
    'B3': 'Intermediate - Advanced',
    'C1': 'Advanced - Effective Operational',
    'C2': 'Mastery - Native-like'
};

export const LEVEL_COLORS: { [key: string]: string } = {
    'A1': 'bg-green-100 text-green-800',
    'A2': 'bg-green-100 text-green-800',
    'B1': 'bg-blue-100 text-blue-800',
    'B2': 'bg-blue-100 text-blue-800',
    'B3': 'bg-blue-100 text-blue-800',
    'C1': 'bg-yellow-100 text-yellow-800',
    'C2': 'bg-orange-100 text-orange-800'
};
