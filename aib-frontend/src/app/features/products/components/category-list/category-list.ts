// category-list.component.ts
import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Category } from '@shared/models/category.model';
import {CategoryService} from '@services/category.service';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './category-list.html',
  })
export class CategoryListComponent implements OnInit {
  categories: Category[] = [];
  loading = false;
  error = '';

  constructor(private categoryService: CategoryService,
              private cdr: ChangeDetectorRef ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.error = '';

    this.categoryService.getActiveCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.error = 'Failed to load categories';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
