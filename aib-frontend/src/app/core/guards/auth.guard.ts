// src/app/core/guards/auth.guard.ts
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@services/auth.service';

export const authGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Use the isLoggedIn() method from your AuthService
  if (authService.isLoggedIn()) {
    return true;
  }

  // Redirect to login if not authenticated
  router.navigate(['/auth/login']);
  return false;
};
