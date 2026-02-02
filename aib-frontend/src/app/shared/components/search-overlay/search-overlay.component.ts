import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Product } from '@shared/models/product.model';
import { Observable, Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import {SearchService} from '@services/search.service';
import {ProductService} from '@services/product.service';

@Component({
  selector: 'app-search-overlay',
  standalone: true,
  imports: [CommonModule, FormsModule, ],
  templateUrl: './search-overlay.component.html',
  styleUrls: ['./search-overlay.component.css']
})
export class SearchOverlayComponent implements OnInit {
  isSearchOpen$: Observable<boolean>;
  searchTerm = new Subject<string>();
  results$: Observable<Product[]> | undefined;

  constructor(
    private searchService: SearchService,
    private productService: ProductService,
    private router: Router
  ) {
    this.isSearchOpen$ = this.searchService.isSearchOpen$;
  }

  ngOnInit(): void {
    this.results$ = this.searchTerm.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(term => this.productService.searchProducts({ keyword: term, size: 5 }).pipe(
        switchMap(response => [response.products])
      ))
    );
  }

  onSearch(event: Event): void {
    const term = (event.target as HTMLInputElement).value;
    this.searchTerm.next(term);
  }

  goToProduct(slug: string): void {
    this.router.navigate(['/products', slug]);
    this.closeSearch();
  }

  closeSearch(): void {
    this.searchService.closeSearch();
  }
}
