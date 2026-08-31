import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { SidebarComponent } from './shared/components/sidebar/sidebar';
import { NavbarComponent } from './shared/components/navbar/navbar';
import { KeycloakService } from './core/services/keycloak.service';

// Prefix-matched: any sub-path also counts as public (e.g. /register?inviteToken=...)
const PUBLIC_ROUTE_PREFIXES = ['/login', '/register', '/admin'];
// Exact-matched only: '/' would match every route as a prefix, hiding the
// sidebar everywhere, so the landing page needs a real equality check instead.
const EXACT_PUBLIC_ROUTES = ['/'];

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
    const path = url.split('?')[0];
    const isPublicRoute = EXACT_PUBLIC_ROUTES.includes(path) ||
      PUBLIC_ROUTE_PREFIXES.some(route => path.startsWith(route));
    this.showChrome = this.isAuthenticated && !isPublicRoute;
  }
}
