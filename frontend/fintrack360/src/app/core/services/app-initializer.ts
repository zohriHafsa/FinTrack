import { KeycloakService } from './keycloak.service';

export function initializeApp(keycloakService: KeycloakService) {
  return async () => {
    await keycloakService.init();
  };
}