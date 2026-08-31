import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { KeycloakService } from '../../../core/services/keycloak.service';
import { AdminDashboardComponent } from '../admin-dashboard/admin-dashboard';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, AdminDashboardComponent],
  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.scss',
  
})
export class AdminLayoutComponent {

  activeTab: 'overview' | 'companies' | 'users' = 'overview';

  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  setTab(tab: 'overview' | 'companies' | 'users'): void {
    this.activeTab = tab;
  }

  logout(): void {
    this.keycloakService.logout();
  }

  getUsername(): string {
    return this.keycloakService.getUsername();
  }
}