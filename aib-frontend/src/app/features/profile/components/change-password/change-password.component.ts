import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import {ChangePasswordRequest, UserService} from '@services/user.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  passwordForm: FormGroup;
  submitting = false;
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private toastr: ToastrService
  ) {
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8), this.passwordStrengthValidator]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  passwordStrengthValidator(control: AbstractControl) {
    const value = control.value;
    if (!value) return null;

    const hasUpperCase = /[A-Z]+/.test(value);
    const hasLowerCase = /[a-z]+/.test(value);
    const hasNumeric = /[0-9]+/.test(value);
    const hasSpecial = /[@#$%^&+=!]+/.test(value);

    const valid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecial;
    return valid ? null : { weakPassword: true };
  }

  get hasUpperCase(): boolean {
    return /[A-Z]+/.test(this.passwordForm.get('newPassword')?.value || '');
  }

  get hasLowerCase(): boolean {
    return /[a-z]+/.test(this.passwordForm.get('newPassword')?.value || '');
  }

  get hasNumeric(): boolean {
    return /[0-9]+/.test(this.passwordForm.get('newPassword')?.value || '');
  }

  get hasSpecial(): boolean {
    return /[@#$%^&+=!]+/.test(this.passwordForm.get('newPassword')?.value || '');
  }

  get hasMinLength(): boolean {
    return (this.passwordForm.get('newPassword')?.value || '').length >= 8;
  }

  onSubmit(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const request: ChangePasswordRequest = this.passwordForm.value;

    this.userService.changePassword(request).subscribe({
      next: () => {
        this.toastr.success('Password changed successfully');
        this.passwordForm.reset();
        this.submitting = false;
      },
      error: (err: any) => {
        this.toastr.error(err.error?.message || 'Failed to change password');
        this.submitting = false;
      }
    });
  }
}
