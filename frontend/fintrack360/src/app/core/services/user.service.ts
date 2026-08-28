import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment.development';

export interface UserProfile {
  userId: string;
  email: string;
  role: string;
  companyId: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  companyName?: string;
  sector?: string;
  inviteToken?: string;
}

export interface RegisterResponse {
  userId: string;
  email: string;
  role: string;
  companyId?: string;
  message: string;
}

export interface InvitePreview {
  valid: boolean;
  email?: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUser = new BehaviorSubject<UserProfile | null>(null);
  currentUser$ = this.currentUser.asObservable();

  constructor(private http: HttpClient) {}

  register(request: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, request);
  }

  previewInvite(token: string): Observable<InvitePreview> {
    return this.http.get<InvitePreview>(`${this.apiUrl}/invite/${token}`);
  }

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
  getTeamMembers(companyId: string): Observable<any[]> {
  return this.http.get<any[]>(`${this.apiUrl}/team/${companyId}`);
}

inviteAccountant(companyId: string, data: { name: string, email: string }): Observable<any> {
  return this.http.post(`${this.apiUrl}/invite`, { ...data, companyId });
}

removeMember(companyId: string, userId: string): Observable<void> {
  return this.http.delete<void>(`${this.apiUrl}/team/${companyId}/members/${userId}`);
}
}