import { Product } from './product.model';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {TokenService} from '@services/token.service';
import {Injectable} from '@angular/core';

export interface OrderItem {
  id: number;
  product: Product;
  quantity: number;
  price: number;
  discount: number;
  totalPrice: number;
  unitPrice: number;
}

export interface Order {
  id: number;
  orderNumber: string;
  status: string;
  paymentStatus: string;
  subtotal: number;
  tax: number;
  shippingFee: number;
  total: number;
  createdAt: string | number[];
  paidAt: string | null;
  cancelledAt: string | null;
  refundedAt: string | null;
  cancellationReason: string | null;
  stripePaymentIntentId: string | null;
  stripeRefundId: string | null;
  shippingName: string;
  shippingPhone: string;
  shippingFullAddress: string;
  items: OrderItem[];
  user?: {
    firstName: string;
    lastName: string;
    email: string;
  };
}

export interface OrderSearchRequest {
  orderNumber?: string;
  customerName?: string;
  status?: string;
  startDate?: string;
  endDate?: string;
  minTotal?: number;
  maxTotal?: number;
  city?: string;
  productName?: string;
  sortBy?: string;
  sortDirection?: string;
  page?: number;
  size?: number;
}

export interface OrderListResponse {
  orders: Order[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly apiUrl = 'http://localhost:8088/api/orders';

  constructor(
    private http: HttpClient,
    private tokenService: TokenService
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.tokenService.getToken();
    return token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();
  }

  getUserOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(
      this.apiUrl,
      { headers: this.getAuthHeaders(), withCredentials: true }
    );
  }

  getOrderDetails(orderId: number): Observable<Order> {
    return this.http.get<Order>(
      `${this.apiUrl}/${orderId}`,
      { headers: this.getAuthHeaders(), withCredentials: true }
    );
  }
}
