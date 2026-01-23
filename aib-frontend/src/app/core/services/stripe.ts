import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { loadStripe, Stripe, StripeElements, StripeCardElement } from '@stripe/stripe-js';
import { Observable } from 'rxjs';

export interface OrderConfirmationData {
  paymentIntentId: string;
  shippingAddressId: number | null;  // Use existing address ID
  shippingAddress: any | null;        // Or provide new address data
  tax: number;
  shippingFee: number;
  subtotal: number;
}

@Injectable({
  providedIn: 'root'
})
export class StripeService {
  private stripe: Stripe | null = null;
  private elements: StripeElements | null = null;
  private cardElement: StripeCardElement | null = null;

  // ⚠️ IMPORTANT: Ensure this key matches the Secret Key used in your backend!
  private publishableKey = 'pk_test_51S2yJSC7SA5mF3zZPV64NQE6JKMRmtgttfzYrdgOvPraCLsWS3KsfwTNi7NZFiM1fsARaKhZg8Zgix6bLbTusdec00Bvlf9HyH';
  private apiUrl = 'http://localhost:8088/api/payment';

  constructor(private http: HttpClient) {
    this.initStripe();
  }

  private async initStripe() {
    this.stripe = await loadStripe(this.publishableKey);
  }

  async createCardElement(elementId: string): Promise<StripeCardElement> {
    if (!this.stripe) {
      await this.initStripe();
    }

    this.elements = this.stripe!.elements();
    this.cardElement = this.elements.create('card', {
      style: {
        base: {
          fontSize: '16px',
          color: '#32325d',
          fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
          '::placeholder': {
            color: '#aab7c4'
          }
        },
        invalid: {
          color: '#ef4444',
          iconColor: '#ef4444'
        }
      }
    });

    this.cardElement.mount(`#${elementId}`);
    return this.cardElement;
  }

  createPaymentIntent(): Observable<any> {
    return this.http.post(`${this.apiUrl}/create-payment-intent`, {}, {
      withCredentials: true,
      headers: this.getAuthHeaders()
    });
  }

  async confirmPayment(clientSecret: string): Promise<any> {
    if (!this.stripe || !this.cardElement) {
      throw new Error('Stripe not initialized');
    }

    console.log('Confirming payment with clientSecret:', clientSecret);

    try {
      return await this.stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: this.cardElement
        }
      });
    } catch (error) {
      console.error('Error in confirmCardPayment:', error);
      throw error;
    }
  }

  /**
   * Confirm order with all pricing details
   */
  confirmOrder(data: OrderConfirmationData): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/confirm-payment`,
      {
        paymentIntentId: data.paymentIntentId,
        shippingAddressId: data.shippingAddressId,
        shippingAddress: data.shippingAddress,
        tax: data.tax,
        shippingFee: data.shippingFee,
        subtotal: data.subtotal
      },
      {
        withCredentials: true,
        headers: this.getAuthHeaders()
      }
    );
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return token ? new HttpHeaders({ 'Authorization': `Bearer ${token}` }) : new HttpHeaders();
  }
}
