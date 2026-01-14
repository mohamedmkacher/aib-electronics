// admin-category-management.component.ts
import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Category, CategoryRequest } from '@shared/models/category.model';
import {RouterLink} from '@angular/router';
import Swal from 'sweetalert2';
import {CategoryService} from '@core/services/category.service';


@Component({
  selector: 'app-admin-category-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './category-management.html',
})
export class AdminCategoryManagementComponent implements OnInit {
  categories: Category[] = [];
  categoryForm: FormGroup;
  loading = false;
  submitting = false;
  showModal = false;
  editingCategory: Category | null = null;
  error = '';
  successMessage = '';

  constructor(
    private categoryService: CategoryService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    this.categoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      description: ['', [Validators.maxLength(1000)]],
      imageUrl: [''],
      active: [true],
      displayOrder: [0]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.error = '';

    this.categoryService.getAllCategories().subscribe({
      next: (categories: Category[]) => {
        this.categories = categories;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Error loading categories:', error);
        this.error = 'Failed to load categories';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  openCreateModal(): void {
    this.editingCategory = null;
    this.categoryForm.reset({
      active: true,
      displayOrder: 0
    });
    this.showModal = true;
    this.error = '';
    this.successMessage = '';
    this.cdr.detectChanges();
  }

  // Alias for template compatibility
  openAddModal(): void {
    this.openCreateModal();
  }

  openEditModal(category: Category): void {
    this.editingCategory = category;
    this.categoryForm.patchValue({
      name: category.name,
      description: category.description,
      imageUrl: category.imageUrl,
      active: category.active,
      displayOrder: category.displayOrder
    });
    this.showModal = true;
    this.error = '';
    this.successMessage = '';
    this.cdr.detectChanges();
  }

  // Alias for template compatibility
  editCategory(category: Category): void {
    this.openEditModal(category);
  }

  closeModal(): void {
    this.showModal = false;
    this.editingCategory = null;
    this.categoryForm.reset();
    this.error = '';
    this.cdr.detectChanges();
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      this.cdr.detectChanges();
      return;
    }

    this.submitting = true;
    this.error = '';
    const formValue: CategoryRequest = this.categoryForm.value;

    if (this.editingCategory) {
      // Update existing category
      this.categoryService.updateCategory(this.editingCategory.id, formValue).subscribe({
        next: (category: Category) => {
          this.successMessage = 'Category updated successfully!';
          this.loadCategories();
          this.submitting = false;
          setTimeout(() => this.closeModal(), 1500);
          this.cdr.detectChanges();
        },
        error: (error: any) => {
          console.error('Error updating category:', error);
          this.error = error.error?.message || 'Failed to update category';
          this.submitting = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      // Create new category
      this.categoryService.createCategory(formValue).subscribe({
        next: (category: Category) => {
          this.successMessage = 'Category created successfully!';
          this.loadCategories();
          this.submitting = false;
          setTimeout(() => this.closeModal(), 1500);
          this.cdr.detectChanges();
        },
        error: (error: any) => {
          console.error('Error creating category:', error);
          this.error = error.error?.message || 'Failed to create category';
          this.submitting = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  // Alias for template compatibility
  saveCategory(): void {
    this.onSubmit();

  }

  toggleStatus(category: Category): void {
    this.categoryService.toggleCategoryStatus(category.id).subscribe({
      next: (updatedCategory: Category) => {
        this.successMessage = `Category "${updatedCategory.name}" is now ${updatedCategory.active ? 'Active' : 'Inactive'}`;
        this.loadCategories();
        setTimeout(() => this.successMessage = '', 3000);
        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Error toggling category status:', error);
        this.error = 'Failed to update category status';
        setTimeout(() => this.error = '', 3000);
        this.cdr.detectChanges();
      }
    });
  }

  deleteCategory(category: Category): void {
    if (category.productCount > 0) {
      Swal.fire({
        title: 'Cannot Delete Category',
        text: `Cannot delete category "${category.name}" because it has ${category.productCount} product(s). Please delete or reassign the products first.`,
        icon: 'error'
      });
      this.cdr.detectChanges();
      return;
    }

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
        this.categoryService.deleteCategory(category.id).subscribe({
          next: () => {
            this.successMessage = 'Category deleted successfully!';
            this.loadCategories();
            setTimeout(() => this.successMessage = '', 3000);
            this.cdr.detectChanges();
          },
          error: (error: any) => {
            console.error('Error deleting category:', error);
            this.error = error.error?.message || 'Failed to delete category';
            setTimeout(() => this.error = '', 3000);
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.categoryForm.get(fieldName);
    if (field?.touched && field?.errors) {
      if (field.errors['required']) return 'This field is required';
      if (field.errors['minlength']) return `Minimum length is ${field.errors['minlength'].requiredLength}`;
      if (field.errors['maxlength']) return `Maximum length is ${field.errors['maxlength'].requiredLength}`;
    }
    return '';
  }

  // Getters for template compatibility
  get isEditing(): boolean {
    return !!this.editingCategory;
  }

  get isSaving(): boolean {
    return this.submitting;
  }

  onImageError(event: any): void {
    event.target.src = 'assets/placeholder.png';
  }
}
