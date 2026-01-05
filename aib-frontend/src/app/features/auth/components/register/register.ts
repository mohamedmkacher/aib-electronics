import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '@services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.html',
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isLoading = false;
  successMessage = '';
  errorMessage = '';
  showPassword = false;
  showConfirmPassword = false;

  // Password strength indicator
  passwordStrength: 'weak' | 'medium' | 'strong' | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.setupPasswordStrengthWatcher();
  }

  /**
   * Initialize the registration form with enhanced validation
   */
  private initializeForm(): void {
    this.registerForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email, this.emailValidator()]],
      phone: [''],  // Optional - no validators
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
    this.registerForm.get('password')?.valueChanges.subscribe(password => {
      this.updatePasswordStrength(password);
    });
  }

  /**
   * Custom email validator with strict validation
   */
  private emailValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) {
        return null;
      }

      // More strict email validation
      const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      const valid = emailRegex.test(control.value);

      return valid ? null : { invalidEmail: true };
    };
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
   * Handle form submission
   */
  onSubmit(): void {
    // Validate form
    if (!this.registerForm.valid || this.isLoading) {
      this.markFormGroupTouched(this.registerForm);
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Prepare form data (exclude confirmPassword and handle empty phone)
    const { confirmPassword, phone, ...formData } = this.registerForm.value;

    // Only include phone if it has a value
    const registrationData = {
      ...formData,
      ...(phone && phone.trim() !== '' ? { phone: phone.trim() } : {})
    };

    // Call registration service
    this.authService.register(registrationData).subscribe({
      next: (response) => {
        console.log('✅ Registration success:', response);
        this.isLoading = false;

        // Show success message
        this.successMessage = response.message ||
          'Registration successful! Please check your email to verify your account.';
        this.errorMessage = '';

        // Reset form
        this.registerForm.reset();
        this.passwordStrength = null;

        // Redirect to login after 3 seconds
        setTimeout(() => {
          this.router.navigate(['/login'], {
            queryParams: { registered: 'true' }
          });
        }, 3000);
      },
      error: (error) => {
        console.error('❌ Registration error:', error);
        this.isLoading = false;

        // Show error message
        this.errorMessage = error.message ||
          'Registration failed. Please try again.';
        this.successMessage = '';
      }
    });
  }

  /**
   * Toggle password visibility
   */
  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  /**
   * Toggle confirm password visibility
   */
  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
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

  // Getter methods for form controls - easier access in template
  get firstName() { return this.registerForm.get('firstName'); }
  get lastName() { return this.registerForm.get('lastName'); }
  get email() { return this.registerForm.get('email'); }
  get phone() { return this.registerForm.get('phone'); }
  get password() { return this.registerForm.get('password'); }
  get confirmPassword() { return this.registerForm.get('confirmPassword'); }
}
