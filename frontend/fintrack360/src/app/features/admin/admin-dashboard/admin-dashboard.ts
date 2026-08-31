import { Component, Input, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss',
})
export class AdminDashboardComponent implements OnInit, OnChanges {

  @Input() activeTab: 'overview' | 'companies' | 'users' = 'overview';

  companies: any[] = [];
  users: any[] = [];
  loading = false;

  stats = {
    totalCompanies: 0,
    totalUsers: 0,
    totalOwners: 0,
    totalAccountants: 0
    
  };

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadData();
  }

  ngOnChanges(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.loadCompanies();
    this.loadUsers();
  }

  loadCompanies(): void {
    this.userService.getAllCompanies().subscribe({
      next: (data: any[]) => {
        this.companies = data;
        this.stats.totalCompanies = data.length;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading companies', err);
        this.loading = false;
      }
    });
  }

  loadUsers(): void {
    this.userService.getAllUsers().subscribe({
      next: (data: any[]) => {
        this.users = data;
        this.stats.totalUsers = data.length;
        this.stats.totalOwners = data.filter(u => u.role === 'OWNER').length;
        this.stats.totalAccountants = data.filter(u => u.role === 'ACCOUNTANT').length;
      },
      error: (err: any) => console.error('Error loading users', err)
    });
  }

  deleteCompany(id: string): void {
    if (!confirm('Delete this company?')) return;
    this.userService.deleteCompany(id).subscribe({
      next: () => this.loadCompanies(),
      error: (err: any) => console.error(err)
    });
  }

  deleteUser(id: string): void {
    if (!confirm('Delete this user?')) return;
    this.userService.deleteUser(id).subscribe({
      next: () => this.loadUsers(),
      error: (err: any) => console.error(err)
    });
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'OWNER': return 'badge-owner';
      case 'ACCOUNTANT': return 'badge-accountant';
      case 'ADMIN': return 'badge-admin';
      default: return '';
    }
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('fr-MA');
  }
}