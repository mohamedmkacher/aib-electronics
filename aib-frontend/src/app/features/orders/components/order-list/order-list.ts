// src/app/pages/order-list/order-list.component.ts
import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService, Order, OrderItem } from '@services/order-service';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './order-list.html'
})
export class OrderListComponent implements OnInit {
  allOrders: Order[] = [];
  orders: Order[] = [];
  loading = false;
  error = '';

  // Filter
  statusFilter = '';

  // Pagination
  currentPage = 0;
  pageSize = 10;
  totalPages = 0;

  constructor(private orderService: OrderService,
              private cdr: ChangeDetectorRef ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.error = '';

    this.orderService.getUserOrders().subscribe({
      next: (orders) => {
        this.allOrders = orders.sort((a, b) => {
          const dateA = Array.isArray(a.createdAt) ? new Date(a.createdAt[0], a.createdAt[1] - 1, a.createdAt[2], a.createdAt[3], a.createdAt[4], a.createdAt[5]) : new Date(a.createdAt);
          const dateB = Array.isArray(b.createdAt) ? new Date(b.createdAt[0], b.createdAt[1] - 1, b.createdAt[2], b.createdAt[3], b.createdAt[4], b.createdAt[5]) : new Date(b.createdAt);
          return dateB.getTime() - dateA.getTime();
        });
        this.applyFilterAndPagination();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Erreur chargement commandes:', error);
        this.error = 'Impossible de charger vos commandes. Veuillez réessayer.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  filterOrders(): void {
    this.currentPage = 0;
    this.applyFilterAndPagination();
    this.cdr.detectChanges();
  }

  applyFilterAndPagination(): void {
    let filtered = this.allOrders;

    if (this.statusFilter) {
      filtered = filtered.filter(order => order.status === this.statusFilter);
    }

    this.totalPages = Math.ceil(filtered.length / this.pageSize);

    const startIndex = this.currentPage * this.pageSize;
    this.orders = filtered.slice(startIndex, startIndex + this.pageSize);
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.applyFilterAndPagination();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.applyFilterAndPagination();
    }
  }

  // Style pour le badge de statut
  getStatusStyle(status: string): { [key: string]: string } {
    const styles: { [key: string]: { [key: string]: string } } = {
      'PLACED': {
        'background-color': 'rgba(59, 130, 246, 0.2)',
        'color': '#60a5fa',
        'border': '1px solid rgba(59, 130, 246, 0.3)'
      },
      'PROCESSING': {
        'background-color': 'rgba(168, 85, 247, 0.2)',
        'color': '#c084fc',
        'border': '1px solid rgba(168, 85, 247, 0.3)'
      },
      'SHIPPED': {
        'background-color': 'rgba(99, 102, 241, 0.2)',
        'color': '#818cf8',
        'border': '1px solid rgba(99, 102, 241, 0.3)'
      },
      'DELIVERED': {
        'background-color': 'rgba(0, 255, 136, 0.2)',
        'color': '#00ff88',
        'border': '1px solid rgba(0, 255, 136, 0.3)'
      },
      'CANCELLED': {
        'background-color': 'rgba(239, 68, 68, 0.2)',
        'color': '#f87171',
        'border': '1px solid rgba(239, 68, 68, 0.3)'
      }
    };
    return styles[status] || {
      'background-color': 'rgba(255, 255, 255, 0.1)',
      'color': '#ffffff',
      'border': '1px solid rgba(255, 255, 255, 0.2)'
    };
  }

  getPaymentStatusStyle(status: string): { [key: string]: string } {
    const styles: { [key: string]: { [key: string]: string } } = {
      'PENDING': {
        'background-color': 'rgba(234, 179, 8, 0.2)',
        'color': '#facc15'
      },
      'PAID': {
        'background-color': 'rgba(0, 255, 136, 0.2)',
        'color': '#00ff88'
      },
      'FAILED': {
        'background-color': 'rgba(239, 68, 68, 0.2)',
        'color': '#f87171'
      },
      'REFUNDED': {
        'background-color': 'rgba(255, 255, 255, 0.1)',
        'color': '#ffffff'
      }
    };
    return styles[status] || {
      'background-color': 'rgba(255, 255, 255, 0.1)',
      'color': '#ffffff'
    };
  }

  formatDate(dateInput: string | number[]): string {
    if (!dateInput) return 'Date inconnue';

    let date: Date;
    if (Array.isArray(dateInput)) {
      date = new Date(dateInput[0], dateInput[1] - 1, dateInput[2], dateInput[3], dateInput[4], dateInput[5]);
    } else {
      date = new Date(dateInput);
    }

    if (isNaN(date.getTime())) return 'Date invalide';

    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getTotalItems(order: Order): number {
    return order.items.reduce((total: number, item: OrderItem) => total + item.quantity, 0);
  }

  getStatusText(status: string): string {
    const statusTexts: { [key: string]: string } = {
      'PLACED': 'Placed',
      'PROCESSING': 'Processing',
      'SHIPPED': 'Shipped',
      'DELIVERED': 'Delivered',
      'CANCELLED': 'Cancelled'
    };
    return statusTexts[status] || status;
  }

  getPaymentStatusText(status: string): string {
    const statusTexts: { [key: string]: string } = {
      'PENDING': 'Pending',
      'PAID': 'Paid',
      'FAILED': 'Failed',
      'REFUNDED': 'Refunded'
    };
    return statusTexts[status] || status;
  }

  formatCurrency(amount: number | string): string {
    if (amount == null) return '';

    const numericAmount = typeof amount === 'string' ? parseFloat(amount) : amount;

    if (isNaN(numericAmount)) return '';

    return new Intl.NumberFormat('fr-TN', {
      style: 'currency',
      currency: 'TND',
      minimumFractionDigits: 3,
      maximumFractionDigits: 3
    }).format(numericAmount);
  }
}
