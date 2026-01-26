import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService, Order, OrderItem } from '@services/order-service';
import { CartService } from '@services/cart.service';
import Swal from 'sweetalert2';

interface OrderStep {
  status: string;
  label: string;
}

@Component({
  selector: 'app-order-details',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './order-details.html'
})
export class OrderDetailsComponent implements OnInit {
  order: Order | null = null;
  loading = false;
  error = '';
  cancelling = false;
  showCancelModal = false;
  showRefundSuccessModal = false;
  cancelReason = '';
  canCancel = false;
  refundAmount = 0;

  orderSteps: OrderStep[] = [
    { status: 'PLACED', label: 'Placed' },
    { status: 'PROCESSING', label: 'Processing' },
    { status: 'SHIPPED', label: 'Shipped' },
    { status: 'DELIVERED', label: 'Delivered' }
  ];

  private statusOrder = ['PLACED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private cartService: CartService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const orderId = params['id'];
      if (orderId) {
        this.loadOrderDetails(orderId);
      } else {
        this.error = 'Invalid order ID';
      }
    });
  }

  loadOrderDetails(orderId: string): void {
    this.loading = true;
    this.error = '';

    const id = parseInt(orderId, 10);
    if (isNaN(id)) {
      this.error = 'Invalid order ID';
      this.loading = false;
      this.cdr.detectChanges();
      return;
    }

    this.orderService.getOrderDetails(id).subscribe({
      next: (order) => {
        this.order = {
          ...order,
          items: order.items.map(item => ({
            ...item,
            unitPrice: this.calculateUnitPrice(item)
          }))
        };
        this.checkCanCancel();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error loading order:', error);
        if (error.status === 404) {
          this.error = 'Order not found';
        } else if (error.status === 403) {
          this.error = 'You don\'t have access to this order';
        } else {
          this.error = 'Unable to load order details';
        }
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  checkCanCancel(): void {
    if (!this.order) {
      this.canCancel = false;
      return;
    }

    if (this.order.status === 'CANCELLED' || this.order.status === 'SHIPPED' || this.order.status === 'DELIVERED') {
      this.canCancel = false;
      return;
    }

    let createdAt: Date;
    if (Array.isArray(this.order.createdAt)) {
      createdAt = new Date(this.order.createdAt[0], this.order.createdAt[1] - 1, this.order.createdAt[2], this.order.createdAt[3], this.order.createdAt[4], this.order.createdAt[5]);
    } else {
      createdAt = new Date(this.order.createdAt);
    }

    const now = new Date();
    const hoursSinceCreation = (now.getTime() - createdAt.getTime()) / (1000 * 60 * 60);
    this.canCancel = hoursSinceCreation <= 24;
  }

  openCancelModal(): void {
    this.showCancelModal = true;
    this.cancelReason = '';
  }

  closeCancelModal(): void {
    this.showCancelModal = false;
    this.cancelReason = '';
  }

  closeRefundSuccessModal(): void {
    this.showRefundSuccessModal = false;
  }

  requestCancelOrder(): void {
    Swal.fire({
      title: 'Are you sure?',
      text: "You won't be able to revert this!",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, cancel it!'
    }).then((result) => {
      if (result.isConfirmed) {
        this.confirmCancelOrder();
      }
    });
  }

  confirmCancelOrder(): void {
    if (!this.order || !this.cancelReason.trim()) return;

    this.cancelling = true;
    this.refundAmount = this.order.total;

    this.orderService.cancelOrder(this.order.id, this.cancelReason).subscribe({
      next: (updatedOrder) => {
        this.order = updatedOrder;
        this.canCancel = false;
        this.showCancelModal = false;
        this.cancelling = false;

        if (updatedOrder.paymentStatus === 'REFUNDED' || updatedOrder.paymentStatus === 'REFUND_PENDING') {
          this.showRefundSuccessModal = true;
        } else {
          Swal.fire(
            'Cancelled!',
            'Your order has been cancelled.',
            'success'
          );
        }

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cancelling order:', error);
        Swal.fire({
          title: 'Error',
          text: error.error?.message || 'Unable to cancel order. Please try again.',
          icon: 'error'
        });
        this.cancelling = false;
        this.cdr.detectChanges();
      }
    });
  }

  calculateUnitPrice(item: OrderItem): number {
    if (item.discount && item.discount > 0) {
      return item.price * (1 - item.discount / 100);
    }
    return item.price;
  }

  isStepComplete(stepStatus: string): boolean {
    if (!this.order) return false;
    if (this.order.status === 'CANCELLED') return false;
    const currentIndex = this.statusOrder.indexOf(this.order.status);
    const stepIndex = this.statusOrder.indexOf(stepStatus);
    return stepIndex <= currentIndex;
  }

  getProgressWidth(): string {
    if (!this.order || this.order.status === 'CANCELLED') return '0%';
    const currentIndex = this.statusOrder.indexOf(this.order.status);
    if (currentIndex === -1) return '0%';
    const percentage = (currentIndex / (this.statusOrder.length - 1)) * 100;
    return `${percentage}%`;
  }

  getStepStyle(stepStatus: string): { [key: string]: string } {
    const isComplete = this.isStepComplete(stepStatus);
    if (isComplete) {
      return {
        'background': 'linear-gradient(135deg, #00f0ff, #00ff88)',
        'box-shadow': '0 0 20px rgba(0, 240, 255, 0.4)'
      };
    } else {
      return {
        'background': 'rgba(255, 255, 255, 0.1)',
        'box-shadow': 'none'
      };
    }
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

  getPaymentStatusStyle(status: string): { [key: string]: string } {
    const styles: { [key: string]: { [key: string]: string } } = {
      'PENDING': { 'background-color': 'rgba(234, 179, 8, 0.2)', 'color': '#facc15' },
      'PAID': { 'background-color': 'rgba(0, 255, 136, 0.2)', 'color': '#00ff88' },
      'REFUNDED': { 'background-color': 'rgba(59, 130, 246, 0.2)', 'color': '#60a5fa' },
      'REFUND_PENDING': { 'background-color': 'rgba(234, 179, 8, 0.2)', 'color': '#facc15' },
      'FAILED': { 'background-color': 'rgba(239, 68, 68, 0.2)', 'color': '#f87171' }
    };
    return styles[status] || { 'background-color': 'rgba(255, 255, 255, 0.1)', 'color': '#ffffff' };
  }

  formatDate(dateInput: string | number[] | null): string {
    if (!dateInput) return 'Unknown date';
    let date: Date;
    if (Array.isArray(dateInput)) {
      date = new Date(dateInput[0], dateInput[1] - 1, dateInput[2], dateInput[3], dateInput[4], dateInput[5]);
    } else {
      date = new Date(dateInput);
    }
    if (isNaN(date.getTime())) return 'Invalid date';
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

  getStatusText(status: string): string {
    const statusTexts: Record<string, string> = {
      'PLACED': 'Placed', 'PROCESSING': 'Processing', 'SHIPPED': 'Shipped', 'DELIVERED': 'Delivered', 'CANCELLED': 'Cancelled'
    };
    return statusTexts[status] || status;
  }

  getPaymentStatusText(status: string): string {
    const statusTexts: Record<string, string> = {
      'PENDING': 'Pending', 'PAID': 'Paid', 'REFUNDED': 'Refunded', 'REFUND_PENDING': 'Refund Pending', 'FAILED': 'Failed'
    };
    return statusTexts[status] || status;
  }

  leaveReview(): void {
    if (this.order) {
      this.router.navigate(['/reviews/new', this.order.id]);
    }
  }

  reorderItems(): void {
    if (!this.order) return;

    const itemsToAdd: { id: number; quantity: number }[] = [];
    const unavailableItems: string[] = [];
    const adjustedItems: string[] = [];

    this.order.items.forEach(item => {
      const product = item.product;

      // Check if product is active
      if (!product.active) {
        unavailableItems.push(`${product.name} (No longer available)`);
        return;
      }

      // Check stock
      if (product.stockQuantity === 0) {
        unavailableItems.push(`${product.name} (Out of stock)`);
        return;
      }

      let quantityToAdd = item.quantity;
      if (quantityToAdd > product.stockQuantity) {
        quantityToAdd = product.stockQuantity;
        adjustedItems.push(`${product.name} (Quantity adjusted to ${quantityToAdd})`);
      }

      itemsToAdd.push({ id: product.id, quantity: quantityToAdd });
    });

    if (itemsToAdd.length === 0) {
      Swal.fire({
        title: 'Cannot Reorder',
        text: 'None of the items from this order are currently available.',
        icon: 'error'
      });
      return;
    }

    // Add valid items to cart
    let addedCount = 0;
    itemsToAdd.forEach(item => {
      this.cartService.addToCart(item.id, item.quantity).subscribe(() => {
        addedCount++;
        if (addedCount === itemsToAdd.length) {
          this.showReorderSummary(unavailableItems, adjustedItems);
        }
      });
    });
  }

  private showReorderSummary(unavailableItems: string[], adjustedItems: string[]): void {
    let message = 'Available items have been added to your cart.';
    let icon: 'success' | 'warning' = 'success';

    if (unavailableItems.length > 0 || adjustedItems.length > 0) {
      icon = 'warning';
      message += '<br><br>';

      if (unavailableItems.length > 0) {
        message += '<strong>Not added:</strong><br>' + unavailableItems.join('<br>') + '<br><br>';
      }

      if (adjustedItems.length > 0) {
        message += '<strong>Adjusted:</strong><br>' + adjustedItems.join('<br>');
      }
    }

    Swal.fire({
      title: 'Reorder Summary',
      html: message,
      icon: icon
    }).then(() => {
      this.router.navigate(['/cart']);
    });
  }
}
