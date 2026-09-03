import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Transaction {
  id: string;
  companyId: string;
  categoryId: string;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  description: string;
  paymentMethod: string;
  date: string;
  createdBy: string;
  createdAt: string;
}

export interface TransactionPage {
  content: Transaction[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface MonthlySummary {
  month: string;
  income: number;
  expense: number;
}

export interface CreateTransactionRequest {
  companyId: string;
  categoryId: string;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  description: string;
  paymentMethod: string;
  date: string;
  createdBy: string;
}

@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  private apiUrl = `${environment.apiUrl}/transactions`;

  constructor(private http: HttpClient) {}

  getTransactions(
    companyId: string,
    type?: string,
    page = 0,
    size = 20
  ): Observable<TransactionPage> {
    let params = new HttpParams()
      .set('companyId', companyId)
      .set('page', page)
      .set('size', size);
    if (type) params = params.set('type', type);
    return this.http.get<TransactionPage>(this.apiUrl, { params });
  }

  createTransaction(request: CreateTransactionRequest): Observable<Transaction> {
    return this.http.post<Transaction>(this.apiUrl, request);
  }

  deleteTransaction(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getMonthlySummary(
    companyId: string,
    startDate: string,
    endDate: string
  ): Observable<MonthlySummary[]> {
    const params = new HttpParams()
      .set('companyId', companyId)
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<MonthlySummary[]>(
      `${this.apiUrl}/monthly-summary`, { params });
  }
  importCsv(companyId: string, createdBy: string, file: File): Observable<any[]> {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('companyId', companyId);
  formData.append('createdBy', createdBy);

  return this.http.post<any[]>(
    `${environment.apiUrl}/transactions/import`,
    formData
  );
}
}