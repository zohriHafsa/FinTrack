import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { SidebarComponent } from './shared/components/sidebar/sidebar';
import { NavbarComponent } from './shared/components/navbar/navbar';
import { KeycloakService } from './core/services/keycloak.service';

const PUBLIC_ROUTES = ['/login', '/register'];

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, SidebarComponent, NavbarComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class AppComponent implements OnInit {

  isAuthenticated = false;
  showChrome = false;

  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.isAuthenticated = this.keycloakService.isAuthenticated();
    this.updateChrome(this.router.url);

    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe(event => this.updateChrome(event.urlAfterRedirects));
  }

  private updateChrome(url: string): void {
    const isPublicRoute = PUBLIC_ROUTES.some(route => url.startsWith(route));
    this.showChrome = this.isAuthenticated && !isPublicRoute;
  }
}
