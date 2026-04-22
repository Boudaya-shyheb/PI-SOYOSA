export interface Event {
  id?: number;
  title: string;
  description: string;
  date: string;
  startTime: string;
  endTime: string;
  location: string;
  maxParticipants: number;
  createdAt?: string;

  categoryId: number | null;
  categoryName?: string;
}