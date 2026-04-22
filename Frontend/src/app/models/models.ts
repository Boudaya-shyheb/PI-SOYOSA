export interface Course {
  id: string;
  title: string;
  description: string;
  price: number;
  oldPrice?: number;
  type: 'Blended' | 'Live' | 'Self-paced';
  level: 'Beginner' | 'Mid-level' | 'Advanced';
  duration: string;
  image: string;
  tags: string[];
}

export interface SchoolEvent {
  id: string;
  title: string;
  date: string;
  time: string;
  location: string;
  image: string;
  status: 'All' | 'Today' | 'Past event' | 'Next event';
  description?: string;
}

export interface Club {
  id: string;
  title: string;
  description: string;
  icon: string;
  image: string;
}

export interface Instructor {
  id: string;
  name: string;
  role: string;
  image: string;
}

export interface Chapter {
  id: string;
  title: string;
  sections: {
    id: string;
    title: string;
    duration: string;
    completed: boolean;
  }[];
}
