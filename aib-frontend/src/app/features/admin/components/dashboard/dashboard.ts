// src/app/admin/dashboard/dashboard.ts
import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService, DashboardStats } from '../../services/dashboard';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
  standalone: true,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  stats: DashboardStats | null = null;
  loading = true;
  error: string | null = null;

  constructor(private dashboardService: DashboardService,
              private cdr: ChangeDetectorRef ) {}

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading = true;
    this.error = null;

    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
        console.log('Dashboard data loaded:', data);
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.error = 'Error loading dashboard data';
        this.loading = false;

        this.cdr.detectChanges();
      }
    });
  }

  getMaxSales(): number {
    if (!this.stats?.salesChart || this.stats.salesChart.length === 0) {
      return 1;
    }
    return Math.max(...this.stats.salesChart.map(p => Number(p.amount) || 0), 1);
  }

  // Calculate bar height percentage - ensures proper number conversion
  getBarHeight(amount: number): number {
    const numAmount = Number(amount) || 0;
    const maxSales = this.getMaxSales();
    const height = (numAmount / maxSales) * 100;
    // Minimum 2% height for visibility (like original)
    return height || 2;
  }

  getStatusBadgeClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'PROCESSING': 'bg-blue-100 text-blue-800',
      'SHIPPED': 'bg-purple-100 text-purple-800',
      'DELIVERED': 'bg-green-100 text-green-800',
      'CANCELLED': 'bg-red-100 text-red-800'
    };
    return statusMap[status] || 'bg-gray-100 text-gray-800';
  }

  getStatusLabel(status: string): string {
    const labelMap: { [key: string]: string } = {
      'PENDING': 'Pending',
      'PROCESSING': 'Processing',
      'SHIPPED': 'Shipped',
      'DELIVERED': 'Delivered',
      'CANCELLED': 'Cancelled'
    };
    return labelMap[status] || status;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'TND',
      minimumFractionDigits: 3
    }).format(amount);
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('en-US', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  }

  refresh(): void {
    this.loadDashboard();
  }
}
