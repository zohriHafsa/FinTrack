import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { KeycloakService } from '../../../core/services/keycloak.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login implements OnInit {

  errorMessage = '';

  constructor(private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    const errorDescription = urlParams.get('error_description');

    if (error === 'access_denied') {
      if (errorDescription?.includes('temporarily locked')) {
        this.errorMessage = 'Your account has been temporarily locked after 5 failed attempts. Please wait 15 minutes and try again.';
      } else {
        this.errorMessage = 'Invalid credentials. Please try again.';
      }
    }
  }

  login(): void {
    this.keycloakService.login(window.location.origin + '/dashboard');
  }
}