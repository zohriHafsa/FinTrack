import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BudgetService, Budget, CreateBudgetRequest } from '../../../core/services/budget.service';
import { CompanyService, Category } from '../../../core/services/company.service';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-budget-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './budget-list.html',
  styleUrl: './budget-list.scss',
})
export class BudgetList implements OnInit {

  budgets: Budget[] = [];
  categories: Category[] = [];
  companyId: string = '';
  loading = false;
  accountError: string | null = null;
  showCreateModal = false;
  showEditModal = false;
  selectedBudget: Budget | null = null;
  showCustomCategory = false;
  customCategoryName = '';
  saving = false;

  // Create form
  newBudget: CreateBudgetRequest = {
    companyId: '',
    categoryId: '',
    limitAmount: 0,
    period: 'MONTHLY',
    periodStart: '',
    periodEnd: ''
  };

  // Edit form
  editBudget: Partial<CreateBudgetRequest> = {};

  constructor(
    private budgetService: BudgetService,
    private companyService: CompanyService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
  this.userService.loadCurrentUser().subscribe({
    next: (user) => {
      this.companyId = user.companyId;
      this.newBudget.companyId = user.companyId;
      if (this.companyId) {
        this.loadBudgets();
        this.loadCategories();
      } else {
        this.accountError = 'Your company is still being set up. Please wait a few seconds and refresh the page.';
      }
    },
    error: (err) => {
      console.error('Error loading user', err);
      this.accountError = err.status === 404
        ? "This login isn't registered yet. Please sign up first."
        : 'Could not load your account. Please try again later.';
    }
  });
}

  onCategorySelect(event: any): void {
    if (event.target.value === 'custom') {
      this.showCustomCategory = true;
      this.newBudget.categoryId = '';
    } else {
      this.showCustomCategory = false;
      this.customCategoryName = '';
    }
  }

  loadBudgets(): void {
    this.loading = true;
    this.budgetService.getBudgets(this.companyId).subscribe({
      next: (budgets) => {
        this.budgets = budgets;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading budgets', err);
        this.loading = false;
      }
    });
  }

  loadCategories(): void {
    this.companyService.getCategories(this.companyId).subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (err) => console.error('Error loading categories', err)
    });
  }

  getSpendPercentage(budget: Budget): number {
    if (budget.limitAmount === 0) return 0;
    return Math.min((budget.currentSpend / budget.limitAmount) * 100, 100);
  }

  getProgressColor(budget: Budget): string {
    const pct = this.getSpendPercentage(budget);
    if (pct >= 100) return '#ef4444';
    if (pct >= 80) return '#f59e0b';
    return '#14b8a6';
  }

  getBudgetStatus(budget: Budget): string | null {
    const pct = this.getSpendPercentage(budget);
    if (pct >= 100) return 'Exceeded';
    if (pct >= 80) return 'Near limit';
    return null;
  }

  getBudgetStatusClass(budget: Budget): string {
    const pct = this.getSpendPercentage(budget);
    if (pct >= 100) return 'badge-exceeded';
    if (pct >= 80) return 'badge-near';
    return '';
  }

  getRemainingAmount(budget: Budget): number {
    return budget.limitAmount - budget.currentSpend;
  }

  getPeriodDates(period: 'MONTHLY' | 'QUARTERLY' | 'ANNUAL'): { start: string, end: string } {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth();

    if (period === 'MONTHLY') {
      const start = new Date(year, month, 1);
      const end = new Date(year, month + 1, 0);
      return {
        start: start.toISOString().split('T')[0],
        end: end.toISOString().split('T')[0]
      };
    } else if (period === 'QUARTERLY') {
      const quarter = Math.floor(month / 3);
      const start = new Date(year, quarter * 3, 1);
      const end = new Date(year, quarter * 3 + 3, 0);
      return {
        start: start.toISOString().split('T')[0],
        end: end.toISOString().split('T')[0]
      };
    } else {
      return {
        start: `${year}-01-01`,
        end: `${year}-12-31`
      };
    }
  }

  onPeriodChange(): void {
    const dates = this.getPeriodDates(this.newBudget.period);
    this.newBudget.periodStart = dates.start;
    this.newBudget.periodEnd = dates.end;
  }

  openCreateModal(): void {
    if (!this.companyId) return;
    this.newBudget = {
      companyId: this.companyId,
      categoryId: this.categories.length > 0 ? this.categories[0].id : '',
      limitAmount: 0,
      period: 'MONTHLY',
      periodStart: '',
      periodEnd: ''
    };
    this.onPeriodChange();
    this.showCreateModal = true;
  }

  closeCreateModal(): void {
  this.showCreateModal = false;
  this.saving = false;  
  this.showCustomCategory = false;
  this.customCategoryName = '';
}


  
saveBudget(): void {
  if (this.saving) return;  
  if (!this.newBudget.categoryId || this.newBudget.limitAmount <= 0) return;

  this.saving = true;

  if (this.showCustomCategory) {
    if (!this.customCategoryName.trim()) {
      this.saving = false;
      return;
    }

    this.companyService.createCategory(this.companyId, {
      name: this.customCategoryName.trim(),
      type: 'EXPENSE'
    }).subscribe({
      next: (category) => {
        this.categories.push(category);
        this.newBudget.categoryId = category.id;
        this.createBudgetRequest();
      },
      error: (err) => {
        console.error('Error creating category', err);
        this.saving = false;
      }
    });
  } else {
    this.createBudgetRequest();
  }
}

private createBudgetRequest(): void {
  this.budgetService.createBudget(this.newBudget).subscribe({
    next: () => {
      this.saving = false;
      this.closeCreateModal();        
      this.showCustomCategory = false;
      this.customCategoryName = '';
      this.loadBudgets();             
    },
    error: (err) => {
      console.error('Error creating budget', err);
      this.saving = false;
    }
  });
}

  openEditModal(budget: Budget): void {
    this.selectedBudget = budget;
    this.editBudget = {
      categoryId: budget.categoryId,
      limitAmount: budget.limitAmount,
      period: budget.period,
      periodStart: budget.periodStart,
      periodEnd: budget.periodEnd
    };
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.selectedBudget = null;
  }

  updateBudget(): void {
    if (!this.selectedBudget) return;

    this.budgetService.updateBudget(this.selectedBudget.id, this.editBudget).subscribe({
      next: () => {
        this.loadBudgets();
        this.closeEditModal();
      },
      error: (err) => console.error('Error updating budget', err)
    });
  }

  deleteBudget(id: string): void {
    if (!confirm('Are you sure you want to delete this budget?')) return;

    this.budgetService.deleteBudget(id).subscribe({
      next: () => this.loadBudgets(),
      error: (err) => console.error('Error deleting budget', err)
    });
  }

  getCategoryName(categoryId: string): string {
    const cat = this.categories.find(c => c.id === categoryId);
    return cat ? cat.name : 'Unknown';
  }
}