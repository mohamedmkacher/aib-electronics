import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, OrderListResponse, OrderSearchRequest } from '@shared/models/order.model';

// Re-export for compatibility
export type { Order, OrderItem } from '@shared/models/order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly apiUrl = 'http://localhost:8088/api/orders';

  constructor(private http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'application/json'
    });
  }

  getUserOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(this.apiUrl, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  getUserOrdersForAdmin(userId: number): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/admin/user/${userId}`, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  getOrderDetails(orderId: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/${orderId}`, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  getOrderDetailsForAdmin(orderId: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/admin/${orderId}`, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  cancelOrder(orderId: number, reason: string): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/${orderId}/cancel`, { reason }, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  canCancelOrder(orderId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${orderId}/can-cancel`, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  // Admin Methods
  searchOrders(request: OrderSearchRequest): Observable<OrderListResponse> {
    return this.http.post<OrderListResponse>(`${this.apiUrl}/admin/search`, request, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }

  updateOrderStatus(orderId: number, status: string): Observable<Order> {
    return this.http.patch<Order>(`${this.apiUrl}/${orderId}/status`, { status }, {
      headers: this.getAuthHeaders(),
      withCredentials: true
    });
  }
}
