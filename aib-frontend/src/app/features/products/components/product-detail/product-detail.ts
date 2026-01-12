import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Product } from '@shared/models/product.model';
import { CartService } from '@services/cart.service';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '@services/auth.service';
import {ProductService} from '@services/product.service';

@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-detail.html',
  styleUrls: ['./product-detail.css']
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  relatedProducts: Product[] = [];
  loading = false;
  error = '';
  selectedImageIndex = 0;
  quantity = 1;

  constructor(
    private cartService: CartService,
    private toastr: ToastrService,
    private route: ActivatedRoute,
    private productService: ProductService,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const slug = params['slug'];
      if (slug) {
        this.loadProduct(slug);
      }
    });
  }

  loadProduct(slug: string): void {
    this.loading = true;
    this.error = '';

    this.productService.getProductBySlug(slug).subscribe({
      next: (product) => {
        this.product = this.calculateProductFields(product);
        this.loading = false;

        if (product.categoryId) {
          this.loadRelatedProducts(product.categoryId, product.id);
        }
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading product:', error);
        this.error = 'Product not found';
        this.loading = false;
        this.cdr.detectChanges();
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

  loadRelatedProducts(categoryId: number, currentProductId: number): void {
    this.productService.getProductsByCategory(categoryId, 0, 4).subscribe({
      next: (products) => {
        this.relatedProducts = products
          .filter(p => p.id !== currentProductId)
          .map(p => this.calculateProductFields(p));
      },
      error: (error) => console.error('Error loading related products:', error)
    });
  }

  addToCart() {
    if (!this.product) return;

    this.cartService.addToCart(this.product.id, this.quantity).subscribe({
      next: () => {
        this.toastr.success(
          `
          <div class="flex items-center gap-3">
            <img src="${this.product?.mainImageUrl || 'assets/images/placeholder.jpg'}"
                 class="w-12 h-12 object-cover rounded-lg shadow"
                 alt="${this.product?.name}">
            <div>
              <div class="font-bold text-sm">${this.product?.name}</div>
              <div class="text-xs text-gray-600">Quantity: ${this.quantity}</div>
            </div>
          </div>
          `,
          'Added to cart!',
          {
            enableHtml: true,
            toastClass: 'ngx-toastr custom-toast',
            timeOut: 4000,
            positionClass: 'toast-top-right',
            progressBar: true,
            closeButton: true
          }
        );
        this.cartService.refresh();
      },
      error: (err) => {
        console.error('Cart error:', err);
        this.toastr.error('Failed to add to cart', 'Error');
      }
    });
  }

  increaseQuantity(): void {
    if (this.product && this.quantity < this.product.stockQuantity) {
      this.quantity++;
    }
  }

  decreaseQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  buyNow(): void {
    if (this.product) {
      console.log('Buy now:', { product: this.product, quantity: this.quantity });
      alert(`Proceeding to checkout with ${this.quantity} ${this.product.name}`);
    }
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }
}
