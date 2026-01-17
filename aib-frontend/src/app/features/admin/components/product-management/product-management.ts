import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Product, ProductRequest, ProductSearchRequest } from '@shared/models/product.model';
import { Category } from '@shared/models/category.model';
import Swal from 'sweetalert2';
import {ProductService} from '@services/product.service';
import {CategoryService} from '@services/category.service';

@Component({
  selector: 'app-admin-product-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './product-management.html',
})
export class AdminProductManagementComponent implements OnInit {
  products: Product[] = [];
  categories: Category[] = [];
  productForm: FormGroup;
  loading = false;
  submitting = false;
  showModal = false;
  editingProduct: Product | null = null;
  error = '';
  successMessage = '';

  totalProducts = 0;
  activeProducts = 0;
  lowStockProducts = 0;
  outOfStockProducts = 0;

  searchKeyword = '';
  selectedCategoryFilter: number | undefined;
  selectedStatusFilter: 'all' | 'active' | 'inactive' = 'all';
  stockFilter: string = 'all';

  currentPage = 0;
  totalPages = 0;
  pageSize = 20;

  constructor(
    private productService: ProductService,
    private categoryService: CategoryService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef,
    private route: ActivatedRoute
  ) {
    this.productForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
      description: ['', [Validators.maxLength(5000)]],
      categoryId: ['', [Validators.required]],
      price: ['', [Validators.required, Validators.min(0.01)]],
      discount: [0, [Validators.min(0), Validators.max(100)]],
      stockQuantity: ['', [Validators.required, Validators.min(0)]],
      brand: ['', [Validators.required, Validators.maxLength(100)]],
      model: ['', [Validators.maxLength(100)]],
      mainImageUrl: [''],
      isFeatured: [false],
      active: [true]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
    this.route.queryParams.subscribe(params => {
      const categoryId = params['categoryId'];
      if (categoryId) {
        this.selectedCategoryFilter = +categoryId;
      }
      this.loadProducts();
    });
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (categories: Category[]) => this.categories = categories,
      error: (error: any) => this.error = 'Failed to load categories'
    });
  }

  loadProducts(): void {
    this.loading = true;
    this.error = '';

    const searchRequest: ProductSearchRequest = {
      keyword: this.searchKeyword || undefined,
      categoryId: this.selectedCategoryFilter,
      page: this.currentPage,
      size: this.pageSize,
      sortBy: 'createdAt',
      sortDirection: 'DESC',
      stockStatus: this.stockFilter !== 'all' ? this.stockFilter : undefined
    };

    if (this.selectedStatusFilter !== 'all') {
      searchRequest.active = this.selectedStatusFilter === 'active';
    }

    this.productService.searchProducts(searchRequest).subscribe({
      next: (response: any) => {
        this.products = response.products.map((p: any) => this.calculateProductFields(p));
        this.totalPages = response.totalPages;
        this.totalProducts = response.totalElements;
        this.updateStats();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error: any) => {
        this.error = 'Failed to load products';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  updateStats(): void {
    this.activeProducts = this.products.filter(p => p.active).length;
    this.lowStockProducts = this.products.filter(p => p.stockQuantity > 0 && p.stockQuantity < 10).length;
    this.outOfStockProducts = this.products.filter(p => p.stockQuantity === 0).length;
  }

  calculateProductFields(product: Product): Product {
    const hasDiscount = !!(product.discountPercentage && product.discountPercentage > 0);
    return {
      ...product,
      hasDiscount,
      currentPrice: hasDiscount ? product.price * (1 - product.discountPercentage! / 100) : product.price
    };
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadProducts();
  }

  onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadProducts();
    }
  }

  openCreateModal(): void {
    this.editingProduct = null;
    this.productForm.reset({ active: true, isFeatured: false, discount: 0 });
    this.showModal = true;
    this.error = '';
    this.successMessage = '';
    this.cdr.detectChanges();
  }

  openEditModal(product: Product): void {
    this.editingProduct = product;
    this.productForm.patchValue({
      ...product,
      discount: product.discountPercentage
    });
    this.showModal = true;
    this.error = '';
    this.successMessage = '';
    this.cdr.detectChanges();
  }

  closeModal(): void {
    this.showModal = false;
    this.editingProduct = null;
    this.productForm.reset();
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (this.productForm.invalid) {
      this.productForm.markAllAsTouched();
      this.cdr.detectChanges();
      return;
    }

    this.submitting = true;
    this.error = '';
    const formValue: ProductRequest = this.productForm.value;

    const apiCall = this.editingProduct
      ? this.productService.updateProduct(this.editingProduct.id, formValue)
      : this.productService.createProduct(formValue);

    apiCall.subscribe({
      next: () => {
        this.successMessage = `Product ${this.editingProduct ? 'updated' : 'created'} successfully!`;
        this.loadProducts();
        this.submitting = false;
        setTimeout(() => this.closeModal(), 1500);
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.error = err.error?.message || `Failed to ${this.editingProduct ? 'update' : 'create'} product`;
        this.submitting = false;
        this.cdr.detectChanges();
      }
    });
  }

  toggleStatus(product: Product): void {
    this.productService.toggleProductStatus(product.id).subscribe({
      next: (updatedProduct: any) => {
        this.successMessage = `Product "${updatedProduct.name}" is now ${updatedProduct.active ? 'Active' : 'Inactive'}`;
        this.loadProducts();
        this.cdr.detectChanges();
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err: any) => {
        console.error('Error toggling product status:', err);
        this.error = err.error?.message || 'Failed to update product status';
        setTimeout(() => this.error = '', 5000);
        this.cdr.detectChanges();
      }
    });
  }

  deleteProduct(product: Product): void {
    Swal.fire({
      title: 'Are you sure?',
      text: `You won't be able to revert this!`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete it!',
      cancelButtonText: 'No, cancel!',
      reverseButtons: true
    }).then((result) => {
      if (result.isConfirmed) {
        this.productService.deleteProduct(product.id).subscribe({
          next: () => {
            this.successMessage = 'Product deleted successfully!';
            this.loadProducts();
            this.cdr.detectChanges();
          },
          error: (err: any) => {
            this.error = err.error?.message || 'Failed to delete product';
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  getCategoryName(categoryId: number): string {
    const category = this.categories.find(c => c.id === categoryId);
    return category ? category.name : 'Unknown';
  }

  getFieldError(fieldName: string): string {
    const field = this.productForm.get(fieldName);
    if (field?.touched && field?.errors) {
      if (field.errors['required']) return 'This field is required';
      if (field.errors['minlength']) return `Minimum length is ${field.errors['minlength'].requiredLength}`;
      if (field.errors['maxlength']) return `Maximum length is ${field.errors['maxlength'].requiredLength}`;
      if (field.errors['min']) return `Minimum value is ${field.errors['min'].min}`;
      if (field.errors['max']) return `Maximum value is ${field.errors['max'].max}`;
    }
    return '';
  }

  get isEditing(): boolean { return !!this.editingProduct; }
  get isSaving(): boolean { return this.submitting; }
}
