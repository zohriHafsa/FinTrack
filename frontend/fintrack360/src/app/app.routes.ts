import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role-guard';

export const routes: Routes = [
  
  {
    path: '',
    loadComponent: () =>
      import('./features/landing/landing')
        .then(m => m.LandingComponent)
  },

  
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login')
        .then(m => m.Login)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register')
        .then(m => m.Register)
  },

  
  {
    path: 'admin',
    loadComponent: () =>
      import('./features/admin/admin-layout/admin-layout')
        .then(m => m.AdminLayoutComponent),
    canActivate: [roleGuard],
    data: { roles: ['ADMIN'] }
  },

  // ← OWNER/ACCOUNTANT SPACE
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard')
        .then(m => m.Dashboard),
    canActivate: [authGuard]
  },
  {
    path: 'transactions',
    loadComponent: () =>
      import('./features/transactions/transaction-list/transaction-list')
        .then(m => m.TransactionList),
    canActivate: [authGuard]
  },
  {
    path: 'invoices',
    loadComponent: () =>
      import('./features/invoices/invoices-list/invoice-list')
        .then(m => m.InvoicesList),
    canActivate: [authGuard]
  },
  {
    path: 'budgets',
    loadComponent: () =>
      import('./features/budgets/budget-list/budget-list')
        .then(m => m.BudgetList),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['OWNER'] }
  },
  {
    path: 'insights',
    loadComponent: () =>
      import('./features/insights/insights')
        .then(m => m.Insights),
    canActivate: [authGuard]
  },
  {
    path: 'team',
    loadComponent: () =>
      import('./features/team/team')
        .then(m => m.TeamComponent),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['OWNER'] }
  },
  {
    path: 'notifications',
    loadComponent: () =>
      import('./features/notifications/notifications')
        .then(m => m.NotificationsComponent),
    canActivate: [authGuard]
  },
  {
    path: 'unauthorized',
    loadComponent: () =>
      import('./features/auth/unauthorized/unauthorized')
        .then(m => m.Unauthorized)
  }
];