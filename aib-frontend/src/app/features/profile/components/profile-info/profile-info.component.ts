import {Component, Input, OnInit, OnChanges, SimpleChanges, ChangeDetectorRef} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { User } from '@shared/models/user.model';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '@services/auth.service';
import {UpdateProfileRequest, UserService} from '@services/user.service';

@Component({
  selector: 'app-profile-info',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile-info.component.html',
  styleUrls: ['./profile-info.component.css']
})
export class ProfileInfoComponent implements OnInit, OnChanges {
  @Input() user: User | null = null;
  profileForm!: FormGroup;
  isEditing = false;
  submitting = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private toastr: ToastrService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.initForm();
    if (this.user) {
      this.profileForm.patchValue(this.user);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['user'] && this.user && this.profileForm) {
      this.profileForm.patchValue(this.user);
    }
  }

  initForm(): void {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
      phone: ['']
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
    if (!this.isEditing) {
      this.profileForm.patchValue(this.user!); // Reset if cancelling edit
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }
    this.submitting = true;
    const request: UpdateProfileRequest = this.profileForm.value;

    this.userService.updateProfile(request).subscribe({
      next: (updatedUser: User) => {
        this.toastr.success('Profile updated successfully!');
        this.authService.refreshCurrentUser(); // Refresh user in auth service
        this.user = updatedUser;
        this.isEditing = false;
        this.submitting = false;
      },
      error: (err: any) => {
        this.toastr.error(err.error?.message || 'Failed to update profile');
        this.submitting = false;
      }
    });
  }
}
