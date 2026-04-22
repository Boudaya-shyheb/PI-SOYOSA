import { Course, SchoolEvent, Club, Instructor } from '../models/models';

export const COLORS = {
  primary: '#2D5757',
  secondary: '#F7EDE2',
  accent: '#F6BD60',
  danger: '#C84630',
  dark: '#3D3D60',
};

export const COURSES: Course[] = [
  {
    id: '1',
    title: 'Speak Fluent English in 30 Days—No Boring Grammar Rules!',
    description: 'Struggling to speak confidently? This course ditches complex grammar drills and focuses on real-world conversations.',
    price: 350,
    oldPrice: 530,
    type: 'Blended',
    level: 'Beginner',
    duration: '30 Days',
    image: 'https://picsum.photos/seed/course1/800/600',
    tags: ['Conversation', 'Fluency']
  },
  {
    id: '2',
    title: 'The Ultimate English Writing Masterclass: From Beginner to Pro!',
    description: "Want to write like a pro? Whether it's emails, essays, or creative stories, this course teaches you the secrets of powerful writing.",
    price: 369,
    type: 'Live',
    level: 'Mid-level',
    duration: '8 Weeks',
    image: 'https://picsum.photos/seed/course2/800/600',
    tags: ['Writing', 'Professional']
  },
  {
    id: '3',
    title: 'Accent Makeover: Sound Like a Native in Just Weeks!',
    description: 'Tired of being misunderstood? Learn pronunciation hacks, rhythm, and intonation that will instantly improve your accent.',
    price: 1400,
    oldPrice: 2000,
    type: 'Live',
    level: 'Advanced',
    duration: '6 Weeks',
    image: 'https://picsum.photos/seed/course3/800/600',
    tags: ['Accent', 'Phonetics']
  }
];

export const EVENTS: SchoolEvent[] = [
  {
    id: 'e1',
    title: 'Freelancer & Entrepreneur Networking Night',
    date: '12 Jan. 2025',
    time: '11:00 am to 18:30 pm',
    location: 'Technopole, Sousse, Tunisia',
    status: 'Next event',
    image: 'https://picsum.photos/seed/event1/800/600'
  },
  {
    id: 'e2',
    title: 'Mastering Contracts Workshop and Publishing Industries',
    date: '12 Jan. 2025',
    time: '11:00 am to 18:30 pm',
    location: 'Technopole, Sousse, Tunisia',
    status: 'Past event',
    image: 'https://picsum.photos/seed/event2/800/600'
  },
  {
    id: 'e3',
    title: 'Skill Up: Online Workshop Series',
    date: '12 Jan. 2025',
    time: '11:00 am to 18:30 pm',
    location: 'Technopole, Sousse, Tunisia',
    status: 'Next event',
    image: 'https://picsum.photos/seed/event3/800/600'
  }
];

export const CLUBS: Club[] = [
  {
    id: 'c1',
    title: 'English Conversation Club',
    description: 'Practice speaking with peers in a fun and supportive environment through interactive discussions.',
    icon: '📣',
    image: 'https://picsum.photos/seed/club1/800/600'
  },
  {
    id: 'c2',
    title: 'Book & Storytelling Club',
    description: 'Improve reading skills and vocabulary by exploring books, short stories, and creative storytelling.',
    icon: '📖',
    image: 'https://picsum.photos/seed/club2/800/600'
  },
  {
    id: 'c3',
    title: 'Drama & Roleplay Club',
    description: 'Boost confidence and pronunciation by acting out real-life scenarios and fun roleplays.',
    icon: '🎭',
    image: 'https://picsum.photos/seed/club3/800/600'
  }
];

export const INSTRUCTORS: Instructor[] = [
  { id: 'i1', name: 'David Richards', role: 'Business English & Cross-Cultural Specialist', image: 'https://picsum.photos/seed/inst1/200/200' },
  { id: 'i2', name: 'Sophia Abdu', role: 'English for Academic Purposes (EAP) Expert', image: 'https://picsum.photos/seed/inst2/200/200' },
  { id: 'i3', name: 'Michael Lee', role: 'English Pronunciation & Accent Specialist', image: 'https://picsum.photos/seed/inst3/200/200' },
  { id: 'i4', name: 'Emily Watson', role: 'General English & Exam Prep Specialist', image: 'https://picsum.photos/seed/inst4/200/200' }
];
