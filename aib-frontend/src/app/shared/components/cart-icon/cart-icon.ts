// src/app/components/cart-icon/cart-icon.component.ts
import { Component } from '@angular/core';
import { CartService } from '@services/cart.service';
import { AsyncPipe } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-cart-icon',
  standalone: true,
  imports: [AsyncPipe, RouterLink],          // NgIf n’est plus nécessaire avec @if
  template: `
    <a routerLink="/cart" class="relative inline-block">
      <span class="material-icons text-3xl">shopping_cart</span>

      @if (cartService.itemCount$ | async; as count) {
        <span class="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-6 h-6 flex items-center justify-center">
          {{ count }}
        </span>
      }
    </a>
  `
})
export class CartIconComponent {
  constructor(public cartService: CartService) {}
}
