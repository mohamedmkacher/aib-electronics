// User model interfaces for AIB Electronics Platform

/**
 * User interface
 */
export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  emailVerified: boolean;
  enabled?: boolean;
  provider?: string;
  roles: string[];
  createdAt: string;
  lastLogin?: string;
}

/**
 * Register request interface
 */
export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  password: string;
}

/**
 * Login request interface
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * Authentication response interface
 */
export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  email: string;
  fullName: string;
  phone?: string;
  provider?: string; // Ajout du provider
  roles: string[];
  message?: string;
}

/**
 * API response interface
 */
export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
}

/**
 * Error response interface
 */
export interface ErrorResponse {
  message: string;
  status?: number;
  error?: any;
}

/**
 * Google OAuth credential interface
 */
export interface GoogleCredential {
  token: string;
}

/**
 * Email verification interface
 */
export interface EmailVerification {
  token: string;
}

/**
 * Password reset request interface
 */
export interface PasswordResetRequest {
  email: string;
}

/**
 * Password reset confirmation interface
 */
export interface PasswordResetConfirmation {
  token: string;
  newPassword: string;
}

/**
 * User update request interface
 */
export interface UserUpdateRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
}

/**
 * Change password request interface
 */
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
