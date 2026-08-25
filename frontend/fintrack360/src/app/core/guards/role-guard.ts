import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { KeycloakService } from '../services/keycloak.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const keycloakService = inject(KeycloakService);
  const router = inject(Router);
  const requiredRoles: string[] = route.data?.['roles'] || [];

  if (requiredRoles.length === 0) {
    return keycloakService.isAuthenticated();
  }

  const hasRole = requiredRoles.some(role => keycloakService.hasRole(role));

  if (hasRole) {
    return true;
  }

  router.navigate(['/unauthorized']);
  return false;
};