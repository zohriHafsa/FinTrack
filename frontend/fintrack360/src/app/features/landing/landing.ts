import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { KeycloakService } from '../../core/services/keycloak.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './landing.html',
  styleUrl: './landing.scss',
})
export class LandingComponent {

  features = [
    {
      icon: 'receipt_long',
      title: 'Transaction Management',
      description: 'Track all your income and expenses in one place with smart categorization.'
    },
    {
      icon: 'description',
      title: 'Invoice Management',
      description: 'Create, send and track invoices. Get notified when payments are overdue.'
    },
    {
      icon: 'account_balance_wallet',
      title: 'Budget Control',
      description: 'Set spending limits per category and get alerts before you exceed them.'
    },
    {
      icon: 'auto_awesome',
      title: 'AI Insights',
      description: 'Get intelligent financial insights and forecasts powered by AI.'
    },
    {
      icon: 'group',
      title: 'Team Collaboration',
      description: 'Invite your accountant and collaborate on your finances securely.'
    },
    {
      icon: 'notifications',
      title: 'Smart Notifications',
      description: 'Stay informed with real-time alerts about your financial activity.'
    }
  ];

  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  login(): void {
    this.keycloakService.login();
  }

  register(): void {
    this.router.navigate(['/register']);
  }
}