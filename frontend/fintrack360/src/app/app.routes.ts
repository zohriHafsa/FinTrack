import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role-guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
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
    path: 'admin',
    loadComponent: () =>
      import('./features/admin/admin')
        .then(m => m.Admin),
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'unauthorized',
    loadComponent: () =>
      import('./features/auth/unauthorized/unauthorized')
        .then(m => m.Unauthorized)
  }
];