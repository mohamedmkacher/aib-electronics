import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import {Address, AddressService} from '@services/address.service';

@Component({
  selector: 'app-address-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './address-management.component.html',
  styleUrls: ['./address-management.component.css']
})
export class AddressManagementComponent implements OnInit {
  addresses: Address[] = [];
  addressForm!: FormGroup;
  showModal = false;
  isEditing = false;
  editingId: number | null = null;
  submitting = false;

  constructor(
    private addressService: AddressService,
    private fb: FormBuilder,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadAddresses();
    this.initForm();
  }

  initForm(): void {
    this.addressForm = this.fb.group({
      fullName: ['', Validators.required],
      phone: ['', Validators.required],
      addressLine: ['', Validators.required],
      city: ['', Validators.required],
      postalCode: ['', Validators.required],
      country: ['Tunisia', Validators.required],
      label: ['Home'],
      isDefault: [false]
    });
  }

  loadAddresses(): void {
    this.addressService.getUserAddresses().subscribe({
      next: (addresses) => this.addresses = addresses,
      complete: () => this.cdr.detectChanges(),
      error: () => this.toastr.error('Failed to load addresses')
    });

  }

  openAddModal(): void {
    this.isEditing = false;
    this.editingId = null;
    this.addressForm.reset({ country: 'Tunisia', label: 'Home', isDefault: false });
    this.showModal = true;
  }

  openEditModal(address: Address): void {
    this.isEditing = true;
    this.editingId = address.id!;
    this.addressForm.patchValue(address);
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  onSubmit(): void {
    if (this.addressForm.invalid) {
      this.addressForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const addressData = this.addressForm.value;

    const request = this.isEditing
      ? this.addressService.updateAddress(this.editingId!, addressData)
      : this.addressService.createAddress(addressData);

    request.subscribe({
      next: () => {
        this.toastr.success(`Address ${this.isEditing ? 'updated' : 'added'} successfully`);
        this.loadAddresses();
        this.closeModal();
        this.submitting = false;
      },
      error: (err: any) => {
        this.toastr.error(err.error?.message || 'Operation failed');
        this.submitting = false;
      }
    });
  }

  deleteAddress(id: number): void {
    if (confirm('Are you sure you want to delete this address?')) {
      this.addressService.deleteAddress(id).subscribe({
        next: () => {
          this.toastr.success('Address deleted');
          this.loadAddresses();
        },
        error: () => this.toastr.error('Failed to delete address')
      });
    }
  }

  setDefault(id: number): void {
    this.addressService.setDefaultAddress(id).subscribe({
      next: () => {
        this.toastr.success('Default address updated');
        this.loadAddresses();
      },
      error: () => this.toastr.error('Failed to set default address')
    });
  }
}
