import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import * as L from 'leaflet';
import { EcommerceAdminApiService, CartDto, CreateOrderRequest } from '../../../services/ecommerce-admin-api.service';
import { AuthService } from '../../../services/auth.service';
import { ReverseGeocodeResult, RoutingService } from '../../../services/routing.service';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-shop-cart',
  templateUrl: './shop-cart.component.html',
  styleUrls: ['./shop-cart.component.css']
})
export class ShopCartComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('deliveryMap') deliveryMap?: ElementRef<HTMLDivElement>;
  couponCode = '';
  appliedCode = '';
  discount = 0;
  freeShipping = false;
  customerId = 1;
  cart: CartDto | null = null;
  items: { itemType: 'PRODUCT' | 'BUNDLE'; productId?: number; bundleId?: number; title: string; price: number; quantity: number; image: string }[] = [];
  isLoading = false;
  isCheckingOut = false;
  error: string | null = null;
  checkoutError: string | null = null;
  checkoutSuccess = '';
  couponError: string | null = null;
  deliveryStreet = '';
  deliveryCity = '';
  deliveryPostalCode = '';
  deliveryCountry = 'Tunisia';
  deliveryLat: number | null = null;
  deliveryLng: number | null = null;
  locationError = '';
  isLocating = false;
  private destroy$ = new Subject<void>();
  private map: L.Map | null = null;
  private marker: L.Marker | null = null;

  constructor(
    private ecommerceService: EcommerceAdminApiService,
    private auth: AuthService,
    private routing: RoutingService
  ) { }

  ngOnInit(): void {
    const resolved = this.resolveCustomerId();
    if (resolved === null) {
      this.error = 'Login required to access your cart.';
      return;
    }
    this.customerId = resolved;
    this.loadCart();
  }

  ngAfterViewInit(): void {
    if (!this.deliveryMap) {
      return;
    }
    const initialLat = 36.8065;
    const initialLng = 10.1815;
    this.map = L.map(this.deliveryMap.nativeElement, { zoomControl: true })
      .setView([initialLat, initialLng], 12);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);

    this.map.on('click', (event: L.LeafletMouseEvent) => {
      this.setDeliveryCoords(event.latlng.lat, event.latlng.lng, true);
      this.reverseFillAddress(event.latlng.lat, event.latlng.lng, false);
    });
  }

  get subtotal(): number {
    return this.items.reduce((total, item) => total + item.price * item.quantity, 0);
  }

  get discountedSubtotal(): number {
    return Math.max(this.subtotal - this.discount, 0);
  }

  get shipping(): number {
    if (this.freeShipping) {
      return 0;
    }
    return this.discountedSubtotal > 120 ? 0 : 8;
  }

  get total(): number {
    return this.discountedSubtotal + this.shipping;
  }

  applyCoupon() {
    if (!this.customerId) {
      this.couponError = 'Login required to apply a coupon.';
      return;
    }
    const code = this.couponCode.trim().toUpperCase();
    if (!code) {
      this.couponError = 'Enter a coupon code.';
      return;
    }
    this.couponError = null;
    this.ecommerceService.applyCoupon(this.customerId, code)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.discount = Number(response.data.discountAmount || 0);
          this.freeShipping = !!response.data.freeShipping;
          this.appliedCode = response.data.code || code;
        },
        error: (err) => {
          this.discount = 0;
          this.freeShipping = false;
          this.appliedCode = '';
          this.couponError = err?.error?.message || 'Invalid coupon.';
        }
      });
  }

  updateQuantity(item: { itemType: 'PRODUCT' | 'BUNDLE'; productId?: number; bundleId?: number }, quantity: number): void {
    if (!this.customerId) {
      this.error = 'Login required to update cart.';
      return;
    }
    const nextQuantity = Number(quantity);
    if (!Number.isFinite(nextQuantity) || nextQuantity <= 0) {
      return;
    }
    if (item.itemType === 'BUNDLE' && item.bundleId) {
      this.ecommerceService.updateBundleItemQuantity(this.customerId, item.bundleId, nextQuantity)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => this.syncCart(response.data),
          error: (err) => {
            this.error = err?.error?.message || 'Failed to update quantity.';
          }
        });
      return;
    }
    if (item.productId) {
      this.ecommerceService.updateCartItemQuantity(this.customerId, item.productId, nextQuantity)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => this.syncCart(response.data),
          error: (err) => {
            this.error = err?.error?.message || 'Failed to update quantity.';
          }
        });
    }
  }

  removeItem(item: { itemType: 'PRODUCT' | 'BUNDLE'; productId?: number; bundleId?: number }): void {
    if (!this.customerId) {
      this.error = 'Login required to update cart.';
      return;
    }
    if (item.itemType === 'BUNDLE' && item.bundleId) {
      this.ecommerceService.removeBundleFromCart(this.customerId, item.bundleId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => this.syncCart(response.data),
          error: (err) => {
            this.error = err?.error?.message || 'Failed to remove item.';
          }
        });
      return;
    }
    if (item.productId) {
      this.ecommerceService.removeFromCart(this.customerId, item.productId)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: (response) => this.syncCart(response.data),
          error: (err) => {
            this.error = err?.error?.message || 'Failed to remove item.';
          }
        });
    }
  }

  clearCart(): void {
    if (!this.customerId) {
      this.error = 'Login required to clear cart.';
      return;
    }
    this.ecommerceService.clearCart(this.customerId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => this.syncCart(response.data),
        error: (err) => {
          this.error = err?.error?.message || 'Failed to clear cart.';
        }
      });
  }

  proceedToCheckout(): void {
    if (!this.customerId) {
      this.checkoutError = 'Login required to checkout.';
      return;
    }
    if (this.items.length === 0) {
      this.checkoutError = 'Your cart is empty.';
      return;
    }
    if (!this.deliveryStreet.trim() || !this.deliveryCity.trim() || !this.deliveryCountry.trim()) {
      this.checkoutError = 'Add a delivery street, city, and country before checkout.';
      return;
    }
    if (this.deliveryLat === null || this.deliveryLng === null) {
      this.checkoutError = 'Pick your delivery location on the map or use your current location.';
      return;
    }
    if (this.isCheckingOut) {
      return;
    }

    const productItems = this.items.filter((item) => item.itemType === 'PRODUCT' && item.productId);
    const bundleItems = this.items.filter((item) => item.itemType === 'BUNDLE' && item.bundleId);

    this.isCheckingOut = true;
    this.checkoutError = null;
    this.checkoutSuccess = '';

    if (bundleItems.length === 0) {
      const request: CreateOrderRequest = {
        customerId: this.customerId,
        items: productItems.map((item) => ({
          productId: item.productId as number,
          quantity: item.quantity
        })),
        discountAmount: this.discount > 0 ? this.discount : undefined,
        couponCode: this.appliedCode || undefined,
        deliveryStreet: this.deliveryStreet.trim(),
        deliveryCity: this.deliveryCity.trim(),
        deliveryPostalCode: this.deliveryPostalCode.trim() || undefined,
        deliveryCountry: this.deliveryCountry.trim(),
        deliveryLat: this.deliveryLat ?? undefined,
        deliveryLng: this.deliveryLng ?? undefined
      };
      this.submitOrder(request);
      return;
    }

    forkJoin(bundleItems.map((item) => this.ecommerceService.getBundleById(item.bundleId as number)))
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (responses) => {
          const bundleOrderItems = responses.flatMap((response, index) => {
            const bundle = response.data;
            const bundleQty = bundleItems[index].quantity;
            const bundleLineItems = bundle?.items ?? [];
            return bundleLineItems.map((bundleItem) => ({
              productId: bundleItem.productId,
              quantity: bundleItem.quantity * bundleQty
            }));
          });

          const request: CreateOrderRequest = {
            customerId: this.customerId,
            items: [
              ...productItems.map((item) => ({
                productId: item.productId as number,
                quantity: item.quantity
              })),
              ...bundleOrderItems
            ],
            discountAmount: this.discount > 0 ? this.discount : undefined,
            couponCode: this.appliedCode || undefined,
            deliveryStreet: this.deliveryStreet.trim(),
            deliveryCity: this.deliveryCity.trim(),
            deliveryPostalCode: this.deliveryPostalCode.trim() || undefined,
            deliveryCountry: this.deliveryCountry.trim(),
            deliveryLat: this.deliveryLat ?? undefined,
            deliveryLng: this.deliveryLng ?? undefined
          };

          this.submitOrder(request);
        },
        error: (err) => {
          this.isCheckingOut = false;
          this.checkoutError = err?.error?.message || 'Failed to prepare order.';
        }
      });
  }

  useCurrentLocation(): void {
    if (!navigator.geolocation) {
      this.locationError = 'Geolocation is not supported by this browser.';
      return;
    }
    this.locationError = '';
    this.isLocating = true;
    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.setDeliveryCoords(position.coords.latitude, position.coords.longitude, true);
        this.reverseFillAddress(position.coords.latitude, position.coords.longitude, false);
        this.isLocating = false;
      },
      () => {
        this.locationError = 'Unable to access your current location.';
        this.isLocating = false;
      },
      { enableHighAccuracy: true, timeout: 10000 }
    );
  }

  private setDeliveryCoords(lat: number, lng: number, centerMap: boolean): void {
    this.deliveryLat = Number(lat.toFixed(6));
    this.deliveryLng = Number(lng.toFixed(6));
    if (this.map) {
      if (!this.marker) {
        this.marker = L.marker([lat, lng]).addTo(this.map);
      } else {
        this.marker.setLatLng([lat, lng]);
      }
      if (centerMap) {
        this.map.setView([lat, lng], 15);
      }
    }
  }

  autoFillAddress(): void {
    if (this.deliveryLat === null || this.deliveryLng === null) {
      this.locationError = 'Pick a delivery location first.';
      return;
    }
    this.reverseFillAddress(this.deliveryLat, this.deliveryLng, true);
  }

  private reverseFillAddress(lat: number, lng: number, force: boolean): void {
    this.routing.reverseGeocode(lat, lng).subscribe({
      next: (result) => this.applyReverseAddress(result, force),
      error: () => {
        if (force) {
          this.locationError = 'Unable to auto-fill address from the map.';
        }
      }
    });
  }

  private applyReverseAddress(result: ReverseGeocodeResult | null, force: boolean): void {
    if (!result) {
      if (force) {
        this.locationError = 'Unable to auto-fill address from the map.';
      }
      return;
    }
    const street = [result.road, result.house_number].filter(Boolean).join(' ');
    const city = result.city || result.town || result.village || result.state || '';
    const country = result.country || '';

    if (force || !this.deliveryStreet.trim()) {
      this.deliveryStreet = street || this.deliveryStreet;
    }
    if (force || !this.deliveryCity.trim()) {
      this.deliveryCity = city || this.deliveryCity;
    }
    if (force || !this.deliveryPostalCode.trim()) {
      this.deliveryPostalCode = result.postcode || this.deliveryPostalCode;
    }
    if (force || !this.deliveryCountry.trim()) {
      this.deliveryCountry = country || this.deliveryCountry;
    }
  }

  private submitOrder(request: CreateOrderRequest): void {
    if (!request.items.length) {
      this.isCheckingOut = false;
      this.checkoutError = 'Your cart is empty.';
      return;
    }
    this.ecommerceService.createOrder(request)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.checkoutSuccess = response.message || 'Order created successfully.';
          this.isCheckingOut = false;
          this.completeCheckout();
        },
        error: (err) => {
          this.isCheckingOut = false;
          this.checkoutError = err?.error?.message || 'Failed to create order.';
        }
      });
  }

  private completeCheckout(): void {
    this.ecommerceService.clearCart(this.customerId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => this.syncCart(response.data),
        error: (err) => {
          this.checkoutError = err?.error?.message || 'Order created, but failed to clear cart.';
        }
      });
  }

  private loadCart(): void {
    if (!this.customerId) {
      return;
    }
    this.isLoading = true;
    this.error = null;
    this.ecommerceService.getCart(this.customerId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.syncCart(response.data);
          this.isLoading = false;
        },
        error: (err) => {
          this.error = err?.error?.message || 'Failed to load cart.';
          this.isLoading = false;
        }
      });
  }

  private syncCart(cart: CartDto): void {
    this.cart = cart;
    const items = cart?.cartItems || [];
    this.items = items.map((item) => {
      const isBundle = item.itemType === 'BUNDLE' || !!item.bundleId;
      const title = isBundle
        ? (item.bundleName || `Bundle ${item.bundleId}`)
        : (item.productName || `Product ${item.productId}`);
      const price = isBundle ? Number(item.bundlePrice || 0) : Number(item.productPrice || 0);
      const image = isBundle
        ? (item.bundleImageUrl || `https://picsum.photos/seed/cart-bundle-${item.bundleId}/400/300`)
        : (item.productImageUrl || `https://picsum.photos/seed/cart-${item.productId}/400/300`);
      return {
        itemType: isBundle ? 'BUNDLE' : 'PRODUCT',
        productId: item.productId ?? undefined,
        bundleId: item.bundleId ?? undefined,
        title,
        price,
        quantity: item.quantity,
        image
      };
    });
  }

  private resolveCustomerId(): number | null {
    const payload = this.auth.getUserInfo();
    const raw = payload?.userId ?? payload?.id ?? payload?.sub ?? '';
    const parsed = Number(raw);
    if (!Number.isFinite(parsed) || parsed <= 0) {
      return null;
    }
    return parsed;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.map?.remove();
  }
}
