import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ShopComponent } from './shop.component';
import { ShopRoutingModule } from './shop-routing.module';
import { ShopCatalogComponent } from './catalog/shop-catalog.component';
import { ShopProductDetailComponent } from './product-detail/shop-product-detail.component';
import { ShopCartComponent } from './cart/shop-cart.component';
import { ShopBundlesComponent } from './bundles/shop-bundles.component';
import { ShopBundleDetailComponent } from './bundle-detail/shop-bundle-detail.component';
import { ShopAdminComponent } from './admin/shop-admin.component';
import { RecommendationsComponent } from './recommendations/recommendations.component';
import { ShopOrderTrackingComponent } from './tracking/shop-order-tracking.component';
import { ShopOrdersComponent } from './orders/shop-orders.component';
import { ShopInsightsComponent } from './insights/shop-insights.component';

@NgModule({
  declarations: [
    ShopCatalogComponent,
    ShopProductDetailComponent,
    ShopCartComponent,
    ShopBundlesComponent,
    ShopBundleDetailComponent,
    ShopAdminComponent,
    ShopInsightsComponent,
    ShopOrderTrackingComponent,
    ShopOrdersComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ShopRoutingModule,
    ShopComponent,
    RecommendationsComponent
  ]
})
export class ShopModule { }
