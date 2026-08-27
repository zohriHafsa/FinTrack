import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { KeycloakService } from '../../../core/services/keycloak.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {

  constructor(private keycloakService: KeycloakService) {}

  login(): void {
    this.keycloakService.login();
  }
}
