export interface Address {
  id: number;
  fullName: string;
  phone: string;
  addressLine: string;
  city: string;
  postalCode: string;
  country: string;
  isDefault: boolean;
  label?: string;
}

export interface AddressRequest {
  fullName: string;
  phone: string;
  addressLine: string;
  city: string;
  postalCode: string;
  country?: string;
  isDefault?: boolean;
  label?: string;
}
