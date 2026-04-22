import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UserComponent } from './user/user.component';
import { AuthComponent } from './user/auth/auth.component';
import { ForgotPasswordComponent } from './user/forgot-password/forgot-password.component';
import { authGuard } from "./guards/auth.guard";
import { ResetPasswordComponent } from "./user/reset-password/reset-password.component";
import { PracticeEnglishComponent } from './features/practice-english/practice-english.component';
import {ProfilePageComponent} from "./user/profile-page/profile-page.component";

const routes: Routes = [
  { path: '', pathMatch: 'full', loadChildren: () => import('./features/landing/landing.module').then(m => m.LandingModule) },
  { path: 'landing', loadChildren: () => import('./features/landing/landing.module').then(m => m.LandingModule) },
  { path: 'login', component: AuthComponent },
  { path: 'register', component: AuthComponent },
  { path: 'courses', loadChildren: () => import('./features/courses/courses.module').then(m => m.CoursesModule) },
  { path: 'course-view', canActivate: [authGuard], loadChildren: () => import('./features/course-view/course-view.module').then(m => m.CourseViewModule) },
  { path: 'lessons', canActivate: [authGuard], loadChildren: () => import('./features/lesson-details/lesson-details.module').then(m => m.LessonDetailsModule) },
  { path: 'events', loadChildren: () => import('./features/events/events.module').then(m => m.EventsModule) },
  { path: 'search', loadChildren: () => import('./features/search/search.module').then(m => m.SearchModule) },
  { path: 'clubs', loadChildren: () => import('./features/clubs/clubs.module').then(m => m.ClubsModule) },
  { path: 'payment', canActivate: [authGuard], loadChildren: () => import('./features/payment/payment.module').then(m => m.PaymentModule) },
  { path: 'training-details/:courseId', canActivate: [authGuard], loadChildren: () => import('./features/training-details/training-details.module').then(m => m.TrainingDetailsModule) },
  { path: 'training-details', canActivate: [authGuard], loadChildren: () => import('./features/training-details/training-details.module').then(m => m.TrainingDetailsModule) },
  { path: 'chat', canActivate: [authGuard], loadChildren: () => import('./features/chat/chat.module').then(m => m.ChatModule) },
  { path: 'games', loadChildren: () => import('./features/games/games.module').then(m => m.GamesModule) },
  { path: 'categories', loadChildren: () => import('./features/event-categories/event-categories.module').then(m => m.EventCategoriesModule) },
  { path: 'trainings', loadChildren: () => import('./features/training/training.module').then(m => m.TrainingModule) },
  { path: 'shop', loadChildren: () => import('./features/shop/shop.module').then(m => m.ShopModule) },
  { path: 'practice-english', component: PracticeEnglishComponent },

  // Public auth pages under /user
  {
    path: 'user',
    component: UserComponent,
    children: [
      { path: 'login', component: AuthComponent },
      { path: 'register', component: AuthComponent },
      { path: 'forgot-password', component: ForgotPasswordComponent },
      { path: 'reset-password', component: ResetPasswordComponent },
      { path: 'profile', component: ProfilePageComponent}
    ]
  },
  { path: 'blog', loadChildren: () => import('./features/blog/blog.module').then(m => m.BlogModule) },
  { path: 'admin', loadChildren: () => import('./features/admin-dashboard/admin-dashboard.module').then(m => m.AdminDashboardModule) },
  { path: 'tutor-quiz', canActivate: [authGuard], loadChildren: () => import('./features/tutor-quiz/tutor-quiz.module').then(m => m.TutorQuizModule) },
    {path :'interview', canActivate: [authGuard], loadChildren: () => import('./features/interview/interview.module').then(m => m.InterviewModule) },
    { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
