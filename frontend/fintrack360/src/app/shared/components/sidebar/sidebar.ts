import { Component, OnInit } from '@angular/core';
import {CommonModule} from '@angular/common';
import { RouterModule } from '@angular/router';
import {KeycloakService} from '../../../core/services/keycloak.service';

interface MenuItem{
  label: string;
  route: string;
  icon: string;
  roles: string[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss',
})

export class SidebarComponent implements OnInit {

  
  private allMenuItems: MenuItem[] = [
    { label: 'Dashboard', route: '/dashboard', icon: 'dashboard', roles: [] },
    { label: 'Transactions', route: '/transactions', icon: 'receipt_long', roles: [] },
    { label: 'Invoices', route: '/invoices', icon: 'description', roles: [] },
    { label: 'Budgets', route: '/budgets', icon: 'account_balance_wallet', roles: ['OWNER'] },
    { label: 'Insights', route: '/insights', icon: 'insights', roles: [] },
    { label: 'Admin', route: '/admin', icon: 'admin_panel_settings', roles: ['ADMIN'] },
  ];

  
  visibleMenuItems: MenuItem[] = [];

  username: string = '';

  
  constructor(private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    // Get username to display in sidebar
    this.username = this.keycloakService.getUsername();

    this.visibleMenuItems = this.allMenuItems.filter(item => {
  
      if (item.roles.length === 0) return true;

      return item.roles.some(role => this.keycloakService.hasRole(role));
    });
  }

  logout(): void {
  this.keycloakService.logout();
  }
}
