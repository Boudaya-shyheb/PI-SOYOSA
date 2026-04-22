import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PaymentComponent } from './payment.component';
import { RoleGuard } from '../../guards/role.guard';

const routes: Routes = [
  { 
    path: '', 
    component: PaymentComponent,
    canActivate: [RoleGuard],
    data: { roles: ['STUDENT', 'USER'] }
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PaymentRoutingModule {}
