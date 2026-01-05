import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '@environments/environment';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html',
  })
export class ResetPasswordComponent implements OnInit {
  resetPasswordForm!: FormGroup;
  isLoading = false;
  isValidating = true;
  errorMessage = '';
  successMessage = '';
  token = '';
  showPassword = false;
  showConfirmPassword = false;

  // Password strength indicator
  passwordStrength: 'weak' | 'medium' | 'strong' | null = null;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.setupPasswordStrengthWatcher();

    // Get token from query params
    this.token = this.route.snapshot.queryParamMap.get('token') || '';

    if (!this.token) {
      this.errorMessage = 'Invalid reset link. No token provided.';
      this.isValidating = false;
      return;
    }

    this.validateToken();
  }

  /**
   * Initialize the reset password form with enhanced validation
   */
  private initializeForm(): void {
    this.resetPasswordForm = this.fb.group({
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(100),
        this.passwordStrengthValidator()
      ]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  /**
   * Setup password strength watcher
   */
  private setupPasswordStrengthWatcher(): void {
    this.resetPasswordForm.get('password')?.valueChanges.subscribe(password => {
      this.updatePasswordStrength(password);
    });
  }

  /**
   * Password strength validator - checks for required character types
   */
  private passwordStrengthValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }

      const password = control.value;
      const errors: any = {};

      // Check for uppercase letter
      if (!/[A-Z]/.test(password)) {
        errors.noUppercase = true;
      }

      // Check for lowercase letter
      if (!/[a-z]/.test(password)) {
        errors.noLowercase = true;
      }

      // Check for number
      if (!/[0-9]/.test(password)) {
        errors.noNumber = true;
      }

      // Check for special character
      if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
        errors.noSpecial = true;
      }

      return Object.keys(errors).length > 0 ? errors : null;
    };
  }

  /**
   * Custom validator to check if passwords match
   */
  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  /**
   * Update password strength indicator based on password complexity
   */
  updatePasswordStrength(password: string): void {
    if (!password) {
      this.passwordStrength = null;
      return;
    }

    let strength = 0;

    // Length check
    if (password.length >= 8) strength++;
    if (password.length >= 12) strength++;

    // Character variety
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/[0-9]/.test(password)) strength++;
    if (/[!@#$%^&*(),.?":{}|<>]/.test(password)) strength++;

    if (strength <= 2) {
      this.passwordStrength = 'weak';
    } else if (strength <= 4) {
      this.passwordStrength = 'medium';
    } else {
      this.passwordStrength = 'strong';
    }
  }

  /**
   * Validate reset token
   */
  validateToken(): void {
    this.http.get(`${environment.apiUrl}/password-reset/validate-token?token=${this.token}`)
      .subscribe({
        next: () => {
          this.isValidating = false;
        },
        error: (error) => {
          this.isValidating = false;
          this.errorMessage = error.error?.message || 'Invalid or expired reset link. Please request a new one.';
        }
      });
  }

  /**
   * Toggle password visibility
   */
  togglePasswordVisibility(field: 'password' | 'confirmPassword'): void {
    if (field === 'password') {
      this.showPassword = !this.showPassword;
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (this.resetPasswordForm.invalid) {
      this.markFormGroupTouched(this.resetPasswordForm);
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      token: this.token,
      password: this.resetPasswordForm.value.password,
      confirmPassword: this.resetPasswordForm.value.confirmPassword
    };

    this.http.post(`${environment.apiUrl}/password-reset/reset-password`, payload)
      .subscribe({
        next: (response: any) => {
          this.isLoading = false;
          this.successMessage = response.message || 'Password reset successfully! You can now login with your new password.';
          this.resetPasswordForm.reset();
          this.passwordStrength = null;

          // Redirect to login after 3 seconds
          setTimeout(() => {
            this.router.navigate(['/login'], {
              queryParams: { passwordReset: 'true' }
            });
          }, 3000);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = error.error?.message || 'Failed to reset password. Please try again.';
        }
      });
  }

  /**
   * Mark all form controls as touched to show validation errors
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      }
    });
  }

  // Getter methods for form controls
  get password() {
    return this.resetPasswordForm.get('password');
  }

  get confirmPassword() {
    return this.resetPasswordForm.get('confirmPassword');
  }

  // Alias for template compatibility
  get resetForm(): FormGroup {
    return this.resetPasswordForm;
  }
}
