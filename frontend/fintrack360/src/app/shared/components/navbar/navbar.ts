import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { KeycloakService } from '../../../core/services/keycloak.service';
import { NotificationService } from '../../../core/services/notification.service';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class NavbarComponent implements OnInit {
  @Input() pageTitle: string = 'Dashboard';

  unreadCount = 0;
  userId = '';
  userInitials = '';
  userRole = '';

  constructor(
    private keycloakService: KeycloakService,
    private notificationService: NotificationService,
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user: any) => {
        this.userId = user.userId;
        this.userRole = user.role;
        this.userInitials = user.email.substring(0, 2).toUpperCase();
        this.loadUnreadCount();
      }
    });

    // Subscribe to unread count updates
    this.notificationService.unreadCount$.subscribe(count => {
      this.unreadCount = count;
    });
  }

  loadUnreadCount(): void {
    if (!this.userId) return;
    this.notificationService.getUnreadCount(this.userId).subscribe({
      next: (count: any) => {
        this.notificationService.updateUnreadCount(count);
      },
      error: (err: any) => console.error('Error loading unread count', err)
    });
  }

  goToNotifications(): void {
    this.router.navigate(['/notifications']);
  }

  logout(): void {
    this.keycloakService.logout();
  }
}