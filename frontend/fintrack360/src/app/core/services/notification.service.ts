import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { environment } from '../../../environments/environment.development';

export interface Notification {
  id: string;
  userId: string;
  companyId: string;
  type: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private apiUrl = `${environment.apiUrl}/notifications`;
  private unreadCount = new BehaviorSubject<number>(0);
  unreadCount$ = this.unreadCount.asObservable();

  constructor(private http: HttpClient) {}

  getNotifications(userId: string): Observable<Notification[]> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<Notification[]>(this.apiUrl, { params });
  }

  getUnreadCount(userId: string): Observable<number> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<number>(`${this.apiUrl}/unread-count`, { params });
  }

  markAsRead(id: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/read`, null);
  }

  markAllAsRead(userId: string): Observable<void> {
    const params = new HttpParams().set('userId', userId);
    return this.http.put<void>(`${this.apiUrl}/mark-all-read`, null, { params });
  }

  updateUnreadCount(count: number): void {
    this.unreadCount.next(count);
  }
}