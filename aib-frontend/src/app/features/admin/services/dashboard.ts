// src/app/admin/services/dashboard.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

// Interfaces
export interface DashboardStats {
  kpis: KpiStats;
  salesChart: SalesDataPoint[];
  ordersByStatus: OrderStatusCount[];
  topProducts: TopProduct[];
  recentOrders: RecentOrder[];
  lowStockProducts: ProductLowStock[];
}

export interface KpiStats {
  totalRevenue: number;
  revenueToday: number;
  revenueThisMonth: number;
  totalOrders: number;
  ordersToday: number;
  ordersThisMonth: number;
  totalProducts: number;
  lowStockProducts: number;
  totalUsers: number;
  newUsersThisMonth: number;
}

export interface SalesDataPoint {
  date: string;
  amount: number;
  orderCount: number;
}

export interface OrderStatusCount {
  status: string;
  count: number;
}

export interface TopProduct {
  id: number;
  name: string;
  imageUrl: string;
  totalSold: number;  // Changed from soldQuantity to match backend
  revenue: number;
}

export interface RecentOrder {
  orderId: number;
  orderNumber: string;
  customerName: string;
  total: number;
  status: string;
  createdAt: string;
}

export interface ProductLowStock {
  id: number;
  name: string;
  imageUrl: string;
  stock: number;
  category: string;
}

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private apiUrl = 'http://localhost:8088/api/admin/dashboard';

  constructor(private http: HttpClient) {}

  getStats(): Observable<DashboardStats> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`, {
      headers,
      withCredentials: true
    });
  }
}
