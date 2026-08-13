import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService, Transaction, TransactionPage, CreateTransactionRequest } from '../../../core/services/transaction.service';
import { UserService } from '../../../core/services/user.service';

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
  selectedType: string = 'ALL';
  searchQuery: string = '';
  loading = false;
  companyId = '';
  userId = '';
  showCreateForm = false;

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
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user) => {
        this.companyId = user.companyId;
        this.userId = user.userId;
        this.newTransaction.companyId = user.companyId;
        this.newTransaction.createdBy = user.userId;
        this.loadTransactions();
      },
      error: (err: any) => console.error('Error loading user:', err)
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
    this.transactionService.createTransaction(this.newTransaction)
      .subscribe({
        next: () => {
          this.showCreateForm = false;
          this.resetForm();
          this.loadTransactions();
        },
        error: (err: any) => console.error('Error creating transaction:', err)
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
    this.newTransaction = {
      companyId: this.companyId,
      categoryId: '',
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