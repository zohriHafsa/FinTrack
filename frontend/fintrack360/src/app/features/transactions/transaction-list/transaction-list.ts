import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService, Transaction, TransactionPage, CreateTransactionRequest } from '../../../core/services/transaction.service';
import { UserService } from '../../../core/services/user.service';
import { CompanyService, Category } from '../../../core/services/company.service';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transaction-list.html',
  styleUrl: './transaction-list.scss'
})
export class TransactionList implements OnInit {

  transactions: Transaction[] = [];
  filteredTransactions: Transaction[] = [];
  categories: Category[] = [];
  selectedType: string = 'ALL';
  searchQuery: string = '';
  loading = false;
  saving = false;
  companyId = '';
  userId = '';
  showCreateForm = false;
  errorMessage = '';
  importing = false;
  importSuccess = false;
  importedCount = 0;
  importError = '';

  typeFilters = ['ALL', 'INCOME', 'EXPENSE'];

  paymentMethods = ['CASH', 'BANK_TRANSFER', 'CARD', 'CHECK'];

  newTransaction: CreateTransactionRequest = {
    companyId: '',
    categoryId: '',
    amount: 0,
    type: 'EXPENSE',
    description: '',
    paymentMethod: 'CASH',
    date: new Date().toISOString().split('T')[0],
    createdBy: ''
  };

  constructor(
    private transactionService: TransactionService,
    private companyService: CompanyService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user) => {
        this.companyId = user.companyId;
        this.userId = user.userId;
        this.newTransaction.companyId = user.companyId;
        this.newTransaction.createdBy = user.userId;
        if (this.companyId) {
          this.loadTransactions();
          this.loadCategories();
        }
      },
      error: (err: any) => console.error('Error loading user:', err)
    });
  }
  @ViewChild('fileInput') fileInput!: ElementRef;

triggerFileInput(): void {
  this.fileInput.nativeElement.click();
}

onFileSelected(event: any): void {
  const file = event.target.files[0];
  if (!file) return;

  // Validate file type
  if (!file.name.endsWith('.csv')) {
    this.importError = 'Please select a valid CSV file.';
    return;
  }

  this.importCsv(file);
}

importCsv(file: File): void {
  this.importing = true;
  this.importSuccess = false;
  this.importError = '';

  this.transactionService.importCsv(
    this.companyId,
    this.userId,
    file
  ).subscribe({
    next: (transactions: any[]) => {
      this.importing = false;
      this.importSuccess = true;
      this.importedCount = transactions.length;
      this.loadTransactions();

      // Hide success message after 3 seconds
      setTimeout(() => this.importSuccess = false, 3000);

      // Reset file input
      this.fileInput.nativeElement.value = '';
    },
    error: (err: any) => {
      this.importing = false;
      this.importError = 'Failed to import CSV. Please check the file format.';
      console.error('Import error:', err);
    }
  });
}

  loadCategories(): void {
    this.companyService.getCategories(this.companyId).subscribe({
      next: (categories) => {
        this.categories = categories;
        if (!this.newTransaction.categoryId && categories.length > 0) {
          this.newTransaction.categoryId = categories[0].id;
        }
      },
      error: (err: any) => console.error('Error loading categories:', err)
    });
  }

  loadTransactions(): void {
    this.loading = true;
    const type = this.selectedType === 'ALL' ? undefined : this.selectedType;

    this.transactionService.getTransactions(this.companyId, type)
      .subscribe({
        next: (page: TransactionPage) => {
          this.transactions = page.content;
          this.applySearch();
          this.loading = false;
        },
        error: (err: any) => {
          console.error('Error loading transactions:', err);
          this.loading = false;
        }
      });
  }

  filterByType(type: string): void {
    this.selectedType = type;
    this.loadTransactions();
  }

  applySearch(): void {
    if (!this.searchQuery.trim()) {
      this.filteredTransactions = this.transactions;
      return;
    }
    const q = this.searchQuery.toLowerCase();
    this.filteredTransactions = this.transactions.filter(t =>
      t.description?.toLowerCase().includes(q) ||
      t.type.toLowerCase().includes(q)
    );
  }

  createTransaction(): void {
    this.errorMessage = '';

    if (!this.newTransaction.categoryId) {
      this.errorMessage = 'Please select a category.';
      return;
    }
    if (!this.newTransaction.amount || this.newTransaction.amount <= 0) {
      this.errorMessage = 'Please enter an amount greater than 0.';
      return;
    }

    this.saving = true;
    this.transactionService.createTransaction(this.newTransaction)
      .subscribe({
        next: () => {
          this.saving = false;
          this.showCreateForm = false;
          this.resetForm();
          this.loadTransactions();
        },
        error: (err: any) => {
          console.error('Error creating transaction:', err);
          this.saving = false;
          this.errorMessage = err?.error?.message || 'Could not create transaction. Please try again.';
        }
      });
  }

  deleteTransaction(id: string): void {
    if (!confirm('Delete this transaction?')) return;
    this.transactionService.deleteTransaction(id).subscribe({
      next: () => this.loadTransactions(),
      error: (err: any) => console.error('Error deleting transaction:', err)
    });
  }

  resetForm(): void {
    this.errorMessage = '';
    this.newTransaction = {
      companyId: this.companyId,
      categoryId: this.categories.length > 0 ? this.categories[0].id : '',
      amount: 0,
      type: 'EXPENSE',
      description: '',
      paymentMethod: 'CASH',
      date: new Date().toISOString().split('T')[0],
      createdBy: this.userId
    };
  }

  getTypeClass(type: string): string {
    return type === 'INCOME' ? 'type-income' : 'type-expense';
  }

  formatAmount(amount: number): string {
    return amount.toLocaleString('fr-MA') + ' MAD';
  }
}