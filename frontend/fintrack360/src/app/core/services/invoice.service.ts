import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Invoice {
  id: string;
  companyId: string;
  clientId: string;
  amount: number;
  status: 'UNPAID' | 'PAID' | 'OVERDUE';
  issueDate: string;
  dueDate: string;
  paidDate?: string;
  notes?: string;
  createdAt: string;
}

export interface CreateInvoiceRequest {
  companyId: string;
  clientId: string;
  amount: number;
  issueDate: string;
  dueDate: string;
  notes?: string;
}

export interface InvoicePage {
  content: Invoice[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {

  private apiUrl = `${environment.apiUrl}/invoices`;

  constructor(private http: HttpClient) {}

  getInvoices(companyId: string, status?: string, page = 0, size = 20): Observable<InvoicePage> {
    let params = new HttpParams()
      .set('companyId', companyId)
      .set('page', page)
      .set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<InvoicePage>(this.apiUrl, { params });
  }

  getInvoiceById(id: string): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/${id}`);
  }

  createInvoice(request: CreateInvoiceRequest): Observable<Invoice> {
    return this.http.post<Invoice>(this.apiUrl, request);
  }

  markAsPaid(id: string): Observable<Invoice> {
    return this.http.put<Invoice>(`${this.apiUrl}/${id}/pay`, {});
  }

  deleteInvoice(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}