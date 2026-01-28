import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '@services/auth.service';
import { User } from '@shared/models/user.model';
import { ProfileInfoComponent } from '@features/profile/components/profile-info/profile-info.component';
import { AddressManagementComponent } from '@features/profile/components/address-management/address-management.component';
import { RecentOrdersComponent } from '@features/profile/components/recent-orders/recent-orders.component';
import { ChangePasswordComponent } from '@features/profile/components/change-password/change-password.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ProfileInfoComponent,
    AddressManagementComponent,
    RecentOrdersComponent,
    ChangePasswordComponent
  ],
  templateUrl: './profile.html',
  styleUrls: ['./profile.css']
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  activeTab: 'info' | 'addresses' | 'orders' | 'security' = 'info';

  constructor(private authService: AuthService,
  private cdr :ChangeDetectorRef ) {}

  ngOnInit(): void {
    // Get initial user value
    this.user = this.authService.getCurrentUserValue();
console.log(this.user)
    // Subscribe to user changes
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
      this.cdr.detectChanges();
    });

    // Force refresh from server to ensure we have latest data
    this.authService.getCurrentUser().subscribe({
      error: (err) => console.error('Failed to load user profile', err)
    });
  }

  setActiveTab(tab: 'info' | 'addresses' | 'orders' | 'security'): void {
    this.activeTab = tab;
    this.cdr.detectChanges();
  }

  hasPassword(): boolean {
    // Show security tab if provider is LOCAL or if provider is not set (legacy/default local)
    return !this.user?.provider || this.user.provider === 'LOCAL';
  }

  getTabClasses(tab: 'info' | 'addresses' | 'orders' | 'security'): string {
    const baseClasses = 'w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200';
    const activeClasses = 'bg-[#00f0ff]/10 text-[#00f0ff]';
    const inactiveClasses = 'text-[#a0a0c0] hover:bg-white/5 hover:text-white';

    return `${baseClasses} ${this.activeTab === tab ? activeClasses : inactiveClasses}`;
  }
}
