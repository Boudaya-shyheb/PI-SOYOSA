export interface CourseDisplayOverride {
  price?: number;
  isPaid?: boolean;
  image?: string;
}

export const COURSE_DISPLAY_OVERRIDES: Record<string, CourseDisplayOverride> = {};

export const DEFAULT_COURSE_IMAGE = 'https://picsum.photos/seed/course/800/600';
export const DEFAULT_COURSE_PRICE = 0;
