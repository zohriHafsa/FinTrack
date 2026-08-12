import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { KeycloakService } from '../../../core/services/keycloak.service';

@Component({
  selector: 'app-navbar',
  standalone: true, 
  imports: [CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class NavbarComponent {
   @Input() pageTitle: string = 'Dashboard';

  constructor(private keycloakService: KeycloakService) {}

  logout(): void {
    this.keycloakService.logout();
  }
}
