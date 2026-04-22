import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EcommerceAdminApiService, OrderDto } from '../../../services/ecommerce-admin-api.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-shop-orders',
  templateUrl: './shop-orders.component.html',
  styleUrls: ['./shop-orders.component.css']
})
export class ShopOrdersComponent implements OnInit {
  orders: OrderDto[] = [];
  loading = false;
  errorMessage = '';
  customerId: number | null = null;

  constructor(
    private ecommerce: EcommerceAdminApiService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = Number(this.auth.getUserId());
    if (!Number.isFinite(id) || id <= 0) {
      this.errorMessage = 'Sign in to view your orders.';
      return;
    }
    this.customerId = id;
    this.loadOrders(id);
  }

  loadOrders(customerId: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.ecommerce.listOrdersByCustomer(customerId).subscribe({
      next: (response) => {
        this.orders = response.data ?? [];
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Unable to load your orders.';
        this.loading = false;
      }
    });
  }

  getItemCount(order: OrderDto): number {
    return (order.orderItems || []).reduce((sum, item) => sum + (item.quantity || 0), 0);
  }

  trackOrder(orderId: number): void {
    this.router.navigate(['/shop/tracking', orderId]);
  }
}
