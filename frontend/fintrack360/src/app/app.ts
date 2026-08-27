import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { SidebarComponent } from './shared/components/sidebar/sidebar';
import { NavbarComponent } from './shared/components/navbar/navbar';
import { KeycloakService } from './core/services/keycloak.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, SidebarComponent, NavbarComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class AppComponent implements OnInit {

  isAuthenticated = false;

  constructor(private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    this.isAuthenticated = this.keycloakService.isAuthenticated();
  }
}