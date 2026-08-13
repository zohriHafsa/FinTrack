import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InvoiceService, Invoice, InvoicePage } from '../../../core/services/invoice.service';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-invoice-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './invoice-list.html',
  styleUrl: './invoice-list.scss'
})
export class InvoicesList implements OnInit {

  invoices: Invoice[] = [];
  filteredInvoices: Invoice[] = [];
  selectedStatus: string = 'ALL';
  searchQuery: string = '';
  loading = false;
  companyId = '';

  statusFilters = ['ALL', 'PAID', 'UNPAID', 'OVERDUE'];

  constructor(
    private invoiceService: InvoiceService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user) => {
        this.companyId = user.companyId;
        this.loadInvoices();
      },
      error: (err: any) => console.error('Error loading user:', err)
    });
  }

  loadInvoices(): void {
    this.loading = true;
    const status = this.selectedStatus === 'ALL' ? undefined : this.selectedStatus;

    this.invoiceService.getInvoices(this.companyId, status).subscribe({
      next: (page: InvoicePage) => {
        this.invoices = page.content;
        this.applySearch();
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading invoices:', err);
        this.loading = false;
      }
    });
  }

  filterByStatus(status: string): void {
    this.selectedStatus = status;
    this.loadInvoices();
  }

  applySearch(): void {
    if (!this.searchQuery.trim()) {
      this.filteredInvoices = this.invoices;
      return;
    }
    const q = this.searchQuery.toLowerCase();
    this.filteredInvoices = this.invoices.filter(inv =>
      inv.id.toLowerCase().includes(q) ||
      inv.clientId.toLowerCase().includes(q)
    );
  }

  markAsPaid(invoice: Invoice): void {
    this.invoiceService.markAsPaid(invoice.id).subscribe({
      next: () => this.loadInvoices(),
      error: (err: any) => console.error('Error marking as paid:', err)
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'PAID': return 'status-paid';
      case 'UNPAID': return 'status-unpaid';
      case 'OVERDUE': return 'status-overdue';
      default: return '';
    }
  }

  formatAmount(amount: number): string {
    return amount.toLocaleString('fr-MA') + ' MAD';
  }
}