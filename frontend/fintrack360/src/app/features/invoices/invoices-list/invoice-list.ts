import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InvoiceService, Invoice, InvoicePage, CreateInvoiceRequest } from '../../../core/services/invoice.service';
import { CompanyService, Client } from '../../../core/services/company.service';
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
  clients: Client[] = [];
  selectedStatus: string = 'ALL';
  searchQuery: string = '';
  loading = false;
  saving = false;
  companyId = '';
  accountError: string | null = null;

  statusFilters = ['ALL', 'PAID', 'UNPAID', 'OVERDUE'];

  showCreateModal = false;
  showCustomClient = false;
  errorMessage = '';

  newInvoice: CreateInvoiceRequest = {
    companyId: '',
    clientId: '',
    amount: 0,
    issueDate: new Date().toISOString().split('T')[0],
    dueDate: '',
    notes: ''
  };

  newClient = { name: '', email: '', phone: '' };

  constructor(
    private invoiceService: InvoiceService,
    private companyService: CompanyService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user) => {
        this.companyId = user.companyId;
        this.newInvoice.companyId = user.companyId;
        if (this.companyId) {
          this.loadInvoices();
          this.loadClients();
        } else {
          this.accountError = 'Your company is still being set up. Please wait a few seconds and refresh the page.';
        }
      },
      error: (err: any) => {
        console.error('Error loading user:', err);
        this.accountError = err.status === 404
          ? "This login isn't registered yet. Please sign up first."
          : 'Could not load your account. Please try again later.';
      }
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

  loadClients(): void {
    this.companyService.getClients(this.companyId).subscribe({
      next: (clients) => this.clients = clients,
      error: (err: any) => console.error('Error loading clients:', err)
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

  getClientName(clientId: string): string {
    const client = this.clients.find(c => c.id === clientId);
    return client ? client.name : clientId;
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

  onClientSelect(event: any): void {
    if (event.target.value === 'custom') {
      this.showCustomClient = true;
      this.newInvoice.clientId = '';
    } else {
      this.showCustomClient = false;
      this.newClient = { name: '', email: '', phone: '' };
    }
  }

  openCreateModal(): void {
    if (!this.companyId) return;
    this.errorMessage = '';
    this.showCustomClient = false;
    this.newClient = { name: '', email: '', phone: '' };

    const today = new Date();
    const inTwoWeeks = new Date();
    inTwoWeeks.setDate(today.getDate() + 14);

    this.newInvoice = {
      companyId: this.companyId,
      clientId: this.clients.length > 0 ? this.clients[0].id : '',
      amount: 0,
      issueDate: today.toISOString().split('T')[0],
      dueDate: inTwoWeeks.toISOString().split('T')[0],
      notes: ''
    };
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
    this.showCreateModal = false;
    this.saving = false;
  }

  saveInvoice(): void {
    if (!this.companyId || this.saving) return;
    this.errorMessage = '';

    if (!this.newInvoice.amount || this.newInvoice.amount <= 0) {
      this.errorMessage = 'Please enter an amount greater than 0.';
      return;
    }
    if (!this.newInvoice.dueDate) {
      this.errorMessage = 'Please pick a due date.';
      return;
    }

    this.saving = true;

    if (this.showCustomClient) {
      if (!this.newClient.name.trim() || !this.newClient.email.trim()) {
        this.errorMessage = 'Client name and email are required.';
        this.saving = false;
        return;
      }

      this.companyService.createClient(this.companyId, this.newClient).subscribe({
        next: (client) => {
          this.clients.push(client);
          this.newInvoice.clientId = client.id;
          this.createInvoiceRequest();
        },
        error: (err: any) => {
          console.error('Error creating client:', err);
          this.errorMessage = err?.error?.message || 'Could not create client.';
          this.saving = false;
        }
      });
    } else {
      if (!this.newInvoice.clientId) {
        this.errorMessage = 'Please select a client.';
        this.saving = false;
        return;
      }
      this.createInvoiceRequest();
    }
  }

  private createInvoiceRequest(): void {
    this.invoiceService.createInvoice(this.newInvoice).subscribe({
      next: () => {
        this.saving = false;
        this.closeCreateModal();
        this.loadInvoices();
      },
      error: (err: any) => {
        console.error('Error creating invoice:', err);
        this.errorMessage = err?.error?.message || 'Could not create invoice. Please try again.';
        this.saving = false;
      }
    });
  }
}
