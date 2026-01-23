import { Component, OnInit, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { StripeService } from '@services/stripe';
import { CartService } from '@services/cart.service';

import { AuthService } from '@services/auth.service';
import { ToastrService } from 'ngx-toastr';
import Swal from 'sweetalert2';
import {Address, AddressService} from '@services/address.service';
import {PaymentMethodCreateParams} from '@stripe/stripe-js';
import {CartItem} from '@shared/models/cart.model';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './checkout.html',
  })
export class Checkout implements OnInit, AfterViewInit {
  checkoutForm: FormGroup;
  addressForm: FormGroup;
  cartItems: CartItem[] = [];
  cartTotal = 0;
  shippingCost = 7.0;
  taxRate = 0.19;
  clientSecret = '';
  processing = false;

  // Address management
  savedAddresses: Address[] = [];
  selectedAddressId: number | null = null;
  showNewAddressForm = false;
  loadingAddresses = true;

  // Payment validation
  cardComplete = false;
  cardError: string | null = null;

  constructor(
    private stripeService: StripeService,
    private cartService: CartService,
    private addressService: AddressService,
    private authService: AuthService,
    private router: Router,
    private toastr: ToastrService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    // Main checkout form (user info only)
    this.checkoutForm = this.fb.group({
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required]]
    });

    // New address form
    this.addressForm = this.fb.group({
      fullName: ['', [Validators.required]],
      phone: ['', [Validators.required]],
      addressLine: ['', [Validators.required]],
      city: ['', [Validators.required]],
      postalCode: ['', [Validators.required]],
      country: ['Tunisia'],
      label: ['Home'],
      isDefault: [false]
    });
  }

  ngOnInit(): void {
    this.loadCart();
    this.loadUserInfo();
    this.loadSavedAddresses();
    this.createPaymentIntent();
  }

  ngAfterViewInit(): void {
    setTimeout(async () => {
      const cardElement = await this.stripeService.createCardElement('card-element');

      // Listen for card element changes to track completion
      cardElement.on('change', (event) => {
        this.cardComplete = event.complete;
        this.cardError = event.error ? event.error.message : null;
        this.cdr.detectChanges();
      });
    }, 100);
  }

  loadCart(): void {
    this.cartService.items$.subscribe(items => {
      this.cartItems = items;
    });
    this.cartService.total$.subscribe(total => {
      this.cartTotal = total;
    });
  }

  /**
   * Pre-fill user info from authenticated user
   */
  loadUserInfo(): void {
    // First try from local cache
    const user = this.authService.getCurrentUserValue();
    if (user) {
      this.checkoutForm.patchValue({
        firstName: user.firstName || '',
        lastName: user.lastName || '',
        email: user.email || '',
        phone: user.phone || ''
      });

      // Also pre-fill address form with user name
      this.addressForm.patchValue({
        fullName: `${user.firstName || ''} ${user.lastName || ''}`.trim(),
        phone: user.phone || ''
      });
    }

    // Also fetch fresh data from server (includes phone if not in cached data)
    this.authService.getCurrentUser().subscribe({
      next: (freshUser) => {
        this.checkoutForm.patchValue({
          firstName: freshUser.firstName || this.checkoutForm.get('firstName')?.value || '',
          lastName: freshUser.lastName || this.checkoutForm.get('lastName')?.value || '',
          email: freshUser.email || this.checkoutForm.get('email')?.value || '',
          phone: freshUser.phone || this.checkoutForm.get('phone')?.value || ''
        });

        // Update address form phone if empty
        if (!this.addressForm.get('phone')?.value && freshUser.phone) {
          this.addressForm.patchValue({
            phone: freshUser.phone
          });
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.log('Could not fetch fresh user data:', err);
      }
    });
  }

  /**
   * Load saved addresses for the user
   */
  loadSavedAddresses(): void {
    this.loadingAddresses = true;
    this.addressService.getUserAddresses().subscribe({
      next: (addresses) => {
        this.savedAddresses = addresses;

        // Auto-select default address or first address
        const defaultAddress = addresses.find(a => a.isDefault);
        if (defaultAddress) {
          this.selectedAddressId = defaultAddress.id!;
        } else if (addresses.length > 0) {
          this.selectedAddressId = addresses[0].id!;
        } else {
          // No saved addresses, show new address form
          this.showNewAddressForm = true;
        }

        this.loadingAddresses = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading addresses:', err);
        this.showNewAddressForm = true;
        this.loadingAddresses = false;
        this.cdr.detectChanges();
      }
    });
  }

  /**
   * Select an existing address
   */
  selectAddress(addressId: number): void {
    this.selectedAddressId = addressId;
    this.showNewAddressForm = false;
  }

  /**
   * Toggle new address form
   */
  toggleNewAddressForm(): void {
    this.showNewAddressForm = !this.showNewAddressForm;
    if (this.showNewAddressForm) {
      this.selectedAddressId = null;
      // Pre-fill with user info
      const user = this.authService.getCurrentUserValue();
      if (user) {
        this.addressForm.patchValue({
          fullName: `${user.firstName || ''} ${user.lastName || ''}`.trim(),
          phone: user.phone || this.checkoutForm.get('phone')?.value || ''
        });
      }
    }
  }

  /**
   * Save new address
   */
  saveNewAddress(): void {
    if (this.addressForm.invalid) {
      this.addressForm.markAllAsTouched();
      this.toastr.warning('Please fill all required address fields', 'Incomplete Address');
      return;
    }

    const addressData: Address = this.addressForm.value;

    this.addressService.createAddress(addressData).subscribe({
      next: (newAddress) => {
        this.toastr.success('Address saved successfully', 'Success');
        this.savedAddresses.push(newAddress);
        this.selectedAddressId = newAddress.id!;
        this.showNewAddressForm = false;
        this.addressForm.reset({
          country: 'Tunisia',
          label: 'Home',
          isDefault: false
        });
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error saving address:', err);
        this.toastr.error('Failed to save address', 'Error');
      }
    });
  }

  /**
   * Delete an address
   */
  deleteAddress(addressId: number, event: Event): void {
    event.stopPropagation();

    Swal.fire({
      title: 'Delete Address?',
      text: 'This action cannot be undone',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete',
      cancelButtonText: 'Cancel',
      customClass: {
        popup: 'cyber-swal-popup',
        confirmButton: 'cyber-swal-confirm',
        cancelButton: 'cyber-swal-cancel'
      }
    }).then((result) => {
      if (result.isConfirmed) {
        this.addressService.deleteAddress(addressId).subscribe({
          next: () => {
            this.savedAddresses = this.savedAddresses.filter(a => a.id !== addressId);
            if (this.selectedAddressId === addressId) {
              this.selectedAddressId = this.savedAddresses.length > 0 ? this.savedAddresses[0].id! : null;
              if (!this.selectedAddressId) {
                this.showNewAddressForm = true;
              }
            }
            this.toastr.success('Address deleted', 'Success');
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error('Error deleting address:', err);
            this.toastr.error('Failed to delete address', 'Error');
          }
        });
      }
    });
  }

  /**
   * Get selected address
   */
  getSelectedAddress(): Address | null {
    if (this.selectedAddressId) {
      return this.savedAddresses.find(a => a.id === this.selectedAddressId) || null;
    }
    return null;
  }

  createPaymentIntent(): void {
    this.stripeService.createPaymentIntent().subscribe({
      next: (response) => {
        this.clientSecret = response.clientSecret;
      },
      error: (err) => {
        console.error('Error creating Payment Intent:', err);
        this.toastr.error('Error initializing payment', 'Error');
      }
    });
  }

  get subtotal(): number {
    return this.cartItems.reduce((acc, item) => acc + item.price * item.quantity, 0);
  }

  get totalDiscount(): number {
    return this.cartItems.reduce((acc, item) => {
      if (item.discount && item.discount > 0) {
        return acc + (item.price - item.unitPrice) * item.quantity;
      }
      return acc;
    }, 0);
  }

  get shipping(): number {
    return this.shippingCost;
  }

  get tax(): number {
    return (this.subtotal - this.totalDiscount) * this.taxRate;
  }

  get total(): number {
    return this.subtotal - this.totalDiscount + this.shippingCost + this.tax;
  }

  get isProcessing(): boolean {
    return this.processing;
  }

  /**
   * Check if form is valid for submission
   */
  get canSubmit(): boolean {
    const hasValidUserInfo = this.checkoutForm.valid;
    const hasValidAddress = this.selectedAddressId !== null ||
      (this.showNewAddressForm && this.addressForm.valid);
    const hasValidPayment = this.cardComplete;

    return hasValidUserInfo && hasValidAddress && hasValidPayment && !this.processing;
  }

  async placeOrder(): Promise<void> {
    // Validate user info
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      this.toastr.warning('Please fill all required personal information', 'Incomplete Form');
      return;
    }

    // Validate address selection
    let shippingAddressId: number | null = null;
    let shippingAddress: any = null;

    if (this.showNewAddressForm) {
      if (this.addressForm.invalid) {
        this.addressForm.markAllAsTouched();
        this.toastr.warning('Please fill all required address fields', 'Incomplete Address');
        return;
      }

      // Use new address form data - backend will save it
      const addressData = this.addressForm.value;
      shippingAddress = {
        fullName: addressData.fullName,
        phone: addressData.phone,
        street: addressData.addressLine,
        city: addressData.city,
        state: '',
        postalCode: addressData.postalCode,
        country: addressData.country,
        label: addressData.label,
        isDefault: addressData.isDefault || this.savedAddresses.length === 0
      };
    } else {
      const selectedAddr = this.getSelectedAddress();
      if (!selectedAddr) {
        this.toastr.warning('Please select a shipping address', 'No Address Selected');
        return;
      }

      // Use existing address ID - no duplication
      shippingAddressId = selectedAddr.id!;
    }

    this.processing = true;

    try {
      const result = await this.stripeService.confirmPayment(this.clientSecret);

      if (result.error) {
        this.toastr.error(result.error.message, 'Payment Error');
        this.processing = false;
        this.cdr.detectChanges();
      } else if (result.paymentIntent.status === 'succeeded') {
        this.stripeService.confirmOrder({
          paymentIntentId: result.paymentIntent.id,
          shippingAddressId: shippingAddressId,
          shippingAddress: shippingAddress,
          tax: this.tax,
          shippingFee: this.shipping,
          subtotal: this.subtotal - this.totalDiscount
        }).subscribe({
          next: () => {
            // Clear cart immediately after successful order
            this.cartService.clearCartLocally();

            Swal.fire({
              title: 'Payment Successful!',
              text: 'Your order has been confirmed',
              icon: 'success',
              confirmButtonText: 'View My Order',
              customClass: {
                popup: 'cyber-swal-popup',
                confirmButton: 'cyber-swal-confirm'
              }
            }).then(() => {
              this.router.navigate(['/order-success'], {
                queryParams: { payment_intent: result.paymentIntent.id }
              });
            });
          },
          error: (err) => {
            console.error('Order confirmation error:', err);
            this.toastr.error('Error confirming order', 'Error');
            this.processing = false;
            this.cdr.detectChanges();
          }
        });
      }
    } catch (error) {
      console.error('Payment error:', error);
      this.toastr.error('An error occurred', 'Error');
      this.processing = false;
      this.cdr.detectChanges();
    }
  }
}
