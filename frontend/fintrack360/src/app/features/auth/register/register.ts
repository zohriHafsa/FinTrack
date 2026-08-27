import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UserService, RegisterRequest } from '../../../core/services/user.service';
import { CompanyService } from '../../../core/services/company.service';
import { KeycloakService } from '../../../core/services/keycloak.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register implements OnInit {

  sectors: string[] = [];
  submitting = false;
  submitted = false;
  errorMessage = '';

  form: RegisterRequest = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    companyName: '',
    sector: ''
  };
  confirmPassword = '';

  constructor(
    private userService: UserService,
    private companyService: CompanyService,
    private keycloakService: KeycloakService
  ) {}

  ngOnInit(): void {
    // Best-effort suggestions; a brand new deployment may have no sectors yet.
    this.companyService.getSectors().subscribe({
      next: (sectors) => this.sectors = sectors,
      error: () => this.sectors = []
    });
  }

  submit(): void {
    this.errorMessage = '';

    if (this.form.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }
    if (this.form.password.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters.';
      return;
    }

    this.submitting = true;
    this.userService.register(this.form).subscribe({
      next: () => {
        this.submitting = false;
        this.submitted = true;
      },
      error: (err: any) => {
        this.submitting = false;
        this.errorMessage = err?.error?.message || 'Registration failed. Please try again.';
      }
    });
  }

  goToLogin(): void {
    this.keycloakService.login();
  }
}
