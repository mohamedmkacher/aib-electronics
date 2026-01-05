import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenService } from '@services/token.service';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenService = inject(TokenService);
  const router = inject(Router);
  const token = tokenService.getToken();

  // Clone request and add authorization header if token exists
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // Handle errors
  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        // Unauthorized - clear token and redirect to login
        tokenService.clear();
        router.navigate(['/auth/login']);
      }
      return throwError(() => error);
    })
  );
};
