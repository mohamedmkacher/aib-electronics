// src/app/core/services/address.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { TokenService } from '@services/token.service';
import { Address } from '@shared/models/address.model';

export type { Address }; // Re-export Address as type

@Injectable({
  providedIn: 'root'
})
export class AddressService {
  private readonly apiUrl = 'http://localhost:8088/api/addresses';

  private addressesSubject = new BehaviorSubject<Address[]>([]);
  public addresses$ = this.addressesSubject.asObservable();

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

  /**
   * Get all addresses for current user
   */
  getUserAddresses(): Observable<Address[]> {
    return this.http.get<Address[]>(this.apiUrl, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    }).pipe(
      tap(addresses => this.addressesSubject.next(addresses))
    );
  }

  /**
   * Create a new address
   */
  createAddress(address: Address): Observable<Address> {
    return this.http.post<Address>(this.apiUrl, address, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    }).pipe(
      tap(() => this.getUserAddresses().subscribe())
    );
  }

  /**
   * Update an existing address
   */
  updateAddress(addressId: number, address: Address): Observable<Address> {
    return this.http.put<Address>(`${this.apiUrl}/${addressId}`, address, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    }).pipe(
      tap(() => this.getUserAddresses().subscribe())
    );
  }

  /**
   * Delete an address
   */
  deleteAddress(addressId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${addressId}`, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    }).pipe(
      tap(() => this.getUserAddresses().subscribe())
    );
  }

  /**
   * Set an address as default
   */
  setDefaultAddress(addressId: number): Observable<Address> {
    return this.http.patch<Address>(`${this.apiUrl}/${addressId}/set-default`, {}, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    }).pipe(
      tap(() => this.getUserAddresses().subscribe())
    );
  }

  /**
   * Get current addresses from cache
   */
  getCurrentAddresses(): Address[] {
    return this.addressesSubject.value;
  }

  /**
   * Get default address from cache
   */
  getDefaultAddress(): Address | undefined {
    return this.addressesSubject.value.find(a => a.isDefault);
  }
}
