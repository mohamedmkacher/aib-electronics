import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { CartService } from '@services/cart.service';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';
import { AuthService } from '@services/auth.service';
import {CartItem, CartResponse} from '@shared/models/cart.model';

interface CartModel {
  items: CartItem[];
  totalItems: number;
  subtotal: number;
  totalDiscount: number;
  total: number;
}

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './cart.html',
  styleUrls: ['./cart.css'],
})
export class Cart implements OnInit {
  loading = true;
  cart: CartModel = {
    items: [],
    totalItems: 0,
    subtotal: 0,
    totalDiscount: 0,
    total: 0
  };

  constructor(
    public cartService: CartService,
    private router: Router,
    private toastr: ToastrService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadCartDetails();
  }

  loadCartDetails(): void {
    this.loading = true;
    this.cartService.cart$.subscribe(cartResponse => {
      if (cartResponse) {
        this.updateCartModel(cartResponse);
      } else {
        this.resetCartModel();
      }
      this.loading = false;
    });
  }

  updateCartModel(cartResponse: CartResponse): void {
    // Calculate discount as difference between subtotal (original price) and total (discounted price)
    const totalDiscount = cartResponse.subtotal - cartResponse.total;

    this.cart = {
      items: cartResponse.items,
      totalItems: cartResponse.itemCount,
      subtotal: cartResponse.subtotal, // Use subtotal from backend
      totalDiscount: totalDiscount > 0 ? totalDiscount : 0,
      total: cartResponse.total // Use total from backend
    };
  }

  resetCartModel(): void {
    this.cart = {
      items: [],
      totalItems: 0,
      subtotal: 0,
      totalDiscount: 0,
      total: 0
    };
  }

  increaseQuantity(item: CartItem): void {
    if (item.quantity >= item.stockQuantity) {
      this.toastr.warning(`Only ${item.stockQuantity} items available in stock`, 'Stock Limit');
      return;
    }
    this.cartService.updateQuantity(item.productId, item.quantity + 1).subscribe({
      next: () => this.toastr.success('Quantity updated', 'Cart'),
      error: (err) => this.handleError(err, 'Failed to update quantity')
    });
  }

  decreaseQuantity(item: CartItem): void {
    if (item.quantity > 1) {
      this.cartService.updateQuantity(item.productId, item.quantity - 1).subscribe({
        next: () => this.toastr.success('Quantity updated', 'Cart'),
        error: (err) => this.handleError(err, 'Failed to update quantity')
      });
    }
  }

  removeItem(item: CartItem): void {
    Swal.fire({
      title: 'Remove this item?',
      text: "Are you sure you want to remove this item from your cart?",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, remove it',
      cancelButtonText: 'Cancel',
    }).then((result) => {
      if (result.isConfirmed) {
        this.cartService.removeFromCart(item.productId).subscribe({
          next: () => this.toastr.success('Item removed', 'Cart'),
          error: (err) => this.handleError(err, 'Failed to remove item')
        });
      }
    });
  }

  clearCart(): void {
    Swal.fire({
      title: 'Clear cart?',
      text: "This action cannot be undone!",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, clear cart',
      cancelButtonText: 'Cancel'
    }).then((result) => {
      if (result.isConfirmed) {
        this.cartService.clearCart().subscribe({
          next: () => {
            this.toastr.success('Cart cleared', 'Success');
            this.router.navigate(['/products']);
          },
          error: (err) => this.handleError(err, 'Failed to clear cart')
        });
      }
    });
  }

  proceedToCheckout(): void {
    if (this.cart.items.length === 0) {
      this.toastr.info('Your cart is empty.', 'Cart');
      return;
    }

    if (this.hasStockIssues()) {
      this.toastr.error('Please resolve stock issues before proceeding.', 'Stock Issue');
      return;
    }

    if (!this.authService.isLoggedIn()) {
      this.toastr.warning('You must be logged in to continue.', 'Login Required');
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: '/checkout' } });
      return;
    }
    this.router.navigate(['/checkout']);
  }

  isOutOfStock(item: CartItem): boolean {
    return item.stockQuantity === 0;
  }

  isInactive(item: CartItem): boolean {
    return !item.active;
  }

  hasInsufficientStock(item: CartItem): boolean {
    return item.quantity > item.stockQuantity;
  }

  hasStockIssues(): boolean {
    return this.cart.items.some(item => this.isOutOfStock(item) || this.isInactive(item) || this.hasInsufficientStock(item));
  }

  private handleError(error: any, message: string): void {
    console.error(message, error);
    this.toastr.error(message, 'Error');
  }
}
