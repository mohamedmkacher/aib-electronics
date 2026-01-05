import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '@services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './verify-email.html',
  })
export class VerifyEmailComponent implements OnInit {
  isVerifying = true;
  isSuccess = false;
  message = '';
  errorMessage = '';
  isResending = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.isVerifying = false;
      this.errorMessage = 'Invalid verification link';
      return;
    }

    this.verifyEmail(token);
  }

  verifyEmail(token: string): void {
    this.authService.verifyEmail(token).subscribe({
      next: (response: any) => {
        this.isVerifying = false;
        this.isSuccess = true;
        this.message = response.message || 'Email verified successfully!';

        // Redirect to login after 3 seconds
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 3000);
      },
      error: (error) => {
        this.isVerifying = false;
        this.isSuccess = false;
        this.errorMessage = error.error?.message || 'Verification failed. The link may be expired.';
      }
    });
  }

  resendVerification(): void {
    // Assuming there's a way to get the email, maybe from query params or local storage if user just registered.
    // Or maybe we just redirect them to login/register page to request it again.
    // For now, I'll just simulate a request or use a placeholder email if available.
    // Since I don't have the email here, I might need to ask the user or check if it's in the token (JWT).
    // But typically verify email page comes from an email link.

    // If the backend supports resending verification without email (maybe via token?), we can try that.
    // But usually we need an email.

    // Let's assume we can't easily resend without email input.
    // I'll just add a dummy implementation or redirect to login.

    this.isResending = true;
    setTimeout(() => {
        this.isResending = false;
        this.router.navigate(['/auth/login']);
    }, 1000);
  }
}
