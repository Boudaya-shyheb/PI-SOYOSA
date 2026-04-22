import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaymentRoutingModule } from './payment.routing';
import { PaymentComponent } from './payment.component';

@NgModule({
  declarations: [PaymentComponent],
  imports: [CommonModule, FormsModule, PaymentRoutingModule]
})
export class PaymentModule {}
