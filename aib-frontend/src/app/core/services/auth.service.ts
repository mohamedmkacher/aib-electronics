import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, throwError, map, catchError } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '@environments/environment';
import { TokenService } from './token.service';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '@shared/models/user.model';
import { CartService } from '@services/cart.service'; // Chemin corrigé

// Define ApiResponse interface
interface ApiResponse {
  success: boolean;
  message: string;
  data?: any;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private tokenService: TokenService,
    private router: Router,
    private cartService: CartService
  ) {
    // Initialize user from storage
    const user = this.tokenService.getUser(); // Correction ici
    if (user) {
      this.currentUserSubject.next(user);
    }
  }

  /**
   * Register a new user
   */
  register(userData: RegisterRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.API_URL}/register`, userData)
      .pipe(
        map((response: ApiResponse) => {
          // Ensure response has expected structure
          if (!response.success) {
            throw new Error(response.message || 'Registration failed');
          }
          return response;
        }),
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Verify email with token
   */
  verifyEmail(token: string): Observable<any> {
    return this.http.get(`${this.API_URL}/verify-email?token=${token}`);
  }

  /**
   * Resend verification email
   */
  resendVerification(email: string): Observable<any> {
    return this.http.post(`${this.API_URL}/resend-verification`, { email });
  }

  /**
   * Login user
   */
  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, request)
      .pipe(
        tap(response => this.handleAuthSuccess(response))
      );
  }

  /**
   * Authenticate with Google
   */
  authenticateWithGoogle(credential: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/google`, { token: credential })
      .pipe(
        tap(response => this.handleAuthSuccess(response))
      );
  }

  /**
   * Get current user from server
   */
  getCurrentUser(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/me`)
      .pipe(
        tap(user => {
          this.tokenService.saveUser(user);
          this.currentUserSubject.next(user);
        })
      );
  }

  /**
   * Refresh current user data from server
   */
  refreshCurrentUser(): void {
    this.getCurrentUser().subscribe();
  }

  /**
   * Get current user value synchronously
   */
  getCurrentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Logout user
   */
  logout(): void {
    this.tokenService.clear();
    this.currentUserSubject.next(null);

    // Clear cart state immediately on logout
    this.cartService.clearCart();

    this.router.navigate(['/login']);
  }

  /**
   * Check if user is logged in
   */
  isLoggedIn(): boolean {
    return this.tokenService.isLoggedIn();
  }

  /**
   * Check if user has specific role
   */
  hasRole(role: string): boolean {
    const user = this.currentUserSubject.value;
    return user ? user.roles.includes(role) : false;
  }

  /**
   * Check if user is admin
   */
  isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }

  /**
   * Handle authentication success
   */
  private handleAuthSuccess(response: AuthResponse): void {
    // Save token
    this.tokenService.saveToken(response.token);

    // Create user object
    const user: User = {
      id: response.userId,
      email: response.email,
      firstName: response.fullName.split(' ')[0],
      lastName: response.fullName.split(' ').slice(1).join(' '),
      phone: response.phone || '',
      emailVerified: true,
      roles: response.roles,
      createdAt: new Date().toISOString(),
      provider: response.provider // Inclure le provider
    };

    // Save user
    this.tokenService.saveUser(user);
    this.currentUserSubject.next(user);

    // Refresh cart after login to merge anonymous cart with user cart
    this.cartService.loadCart();
  }

  /**
   * Handle HTTP errors
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred. Please try again.';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else if (error.error && error.error.message) {
      // Backend error with message
      errorMessage = error.error.message;
    } else if (error.status === 0) {
      // Network error
      errorMessage = 'Unable to connect to server. Please check your connection.';
    } else if (error.status === 409) {
      errorMessage = 'Email already exists. Please use a different email.';
    } else if (error.status === 400) {
      errorMessage = 'Invalid registration data. Please check your inputs.';
    }

    return throwError(() => ({ message: errorMessage }));
  }
}
