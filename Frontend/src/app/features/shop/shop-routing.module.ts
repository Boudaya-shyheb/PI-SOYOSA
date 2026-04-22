import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ShopComponent } from './shop.component';
import { ShopCatalogComponent } from './catalog/shop-catalog.component';
import { ShopProductDetailComponent } from './product-detail/shop-product-detail.component';
import { ShopCartComponent } from './cart/shop-cart.component';
import { ShopBundlesComponent } from './bundles/shop-bundles.component';
import { ShopBundleDetailComponent } from './bundle-detail/shop-bundle-detail.component';
import { ShopAdminComponent } from './admin/shop-admin.component';
import { ShopInsightsComponent } from './insights/shop-insights.component';
import { ComparisonComponent } from './comparison/comparison.component';
import { RoleGuard } from '../../guards/role.guard';
import { ShopOrderTrackingComponent } from './tracking/shop-order-tracking.component';
import { ShopOrdersComponent } from './orders/shop-orders.component';
import { authGuard } from '../../guards/auth.guard';

const routes: Routes = [
  { path: '', component: ShopComponent },
  { path: 'catalog', component: ShopCatalogComponent },
  { path: 'product/:id', component: ShopProductDetailComponent },
  { path: 'cart', component: ShopCartComponent },
  { path: 'bundles', component: ShopBundlesComponent },
  { path: 'bundles/:id', component: ShopBundleDetailComponent },
  { path: 'comparison', component: ComparisonComponent },
  { path: 'orders', component: ShopOrdersComponent, canActivate: [authGuard] },
  { path: 'tracking/:orderId', component: ShopOrderTrackingComponent, canActivate: [authGuard] },
  { path: 'admin', component: ShopAdminComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'admin/insights', component: ShopInsightsComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
  { path: 'admin/:section', component: ShopAdminComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ShopRoutingModule { }
