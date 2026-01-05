// src/app/app.config.ts
import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';   // AJOUT
import { ToastrModule } from 'ngx-toastr';                                 // AJOUT

import { routes } from './app.routes';
import { authInterceptor } from '@interceptors/auth.interceptor';
import {provideSweetAlert2} from '@sweetalert2/ngx-sweetalert2';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),

    provideHttpClient(
      withInterceptors([authInterceptor])
    ),
    provideSweetAlert2(),

    provideAnimations(),                    // Obligatoire pour les animations toastr
    importProvidersFrom(
      ToastrModule.forRoot({                // Configuration globale de toastr
        timeOut: 4000,
        positionClass: 'toast-top-right',
        preventDuplicates: true,
        progressBar: true,
        closeButton: true
      })
    )
  ]
};
