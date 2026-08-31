import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { KeycloakService } from '../services/keycloak.service';

export const authGuard = () => {
  const keycloak = inject(KeycloakService);
  const router = inject(Router);

  if (!keycloak.isAuthenticated()) {
    keycloak.login();
    return false;
  }

  
  if (keycloak.hasRole('ADMIN')) {
    router.navigate(['/admin']);
    return false;
  }

  return true;
};