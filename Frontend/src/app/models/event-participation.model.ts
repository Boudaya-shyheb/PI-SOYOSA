export interface EventParticipation {
  id?: number;
  participationDate?: string;
  status?: string;
  eventId?: number;

  fullName: string;
  email: string;
  phone?: string;
  university?: string;
  level?: string;
  motivation?: string;
}