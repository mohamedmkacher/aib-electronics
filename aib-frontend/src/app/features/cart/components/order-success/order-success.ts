// src/app/pages/order-success/order-success.ts
import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { OrderService, Order } from '@services/order-service';
import { CartService } from '@services/cart.service';

@Component({
  selector: 'app-order-success',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './order-success.html',

})
export class OrderSuccess implements OnInit {
  paymentIntentId = '';
  order: Order | null = null;
  loading = true;
  error = '';

  // Properties for template binding
  orderNumber = '';
  estimatedDelivery = '';
  totalAmount = 0;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private cartService: CartService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Clear cart immediately on order success page load
    this.clearCartAfterOrder();

    this.route.queryParams.subscribe(params => {
      this.paymentIntentId = params['payment_intent'] || '';
      if (this.paymentIntentId) {
        this.loadOrderByPaymentIntent(this.paymentIntentId);
      } else {
        this.error = 'No payment intent ID found.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
    this.cdr.detectChanges();
  }

  /**
   * Clear cart after successful order placement
   */
  private clearCartAfterOrder(): void {
    // Clear cart locally first for immediate UI update
    this.cartService.clearCartLocally();

    // Then clear on server (fire and forget)
    this.cartService.clearCart().subscribe({
      next: () => {
        console.log('Cart cleared successfully after order');
      },
      error: (err) => {
        console.warn('Failed to clear cart on server (may already be empty):', err);
      }
    });
  }

  loadOrderByPaymentIntent(paymentIntentId: string): void {
    this.loading = true;

    // Get user orders and find the one with matching payment intent
    this.orderService.getUserOrders().subscribe({
      next: (orders) => {
        const matchingOrder = orders.find(o => o.stripePaymentIntentId === paymentIntentId);

        if (matchingOrder) {
          this.order = matchingOrder;
          this.orderNumber = matchingOrder.orderNumber;
          this.totalAmount = matchingOrder.total;

          // Calculate estimated delivery (5 business days)
          const createdDate = this.parseOrderDate(matchingOrder.createdAt);
          createdDate.setDate(createdDate.getDate() + 5);
          this.estimatedDelivery = createdDate.toLocaleDateString('en-US', {
            month: 'long',
            day: 'numeric',
            year: 'numeric'
          });
        } else {
          // Fallback if order not found
          this.orderNumber = 'Processing...';
          this.estimatedDelivery = '3-5 business days';
          this.totalAmount = 0;
        }

        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading order:', err);
        // Show success anyway since payment was successful
        this.orderNumber = 'Confirmed';
        this.estimatedDelivery = '3-5 business days';
        this.totalAmount = 0;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
    this.cdr.detectChanges();
  }

  /**
   * Parse order date which can be either ISO string or number array
   */
  private parseOrderDate(createdAt: string | number[]): Date {
    if (Array.isArray(createdAt)) {
      // Format: [year, month, day, hour, minute, second, nano]
      return new Date(
        createdAt[0],
        createdAt[1] - 1, // Month is 0-indexed
        createdAt[2],
        createdAt[3] || 0,
        createdAt[4] || 0,
        createdAt[5] || 0
      );
    }
    return new Date(createdAt);
  }
}
