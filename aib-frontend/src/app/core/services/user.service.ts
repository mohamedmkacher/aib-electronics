import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { TokenService } from '@services/token.service';
import { User } from '@shared/models/user.model';

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = 'http://localhost:8088/api/users';

  constructor(
    private http: HttpClient,
    private tokenService: TokenService
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenService.getToken();
    return new HttpHeaders({
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    });
  }

  updateProfile(request: UpdateProfileRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/profile`, request, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    }).pipe(
      tap(updatedUser => {
        const currentUser = this.tokenService.getUser();
        if (currentUser) {
          const newUser = { ...currentUser, ...updatedUser };
          this.tokenService.saveUser(newUser);
        }
      })
    );
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/change-password`, request, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  // Admin methods
  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/admin/all`, { headers: this.getAuthHeaders() });
  }

  getCustomersOnly(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/admin/customers`, { headers: this.getAuthHeaders() });
  }

  getUserById(userId: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/admin/${userId}`, { headers: this.getAuthHeaders() });
  }

  updateUserRoles(userId: number, roles: string[]): Observable<User> {
    return this.http.patch<User>(`${this.apiUrl}/admin/${userId}/roles`, { roles }, { headers: this.getAuthHeaders() });
  }

  toggleUserStatus(userId: number): Observable<User> {
    return this.http.patch<User>(`${this.apiUrl}/admin/${userId}/toggle-status`, {}, { headers: this.getAuthHeaders() });
  }

  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/admin/${userId}`, { headers: this.getAuthHeaders() });
  }
}
