import { KeycloakService } from './keycloak.service';
import { Router } from '@angular/router';

export function initializeApp(keycloak: KeycloakService, router: Router) {
  return async () => {
    await keycloak.init();

    if (keycloak.isAuthenticated()) {
      if (keycloak.hasRole('ADMIN')) {
        router.navigate(['/admin']);
      } else {
        
        const currentPath = window.location.pathname;
        if (currentPath === '/' || currentPath === '') {
          router.navigate(['/dashboard']);
        }
      }
    }
  };
}