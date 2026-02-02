import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Product } from '@shared/models/product.model';
import { Category } from '@shared/models/category.model';
import { CartService } from '@services/cart.service';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '@services/auth.service';
import {ProductService} from '@services/product.service';
import {CategoryService} from '@services/category.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',

})
export class HomeComponent implements OnInit {
  featuredProducts: Product[] = [];
  categories: Category[] = [];
  loading = true;

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private cartService: CartService,
    private toastr: ToastrService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    this.productService.getFeaturedProducts(4).subscribe({
      next: (products) => {
        this.featuredProducts = products.map(p => this.calculateProductFields(p));
        this.checkLoading();
      },
      error: (err) => {
        console.error('Error loading featured products', err);
        this.checkLoading();
      }
    });

    this.categoryService.getActiveCategories().subscribe({
      next: (categories) => {
        this.categories = categories.slice(0, 6);
        this.checkLoading();
      },
      error: (err) => {
        console.error('Error loading categories', err);
        this.checkLoading();
      }
    });

  }

  calculateProductFields(product: Product): Product {
    const hasDiscount = !!(product.discountPercentage && product.discountPercentage > 0);
    const currentPrice = hasDiscount
      ? product.price * (1 - product.discountPercentage! / 100)
      : product.price;

    return {
      ...product,
      hasDiscount,
      currentPrice
    };
  }

  checkLoading(): void {
    this.loading = false;
    this.cdr.detectChanges();
  }

  addToCart(product: Product): void {
    this.cartService.addToCart(product.id, 1).subscribe({
      next: () => {
        this.toastr.success('Product added to cart', 'Success');
      },
      error: (err) => {
        console.error('Error adding to cart', err);
        this.toastr.error('Failed to add product to cart', 'Error');
      }
    });
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }
}
