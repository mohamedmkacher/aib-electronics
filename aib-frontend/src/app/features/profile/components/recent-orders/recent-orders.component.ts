import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { OrderService, Order } from '@services/order-service'; // Chemin relatif corrigé

@Component({
  selector: 'app-recent-orders',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './recent-orders.component.html',

})
export class RecentOrdersComponent implements OnInit {
  orders: Order[] = [];
  loading = true;

  constructor(private orderService: OrderService,private cdr:ChangeDetectorRef) {}

  ngOnInit(): void {
    this.orderService.getUserOrders().subscribe({
      next: (orders: Order[]) => {
        // Sort by date desc and take first 3
        this.orders = orders
          .sort((a: Order, b: Order) => new Date(this.getDate(b.createdAt)).getTime() - new Date(this.getDate(a.createdAt)).getTime())
          .slice(0, 3);
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => this.loading = false
    });
  }

  getDate(dateInput: string | number[]): string {
    if (Array.isArray(dateInput)) {
      return new Date(dateInput[0], dateInput[1] - 1, dateInput[2], dateInput[3], dateInput[4], dateInput[5]).toISOString();
    }
    return dateInput;
  }

  getStatusClass(status: string): string {
    const classes: {[key: string]: string} = {
      'PLACED': 'text-blue-400 bg-blue-400/10 border-blue-400/20',
      'PROCESSING': 'text-yellow-400 bg-yellow-400/10 border-yellow-400/20',
      'SHIPPED': 'text-purple-400 bg-purple-400/10 border-purple-400/20',
      'DELIVERED': 'text-[#00ff88] bg-[#00ff88]/10 border-[#00ff88]/20',
      'CANCELLED': 'text-red-400 bg-red-400/10 border-red-400/20'
    };
    return classes[status] || 'text-gray-400 bg-gray-400/10 border-gray-400/20';
  }
}
