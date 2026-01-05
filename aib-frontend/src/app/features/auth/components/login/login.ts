// login.component.ts - CYBER-TECH REDESIGN
import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '@services/auth.service';
import { environment } from '@environments/environment';
import { CartService } from '@services/cart.service';
import Swal from 'sweetalert2';
import { forkJoin } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import {CartItem, CartResponse} from '@shared/models/cart.model';

declare const google: any;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent implements OnInit, AfterViewInit {
  loginForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;
  returnUrl: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cartService: CartService,
    private toastr: ToastrService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.returnUrl = params['returnUrl'] || null;
    });

    if (this.authService.isLoggedIn()) {
      if (this.authService.isAdmin()) {
        this.router.navigate(['/admin/dashboard']);
      } else {
        this.router.navigate([this.returnUrl || '/']);
      }
    }
  }

  ngAfterViewInit(): void {
    this.initializeGoogleSignIn();
  }

  initializeGoogleSignIn(): void {
    if (typeof google !== 'undefined') {
      google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: this.handleGoogleSignIn.bind(this),
        auto_select: false,
        cancel_on_tap_outside: true
      });

      google.accounts.id.renderButton(
        document.getElementById('google-signin-button'),
        {
          theme: 'filled_black',
          size: 'large',
          width: 320,
          text: 'continue_with',
          shape: 'pill'
        }
      );
    } else {
      console.error('Google Sign-In library not loaded');
    }
  }

  handleGoogleSignIn(response: any): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.authService.authenticateWithGoogle(response.credential).subscribe({
      next: () => {
        this.handleSuccessfulLogin();
      },
      error: (error) => {
        console.error('Google sign-in error', error);
        this.errorMessage = error.error?.message || 'Google sign-in failed. Please try again.';
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.handleSuccessfulLogin();
      },
      error: (error) => {
        console.error('Login error', error);
        this.errorMessage = error.error?.message || 'Invalid email or password';
        this.isLoading = false;
      }
    });
  }

  private handleSuccessfulLogin(): void {
    if (this.authService.isAdmin()) {
      this.router.navigate(['/admin/dashboard']);
      return;
    }

    if (this.returnUrl === '/checkout') {
      // Check if user already has a cart
      this.cartService.checkUserHasCart().subscribe(hasCart => {
        if (hasCart) {
          // Fetch both carts to display details
          forkJoin({
            anonCart: this.cartService.getAnonymousCart(),
            userCart: this.cartService.getUserCart()
          }).subscribe({
            next: ({ anonCart, userCart }) => {
              this.showCartConflictModal(anonCart, userCart);
            },
            error: (err) => {
              console.error('Error fetching carts:', err);
              // Fallback to simple merge if fetching fails
              this.cartService.mergeCart().subscribe(() => this.router.navigate([this.returnUrl]));
            }
          });
        } else {
          // No existing cart, just merge (which effectively moves items to user cart)
          this.cartService.mergeCart().subscribe(() => this.router.navigate([this.returnUrl]));
        }
      });
    } else {
      this.router.navigate([this.returnUrl || '/']);
    }
  }

  private showCartConflictModal(anonCart: CartResponse, userCart: CartResponse): void {
    const anonItemsHtml = this.generateCartItemsHtml(anonCart.items);
    const userItemsHtml = this.generateCartItemsHtml(userCart.items);

    Swal.fire({
      title: 'Cart Conflict',
      html: `
        <p class="mb-4 text-gray-300">You have items in your account cart. What would you like to do with your current items?</p>

        <div class="grid grid-cols-2 gap-4 text-left text-sm mb-6">
          <div class="p-3 bg-gray-800 rounded-lg border border-gray-700">
            <h4 class="font-bold text-blue-400 mb-2">Current Session Cart</h4>
            <div class="max-h-40 overflow-y-auto custom-scrollbar space-y-2">
              ${anonItemsHtml}
            </div>
            <p class="mt-2 pt-2 border-t border-gray-700 text-right font-bold text-white">
              Total: ${anonCart.total.toFixed(3)} TND
            </p>
          </div>

          <div class="p-3 bg-gray-800 rounded-lg border border-gray-700">
            <h4 class="font-bold text-purple-400 mb-2">Account Cart</h4>
            <div class="max-h-40 overflow-y-auto custom-scrollbar space-y-2">
              ${userItemsHtml}
            </div>
            <p class="mt-2 pt-2 border-t border-gray-700 text-right font-bold text-white">
              Total: ${userCart.total.toFixed(3)} TND
            </p>
          </div>
        </div>
      `,
      icon: 'question',
      showDenyButton: true,
      showCancelButton: true,
      confirmButtonText: 'Merge Carts',
      denyButtonText: 'Replace Account Cart',
      cancelButtonText: 'Keep Account Cart',
      width: '800px',
      customClass: {
        popup: 'glass-card-modal',
        title: 'text-white font-orbitron',
        htmlContainer: 'text-left',
        confirmButton: 'btn-cyber',
        denyButton: 'btn-cyber-deny',
        cancelButton: 'btn-cyber-outline'
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.cartService.mergeCart().subscribe(() => this.router.navigate([this.returnUrl]));
      } else if (result.isDenied) {
        this.cartService.replaceCart().subscribe(() => this.router.navigate([this.returnUrl]));
      } else {
        this.cartService.keepUserCart().subscribe(() => this.router.navigate([this.returnUrl]));
      }
    });
  }

  private generateCartItemsHtml(items: CartItem[]): string {
    if (!items || items.length === 0) return '<p class="text-gray-500 italic">Empty</p>';

    return items.map(item => `
      <div class="flex items-center gap-2">
        <img src="${item.imageUrl || 'assets/placeholder.png'}" class="w-8 h-8 rounded object-cover">
        <div class="flex-1 min-w-0">
          <p class="truncate text-white text-xs">${item.name}</p>
          <p class="text-gray-400 text-xs">x${item.quantity}</p>
        </div>
        <span class="text-blue-300 text-xs">${item.unitPrice.toFixed(3)}</span>
      </div>
    `).join('');
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }
}
