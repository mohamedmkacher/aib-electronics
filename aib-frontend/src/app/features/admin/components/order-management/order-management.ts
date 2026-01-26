import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { OrderService } from '@services/order-service';
import { Order, OrderSearchRequest } from '@shared/models/order.model';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-admin-order-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './order-management.html',
  styleUrls: ['./order-management.css']
})
export class AdminOrderManagementComponent implements OnInit {
  orders: Order[] = [];
  loading = false;
  selectedOrder: Order | null = null;
  showDetailsModal = false;

  // Filters
  searchRequest: OrderSearchRequest = {
    page: 0,
    size: 10,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  };

  // Pagination
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;

  // Status options
  filterStatusOptions = ['PLACED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
  updateStatusOptions = ['PLACED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];

  constructor(
    private orderService: OrderService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.orderService.searchOrders(this.searchRequest).subscribe({
      next: (response) => {
        this.orders = response.orders;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.toastr.error('Failed to load orders');
        this.loading = false;
      }
    });
  }

  onFilterChange(): void {
    this.searchRequest.page = 0;
    this.loadOrders();
  }

  onPageChange(page: number): void {
    this.searchRequest.page = page;
    this.currentPage = page;
    this.loadOrders();
  }

  viewOrderDetails(order: Order): void {
    this.orderService.getOrderDetailsForAdmin(order.id).subscribe({
      next: (fullOrder) => {
        this.selectedOrder = fullOrder;
        this.showDetailsModal = true;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading order details:', error);
        this.toastr.error('Failed to load order details');
      }
    });
  }

  closeModal(): void {
    this.showDetailsModal = false;
    this.selectedOrder = null;
  }

  updateStatus(newStatus: string): void {
    if (!this.selectedOrder) return;

    Swal.fire({
      title: 'Update Order Status?',
      text: `Are you sure you want to change status to ${newStatus}? This will send an email to the customer.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, update it!',
      cancelButtonText: 'No, cancel',
      customClass: {
        confirmButton: 'btn-cyber',
        cancelButton: 'btn-cyber-outline'
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.orderService.updateOrderStatus(this.selectedOrder!.id, newStatus).subscribe({
          next: (updatedOrder) => {
            this.toastr.success(`Order status updated to ${newStatus}`);
            this.selectedOrder = updatedOrder;
            // Update in list
            const index = this.orders.findIndex(o => o.id === updatedOrder.id);
            if (index !== -1) {
              this.orders[index] = updatedOrder;
            }
            this.cdr.detectChanges();
          },
          error: (error) => {
            console.error('Error updating status:', error);
            this.toastr.error(error.error?.message || 'Failed to update status');
          }
        });
      }
    });
  }

  getStatusBadgeClass(status: string): string {
    const classes: {[key: string]: string} = {
      'PLACED': 'bg-blue-500/20 text-blue-400 border-blue-500/30',
      'PROCESSING': 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30',
      'SHIPPED': 'bg-purple-500/20 text-purple-400 border-purple-500/30',
      'DELIVERED': 'bg-green-500/20 text-green-400 border-green-500/30',
      'CANCELLED': 'bg-red-500/20 text-red-400 border-red-500/30'
    };
    return classes[status] || 'bg-gray-500/20 text-gray-400';
  }

  formatDate(dateInput: string | number[]): string {
    if (!dateInput) return 'N/A';
    let date: Date;
    if (Array.isArray(dateInput)) {
      date = new Date(dateInput[0], dateInput[1] - 1, dateInput[2], dateInput[3], dateInput[4], dateInput[5]);
    } else {
      date = new Date(dateInput);
    }
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  }
}
