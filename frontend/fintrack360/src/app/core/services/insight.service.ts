import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment.development';

export interface Insight {
  id: string;
  companyId: string;
  type: 'TREND' | 'SPENDING_ANOMALY' | 'FORECAST';
  title: string;
  content: string;
  generated_at: string;
  valid_until: string;
}

export interface InsightPage {
  content: Insight[];
  totalElements: number;
  totalPages: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class InsightService {

  private apiUrl = `${environment.apiUrl}/insights`;

  constructor(private http: HttpClient) {}

  getInsights(companyId: string, page = 0, size = 10): Observable<InsightPage> {
    const params = new HttpParams()
      .set('companyId', companyId)
      .set('page', page)
      .set('size', size);
    return this.http.get<InsightPage>(this.apiUrl, { params });
  }

  generateInsights(companyId: string): Observable<void> {
    const params = new HttpParams().set('companyId', companyId);
    return this.http.post<void>(`${this.apiUrl}/generate`, null, { params });
  }

  askQuestion(companyId: string, question: string): Observable<{ answer: string }> {
    const params = new HttpParams().set('companyId', companyId);
    return this.http.post<{ answer: string }>(`${this.apiUrl}/chat`, { question }, { params });
  }
}