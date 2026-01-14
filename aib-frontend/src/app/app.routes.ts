import { Routes } from '@angular/router';
import { authGuard } from '@guards/auth.guard';
import {  adminGuard } from '@guards/admin.guard';
import { Checkout } from '@features/cart/components/checkout/checkout';
import { OrderSuccess } from '@features/cart/components/order-success/order-success';
import { CartIconComponent } from '@shared/components/cart-icon/cart-icon';
import { Cart } from '@features/cart/components/cart/cart';
import { OrderListComponent } from '@features/orders/components/order-list/order-list';
import { OrderDetailsComponent } from '@features/orders/components/order-details/order-details';
import { ContactComponent } from '@features/home/components/contact/contact';
import { Dashboard } from '@features/admin/components/dashboard/dashboard';
export const routes: Routes = [
   {
    path: 'admin/dashboard',
    component: Dashboard
  },
   {
    path: 'contact',
    loadComponent: () => import('@features/home/components/contact/contact').then(m => m.ContactComponent)
  },
 {
    path: 'orders/:id',
    loadComponent: () => import('@features/orders/components/order-details/order-details').then(m => m.OrderDetailsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'cart',
    loadComponent: () => import('@features/cart/components/cart/cart').then(m => m.Cart)
  },
   {
    path: 'orders',
    loadComponent: () => import('@features/orders/components/order-list/order-list').then(m => m.OrderListComponent)
  },
  {
    path: '',
    loadComponent: () => import('@features/home/components/home/home').then(m => m.HomeComponent)
  },
  {
    path: 'auth/login',
    loadComponent: () => import('@features/auth/components/login/login').then(m => m.LoginComponent)
  },
  { path: 'checkout', component: Checkout },
{ path: 'order-success', component: OrderSuccess },
  {
    path: 'auth/register',
    loadComponent: () => import('@features/auth/components/register/register').then(m => m.RegisterComponent)
  },
  {
    path: 'auth/verify-email',
    loadComponent: () => import('@features/auth/components/verify-email/verify-email')
      .then(m => m.VerifyEmailComponent)
  },
  {
    path: 'auth/forgot-password',
    loadComponent: () => import('@features/auth/components/forgot-password/forgot-password')
      .then(m => m.ForgotPasswordComponent)
  },
  {
    path: 'auth/reset-password',
    loadComponent: () => import('@features/auth/components/reset-password/reset-password')
      .then(m => m.ResetPasswordComponent)
  },
  {
    path: 'products',
    loadComponent: () => import('@features/products/components/product-list/product-list')
      .then(m => m.ProductListComponent)
  },
  {
    path: 'products/:slug',
    loadComponent: () => import('@features/products/components/product-detail/product-detail')
      .then(m => m.ProductDetailComponent)
  },
  {
    path: 'categories',
    loadComponent: () => import('@features/products/components/category-list/category-list')
      .then(m => m.CategoryListComponent)
  },
  {
    path: 'profile',
    loadComponent: () => import('@features/profile/components/profile/profile').then(m => m.ProfileComponent),
    canActivate: [authGuard]
  },

  // Admin routes
  {
    path: 'admin/categories',
    loadComponent: () => import('@features/admin/components/category-management/category-management')
      .then(m => m.AdminCategoryManagementComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'admin/products',
    loadComponent: () => import('@features/admin/components/product-management/product-management')
      .then(m => m.AdminProductManagementComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'admin/orders',
    loadComponent: () => import('@features/admin/components/order-management/order-management')
      .then(m => m.AdminOrderManagementComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'admin/users',
    loadComponent: () => import('@features/admin/components/user-management/user-management.component')
      .then(m => m.UserManagementComponent),
    canActivate: [authGuard, adminGuard]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
