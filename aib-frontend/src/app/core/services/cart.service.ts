import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { TokenService } from '@services/token.service';
import {CartResponse} from '@shared/models/cart.model';



@Injectable({
  providedIn: 'root'
})
export class CartService {

  private readonly apiUrl = 'http://localhost:8088/api/cart';

  private cartSource = new BehaviorSubject<CartResponse | null>(null);
  public cart$ = this.cartSource.asObservable();

  public itemCount$ = this.cart$.pipe(map(cart => cart?.itemCount || 0));
  public total$ = this.cart$.pipe(map(cart => cart?.total || 0));
  public items$ = this.cart$.pipe(map(cart => cart?.items || []));

  constructor(
    private http: HttpClient,
    private tokenService: TokenService
  ) {
    this.loadCart();
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenService.getToken();
    return token
      ? new HttpHeaders({ 'Authorization': `Bearer ${token}` })
      : new HttpHeaders();
  }

  loadCart(): void {
    const user = this.tokenService.getUser();
    if (user && user.roles && user.roles.includes('ROLE_ADMIN')) {
      this.cartSource.next(null);
      return;
    }

    const headers = this.getAuthHeaders();
    this.http.get<CartResponse>(this.apiUrl, { headers, withCredentials: true })
      .pipe(
        catchError(err => {
          console.error('Error loading cart:', err);
          this.cartSource.next(null);
          return of(null);
        })
      )
      .subscribe(cartFromServer => {
        if (cartFromServer) {
          this.cartSource.next(cartFromServer);
        }
      });
  }

  addToCart(productId: number, quantity = 1): Observable<CartResponse> {
    const headers = this.getAuthHeaders();
    return this.http.post<CartResponse>(
      `${this.apiUrl}/add`,
      { productId, quantity },
      { headers, withCredentials: true }
    ).pipe(
      tap(cart => {
        this.cartSource.next(cart);
        this.loadCart();
      })
    );
  }

  updateQuantity(productId: number, quantity: number): Observable<CartResponse> {
    const headers = this.getAuthHeaders();
    return this.http.put<CartResponse>(
      `${this.apiUrl}/update`,
      { productId, quantity },
      { headers, withCredentials: true }
    ).pipe(
      tap(cart => this.cartSource.next(cart))
    );
  }

  removeFromCart(productId: number): Observable<CartResponse> {
    const headers = this.getAuthHeaders();
    return this.http.delete<CartResponse>(
      `${this.apiUrl}/remove/${productId}`,
      { headers, withCredentials: true }
    ).pipe(
      tap(cart => this.cartSource.next(cart))
    );
  }

  clearCart(): Observable<void> {
    const headers = this.getAuthHeaders();
    return this.http.delete<void>(
      `${this.apiUrl}/clear`,
      { headers, withCredentials: true }
    ).pipe(
      tap(() => this.cartSource.next(null))
    );
  }

  checkUserHasCart(): Observable<boolean> {
    const headers = this.getAuthHeaders();
    return this.http.get<boolean>(`${this.apiUrl}/has-cart`, { headers, withCredentials: true });
  }

  getAnonymousCart(): Observable<CartResponse> {
    const headers = this.getAuthHeaders();
    return this.http.get<CartResponse>(`${this.apiUrl}/anonymous`, { headers, withCredentials: true });
  }

  getUserCart(): Observable<CartResponse> {
    const headers = this.getAuthHeaders();
    return this.http.get<CartResponse>(`${this.apiUrl}/user`, { headers, withCredentials: true });
  }

  mergeCart(): Observable<CartResponse> {
    const headers = this.getAuthHeaders();
    return this.http.post<CartResponse>(
      `${this.apiUrl}/merge`,
      {},
      { headers, withCredentials: true }
    ).pipe(
      tap(cart => this.cartSource.next(cart))
    );
  }

  replaceCart(): Observable<CartResponse> {
    const headers = this.getAuthHeaders();
    return this.http.post<CartResponse>(
      `${this.apiUrl}/replace`,
      {},
      { headers, withCredentials: true }
    ).pipe(
      tap(cart => this.cartSource.next(cart))
    );
  }

  keepUserCart(): Observable<void> {
    const headers = this.getAuthHeaders();
    return this.http.post<void>(
      `${this.apiUrl}/keep`,
      {},
      { headers, withCredentials: true }
    );
  }

  refresh(): void {
    this.loadCart();
  }

  resetCart(): void {
    this.cartSource.next(null);
  }

  clearCartLocally(): void {
    this.cartSource.next(null);
  }

  getCurrentCart(): CartResponse | null {
    return this.cartSource.value;
  }

  hasItems(): boolean {
    const cart = this.cartSource.value;
    return cart !== null && cart.itemCount > 0;
  }

  onUserLogin(): void {
    this.loadCart();
  }

  onUserLogout(): void {
    this.resetCart();
  }
}
