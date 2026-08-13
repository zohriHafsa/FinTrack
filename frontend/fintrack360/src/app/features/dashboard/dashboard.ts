import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransactionService, TransactionPage, MonthlySummary } from '../../core/services/transaction.service';
import { InvoiceService, InvoicePage } from '../../core/services/invoice.service';
import { UserService } from '../../core/services/user.service';
import { Chart, registerables } from 'chart.js';


Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})


export class Dashboard implements OnInit {

  @ViewChild('chartCanvas') chartCanvas!: ElementRef;

  companyId = '';
  totalIncome = 0;
  totalExpenses = 0;
  netCashFlow = 0;
  overdueCount = 0;
  overdueAmount = 0;
  loading = false;
  chart: any;

  chartLabels: string[] = [];
  incomeData: number[] = [];
  expenseData: number[] = [];
  constructor(
    private transactionService: TransactionService,
    private invoiceService: InvoiceService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user) => {
        this.companyId = user.companyId;
        this.loadDashboardData();
      },
      error: (err: any) => console.error('Error loading user:', err)
    });
  }

  ngAfterViewInit(): void {
    this.initChart();
  }

  loadDashboardData(): void {
  this.loading = true;

  // Load transactions for stats
  this.transactionService.getTransactions(this.companyId, undefined, 0, 100)
    .subscribe({
      next: (page: TransactionPage) => {
        const transactions = page.content;
        this.totalIncome = transactions
          .filter(t => t.type === 'INCOME')
          .reduce((sum: number, t) => sum + t.amount, 0);
        this.totalExpenses = transactions
          .filter(t => t.type === 'EXPENSE')
          .reduce((sum: number, t) => sum + t.amount, 0);
        this.netCashFlow = this.totalIncome - this.totalExpenses;
        this.loading = false;
      },
      error: (err: any) => {
        console.error(err);
        this.loading = false;
      }
    });

  // Load overdue invoices
  this.invoiceService.getInvoices(this.companyId, 'OVERDUE')
    .subscribe({
      next: (page: InvoicePage) => {
        this.overdueCount = page.totalElements;
        this.overdueAmount = page.content
          .reduce((sum: number, inv) => sum + inv.amount, 0);
      },
      error: (err: any) => console.error(err)
    });

  // Load monthly summary for chart
  const endDate = new Date().toISOString().split('T')[0];
  const startDate = new Date(
    new Date().setMonth(new Date().getMonth() - 5))
    .toISOString().split('T')[0];

  this.transactionService.getMonthlySummary(
      this.companyId, startDate, endDate)
    .subscribe({
      next: (data: MonthlySummary[]) => {
        this.chartLabels = data.map(d => d.month);
        this.incomeData = data.map(d => d.income);
        this.expenseData = data.map(d => d.expense);
        this.initChart();
      },
      error: (err: any) => console.error(err)
    });
}

  initChart(): void {
  if (!this.chartCanvas) return;

  // Destroy existing chart if exists
  if (this.chart) this.chart.destroy();

  this.chart = new Chart(this.chartCanvas.nativeElement, {
    type: 'line',
    data: {
      labels: this.chartLabels,
      datasets: [
        {
          label: 'Income',
          data: this.incomeData,
          borderColor: '#1e293b',
          backgroundColor: 'transparent',
          tension: 0.4,
          pointBackgroundColor: '#1e293b',
          pointRadius: 4
        },
        {
          label: 'Expense',
          data: this.expenseData,
          borderColor: '#2dd4bf',
          backgroundColor: 'transparent',
          tension: 0.4,
          pointBackgroundColor: '#2dd4bf',
          pointRadius: 4
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: {
        y: {
          grid: { color: '#f1f5f9' },
          ticks: {
            callback: (value) => (Number(value) / 1000) + 'k'
          }
        },
        x: { grid: { display: false } }
      }
    }
  });
}

  formatAmount(amount: number): string {
    return amount.toLocaleString('fr-MA') + ' MAD';
  }

  getCurrentMonth(): string {
    return new Date().toLocaleString('en-US', { month: 'long', year: 'numeric' });
  }
}