// footer.component.ts - CYBER-TECH REDESIGN
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './footer.html'
})
export class FooterComponent {
  currentYear = new Date().getFullYear();
  
  newsletterEmail = '';
  isSubscribing = false;
  subscriptionMessage = '';

  subscribeNewsletter(): void {
    if (!this.newsletterEmail || !this.validateEmail(this.newsletterEmail)) {
      return;
    }

    this.isSubscribing = true;
    
    // Simulate API call
    setTimeout(() => {
      this.subscriptionMessage = 'Thank you for subscribing!';
      this.newsletterEmail = '';
      this.isSubscribing = false;
      
      // Clear message after 3 seconds
      setTimeout(() => {
        this.subscriptionMessage = '';
      }, 3000);
    }, 1000);
  }

  private validateEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}
