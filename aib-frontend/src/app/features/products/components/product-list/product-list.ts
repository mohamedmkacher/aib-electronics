import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Product, ProductSearchRequest } from '@shared/models/product.model';
import { Category } from '@shared/models/category.model';
import { CartService } from '@services/cart.service';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '@services/auth.service';
import {ProductService} from '@services/product.service';
import {CategoryService} from '@services/category.service';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './product-list.html',
  })
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  categories: Category[] = [];
  brands: string[] = [];
  loading = false;
  error = '';

  searchRequest: ProductSearchRequest = {
    page: 0,
    size: 12,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  };

  minPrice = 0;
  maxPrice = 10000;

  currentPage = 0;
  totalPages = 0;
  totalProducts = 0;

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private cartService: CartService,
    private toastr: ToastrService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadInitialData();
    this.route.queryParams.subscribe(params => {
      this.searchRequest.categoryId = params['category'] ? +params['category'] : undefined;
      this.resetPagination();
      this.searchProducts();
    });
  }

  loadInitialData(): void {
    this.categoryService.getActiveCategories().subscribe(data => this.categories = data);
    this.productService.getAllBrands().subscribe(data => this.brands = data);
    this.updatePriceRange();
  }

  searchProducts(): void {
    this.loading = true;
    this.productService.searchProducts(this.searchRequest).subscribe({
      next: (response) => {
        this.products = response.products.map(p => this.calculateProductFields(p));
        this.totalPages = response.totalPages;
        this.totalProducts = response.totalElements;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Failed to load products';
        this.loading = false;
      }
    });
  }

  updatePriceRange(): void {
    this.productService.getPriceRange(this.searchRequest).subscribe(range => {
      this.maxPrice = range.maxPrice;
    });
  }

  resetPagination(): void {
    this.currentPage = 0;
    this.searchRequest.page = 0;
  }

  onCategoryChange(categoryId: number | undefined): void {
    this.searchRequest.categoryId = categoryId;
    this.resetPagination();
    this.searchProducts();
    this.updatePriceRange();
  }

  onBrandChange(brand: string | undefined): void {
    this.searchRequest.brand = brand;
    this.resetPagination();
    this.searchProducts();
    this.updatePriceRange();
  }

  onPriceRangeChange(): void {
    this.searchRequest.minPrice = this.minPrice;
    this.searchRequest.maxPrice = this.maxPrice;
    this.resetPagination();
    this.searchProducts();
  }

  onSortChange(sortBy: string): void {
    if (sortBy === 'price') {
      this.searchRequest.sortDirection = 'ASC';
    } else {
      this.searchRequest.sortDirection = 'DESC';
    }
    this.searchRequest.sortBy = sortBy;
    this.resetPagination();
    this.searchProducts();
  }

  onPageChange(page: number): void {
    this.searchRequest.page = page;
    this.currentPage = page;
    this.searchProducts();
  }

  clearFilters(): void {
    this.searchRequest = {
      page: 0,
      size: 12,
      sortBy: 'createdAt',
      sortDirection: 'DESC'
    };
    this.minPrice = 0;
    this.maxPrice = 10000;
    this.resetPagination();
    this.searchProducts();
    this.updatePriceRange();
  }

  addToCart(product: Product): void {
    this.cartService.addToCart(product.id).subscribe({
      next: () => {
        this.toastr.success(`${product.name} added to cart!`);
        this.cartService.refresh();
      },
      error: (err) => this.toastr.error(err.error?.message || 'Failed to add to cart')
    });
  }

  calculateProductFields(product: Product): Product {
    const hasDiscount = !!(product.discountPercentage && product.discountPercentage > 0);
    return {
      ...product,
      hasDiscount,
      currentPrice: hasDiscount ? product.price * (1 - product.discountPercentage! / 100) : product.price
    };
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get hasPrevious(): boolean {
    return this.currentPage > 0;
  }

  get hasNext(): boolean {
    return this.currentPage < this.totalPages - 1;
  }
}
