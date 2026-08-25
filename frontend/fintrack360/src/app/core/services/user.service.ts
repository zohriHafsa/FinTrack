import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface UserProfile {
  userId: string;
  email: string;
  role: string;
  companyId: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUser = new BehaviorSubject<UserProfile | null>(null);
  currentUser$ = this.currentUser.asObservable();

  constructor(private http: HttpClient) {}

  loadCurrentUser(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.apiUrl}/me`)
      .pipe(tap(user => this.currentUser.next(user)));
  }

  getCurrentUser(): UserProfile | null {
    return this.currentUser.getValue();
  }

  getCompanyId(): string {
    return this.currentUser.getValue()?.companyId || '';
  }
}