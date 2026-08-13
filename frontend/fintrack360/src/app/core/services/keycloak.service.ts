import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {

  private keycloak: Keycloak;

  constructor() {
    this.keycloak = new Keycloak({
      url: environment.keycloak.url,
      realm: environment.keycloak.realm,
      clientId: environment.keycloak.clientId
    });
  }

  async init(): Promise<boolean> {
    try {
      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        pkceMethod: 'S256'
      });
      return authenticated;
    } catch (error) {
      console.error('Keycloak init failed:', error);
      return false;
    }
  }

  login(): void {
    this.keycloak.login();
  }

  logout(): void {
    this.keycloak.logout({ redirectUri: window.location.origin });
  }

  getToken(): string | undefined {
    return this.keycloak.token;
  }

  isAuthenticated(): boolean {
    return !!this.keycloak.token;
  }

  getUsername(): string {
    return this.keycloak.tokenParsed?.['preferred_username'] || '';
  }

  getEmail(): string {
    return this.keycloak.tokenParsed?.['email'] || '';
  }

  getRoles(): string[] {
    return this.keycloak.tokenParsed?.['realm_access']?.['roles'] || [];
  }

  getUserId(): string {
    return this.keycloak.tokenParsed?.['sub'] || '';
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  isOwner(): boolean {
    return this.hasRole('OWNER');
  }

  isAccountant(): boolean {
    return this.hasRole('ACCOUNTANT');
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  async refreshToken(): Promise<boolean> {
    try {
      return await this.keycloak.updateToken(30);
    } catch {
      this.login();
      return false;
    }
  }
}