import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TrainingDetailsRoutingModule } from './training-details.routing';
import { TrainingDetailsComponent } from './training-details.component';

@NgModule({
  declarations: [TrainingDetailsComponent],
  imports: [CommonModule, FormsModule, RouterModule, TrainingDetailsRoutingModule]
})
export class TrainingDetailsModule {}
