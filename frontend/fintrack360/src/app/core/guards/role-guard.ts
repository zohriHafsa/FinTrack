import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { KeycloakService } from '../services/keycloak.service';

export const roleGuard = (route: any) => {
  const keycloak = inject(KeycloakService);
  const router = inject(Router);

  const requiredRoles: string[] = route.data?.roles || [];

  // Admin guard - only ADMIN can access /admin
  if (requiredRoles.includes('ADMIN')) {
    if (keycloak.hasRole('ADMIN')) {
      return true;
    }
    router.navigate(['/unauthorized']);
    return false;
  }

  // Owner/Accountant guard
  const hasRole = requiredRoles.some(role => keycloak.hasRole(role));
  if (!hasRole && requiredRoles.length > 0) {
    router.navigate(['/unauthorized']);
    return false;
  }

  return true;
};