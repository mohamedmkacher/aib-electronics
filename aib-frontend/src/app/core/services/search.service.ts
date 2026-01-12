import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SearchService {
  private isSearchOpen = new BehaviorSubject<boolean>(false);
  isSearchOpen$ = this.isSearchOpen.asObservable();

  openSearch(): void {
    this.isSearchOpen.next(true);
  }

  closeSearch(): void {
    this.isSearchOpen.next(false);
  }
}
