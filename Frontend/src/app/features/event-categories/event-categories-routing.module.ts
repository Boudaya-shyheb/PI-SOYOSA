import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EventCategoriesComponent } from './event-categories/event-categories.component';
import { CreateCategoryComponent } from './create-category/create-category.component';
import { RoleGuard } from '../../guards/role.guard';

const routes: Routes = [
  { path: '', component: EventCategoriesComponent },
  { path: 'create', component: CreateCategoryComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'edit/:id', component: CreateCategoryComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EventCategoriesRoutingModule {}
