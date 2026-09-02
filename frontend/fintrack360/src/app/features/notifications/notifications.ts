import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from '../../core/services/notification.service';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.html',
  styleUrl: './notifications.scss',
})
export class NotificationsComponent implements OnInit {

  notifications: Notification[] = [];
  userId = '';
  loading = false;

  constructor(
    private notificationService: NotificationService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user: any) => {
        this.userId = user.userId;
        this.loadNotifications();
      }
    });
  }

  loadNotifications(): void {
    this.loading = true;
    this.notificationService.getNotifications(this.userId).subscribe({
      next: (notifications: any) => {
        this.notifications = notifications;
        this.loading = false;
        // Update unread count
        const unread = notifications.filter((n: any) => !n.isRead).length;
        this.notificationService.updateUnreadCount(unread);
      },
      error: (err: any) => {
        console.error('Error loading notifications', err);
        this.loading = false;
      }
    });
  }

  markAsRead(notification: Notification): void {
    if (notification.isRead) return;

    this.notificationService.markAsRead(notification.id).subscribe({
      next: () => {
        notification.isRead = true;
        const unread = this.notifications.filter(n => !n.isRead).length;
        this.notificationService.updateUnreadCount(unread);
      }
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead(this.userId).subscribe({
      next: () => {
        this.notifications.forEach(n => n.isRead = true);
        this.notificationService.updateUnreadCount(0);
      }
    });
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'BUDGET_EXCEEDED': return 'Budget exceeded';
      case 'BUDGET_WARNING': return 'Budget nearing limit';
      case 'INVOICE_OVERDUE': return 'Invoice overdue';
      case 'PAYMENT_RECEIVED': return 'Payment received';
      case 'WEEKLY_SUMMARY': return 'Weekly summary';
      default: return 'Notification';
    }
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'BUDGET_EXCEEDED': return 'warning';
      case 'BUDGET_WARNING': return 'trending_up';
      case 'INVOICE_OVERDUE': return 'receipt_long';
      case 'PAYMENT_RECEIVED': return 'payments';
      case 'WEEKLY_SUMMARY': return 'summarize';
      default: return 'notifications';
    }
  }

  getTypeColor(type: string): string {
    switch (type) {
      case 'BUDGET_EXCEEDED': return '#ef4444';
      case 'BUDGET_WARNING': return '#f59e0b';
      case 'INVOICE_OVERDUE': return '#f59e0b';
      case 'PAYMENT_RECEIVED': return '#22c55e';
      default: return '#3b82f6';
    }
  }

  getTimeAgo(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diff < 60) return 'Just now';
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    return `${Math.floor(diff / 86400)}d ago`;
  }
}