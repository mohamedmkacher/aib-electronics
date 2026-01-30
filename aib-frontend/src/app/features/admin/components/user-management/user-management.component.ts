import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { User } from '@shared/models/user.model';
import { OrderService } from '@services/order-service';
import { Order } from '@shared/models/order.model';
import Swal from 'sweetalert2';
import {UserService} from '@services/user.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  filteredUsers: User[] = [];
  loading = false;
  searchTerm = '';

  // Expanded user tracking
  expandedUserId: number | null = null;

  // Orders cache
  userOrders: { [userId: number]: Order[] } = {};
  loadingOrders: { [userId: number]: boolean } = {};

  // Order modal
  showOrderModal = false;
  selectedOrder: Order | null = null;

  constructor(
    private userService: UserService,
    private orderService: OrderService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  /**
   * Format date from backend (handles string | number[] type)
   */
  formatDate(date: string | number[] | null | undefined): string {
    if (!date) return 'N/A';

    try {
      if (Array.isArray(date)) {
        // Java LocalDateTime comes as [year, month, day, hour, minute, second, nano]
        const [year, month, day, hour = 0, minute = 0] = date;
        const dateObj = new Date(year, month - 1, day, hour, minute);
        return dateObj.toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'short',
          day: 'numeric'
        });
      } else {
        // ISO string format
        return new Date(date).toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'short',
          day: 'numeric'
        });
      }
    } catch {
      return 'N/A';
    }
  }

  loadUsers(): void {
    this.loading = true;
    // Use getCustomersOnly to get only ROLE_USER (backend filtering)
    this.userService.getCustomersOnly().subscribe({
      next: (users: User[]) => {
        this.users = users;
        this.filteredUsers = [...this.users];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Error loading users:', error);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  filterUsers(): void {
    if (!this.searchTerm.trim()) {
      this.filteredUsers = [...this.users];
      this.cdr.detectChanges();
      return;
    }

    const term = this.searchTerm.toLowerCase().trim();
    this.filteredUsers = this.users.filter(user =>
      user.firstName?.toLowerCase().includes(term) ||
      user.lastName?.toLowerCase().includes(term) ||
      user.email?.toLowerCase().includes(term) ||
      user.phone?.toLowerCase().includes(term) ||
      `${user.firstName} ${user.lastName}`.toLowerCase().includes(term)
    );
    this.cdr.detectChanges();
  }

  clearSearch(): void {
    this.searchTerm = '';
    this.filteredUsers = [...this.users];
    this.cdr.detectChanges();
  }

  toggleUserExpand(user: User): void {
    if (this.expandedUserId === user.id) {
      this.expandedUserId = null;
    } else {
      this.expandedUserId = user.id;
      this.loadUserOrders(user.id);
    }
    this.cdr.detectChanges();
  }

  loadUserOrders(userId: number): void {
    // Check if already loaded
    if (this.userOrders[userId]) {
      return;
    }

    this.loadingOrders[userId] = true;
    this.orderService.getUserOrdersForAdmin(userId).subscribe({
      next: (orders: Order[]) => {
        this.userOrders[userId] = orders;
        this.loadingOrders[userId] = false;
        this.cdr.detectChanges();
      },
      error: (error: any) => {
        console.error('Error loading orders for user:', userId, error);
        this.userOrders[userId] = [];
        this.loadingOrders[userId] = false;
        this.cdr.detectChanges();
      }
    });
  }

  getInitials(user: User): string {
    const first = user.firstName?.charAt(0)?.toUpperCase() || '';
    const last = user.lastName?.charAt(0)?.toUpperCase() || '';
    return first + last || user.email?.charAt(0)?.toUpperCase() || '?';
  }

  viewOrderDetails(order: Order): void {
    this.selectedOrder = order;
    this.showOrderModal = true;
    this.cdr.detectChanges();
  }

  closeOrderModal(): void {
    this.showOrderModal = false;
    this.selectedOrder = null;
    this.cdr.detectChanges();
  }

  sendEmail(user: User): void {
    window.location.href = `mailto:${user.email}`;
  }

  callUser(user: User): void {
    if (user.phone) {
      window.location.href = `tel:${user.phone}`;
    }
  }

  toggleUserStatus(user: User): void {
    this.userService.toggleUserStatus(user.id).subscribe({
      next: (updatedUser: User) => {
        const index = this.users.findIndex(u => u.id === updatedUser.id);
        if (index !== -1) {
          this.users[index] = updatedUser;
          this.filterUsers(); // Re-apply filter to update view
        }
        Swal.fire({
          title: 'Success',
          text: `User ${updatedUser.enabled ? 'activated' : 'deactivated'} successfully`,
          icon: 'success',
          timer: 2000,
          showConfirmButton: false
        });
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        Swal.fire('Error', 'Failed to update user status', 'error');
        this.cdr.detectChanges();
      }
    });
  }

  deleteUser(user: User): void {
    Swal.fire({
      title: 'Are you sure?',
      text: `You are about to delete user ${user.firstName} ${user.lastName}. This action cannot be undone.`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete it!',
      cancelButtonText: 'No, cancel',
      customClass: {
        confirmButton: 'btn-cyber-danger',
        cancelButton: 'btn-cyber-outline'
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.userService.deleteUser(user.id).subscribe({
          next: () => {
            this.users = this.users.filter(u => u.id !== user.id);
            this.filterUsers();
            Swal.fire('Deleted!', 'User has been deleted.', 'success');
            this.cdr.detectChanges();
          },
          error: () => {
            Swal.fire('Error!', 'Failed to delete user.', 'error');
            this.cdr.detectChanges();
          }
        });
      }
    });
  }
}
