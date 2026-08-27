import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';

export interface Category {
  id: string;
  name: string;
  type: 'INCOME' | 'EXPENSE';
  companyId: string;
}

export interface Client {
  id: string;
  name: string;
  email: string;
  phone: string;
  companyId: string;
}

@Injectable({
  providedIn: 'root'
})
export class CompanyService {

  private apiUrl = `${environment.apiUrl}/companies`;

  constructor(private http: HttpClient) {}

  getCategories(companyId: string): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/${companyId}/categories`);
  }

  getClients(companyId: string): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.apiUrl}/${companyId}/clients`);
  }

  createClient(companyId: string, client: { name: string, email: string, phone?: string }): Observable<Client> {
    return this.http.post<Client>(`${this.apiUrl}/${companyId}/clients`, client);
  }

  getSectors(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/sectors`);
  }

  createCategory(companyId: string, category: { name: string, type: string }): Observable<Category> {
  return this.http.post<Category>(
    `${this.apiUrl}/${companyId}/categories`,
    category
  );
}
}