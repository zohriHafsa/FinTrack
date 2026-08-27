import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';

export interface Budget {
  id: string;
  companyId: string;
  categoryId: string;
  categoryName?: string;
  limitAmount: number;
  period: 'MONTHLY' | 'QUARTERLY' | 'ANNUAL';
  periodStart: string;
  periodEnd: string;
  currentSpend: number;
  createdAt: string;
}

export interface CreateBudgetRequest {
  companyId: string;
  categoryId: string;
  limitAmount: number;
  period: 'MONTHLY' | 'QUARTERLY' | 'ANNUAL';
  periodStart: string;
  periodEnd: string;
}

@Injectable({
  providedIn: 'root'
})
export class BudgetService {

  private apiUrl = `${environment.apiUrl}/budgets`;

  constructor(private http: HttpClient) {}

  getBudgets(companyId: string): Observable<Budget[]> {
    const params = new HttpParams().set('companyId', companyId);
    return this.http.get<Budget[]>(this.apiUrl, { params });
  }

  createBudget(request: CreateBudgetRequest): Observable<Budget> {
    return this.http.post<Budget>(this.apiUrl, request);
  }

  updateBudget(id: string, request: Partial<CreateBudgetRequest>): Observable<Budget> {
    return this.http.put<Budget>(`${this.apiUrl}/${id}`, request);
  }

  deleteBudget(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
  
}