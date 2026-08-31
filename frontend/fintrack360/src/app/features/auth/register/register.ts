import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { UserService, RegisterRequest } from '../../../core/services/user.service';
import { CompanyService } from '../../../core/services/company.service';
import { KeycloakService } from '../../../core/services/keycloak.service';
import { environment } from '../../../../environments/environment.development';

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

  // Invite-based registration (joining an existing company as an Accountant)
  inviteToken: string | null = null;
  checkingInvite = false;
  inviteInvalidMessage = '';

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
    private keycloakService: KeycloakService,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.inviteToken = this.route.snapshot.queryParamMap.get('inviteToken');

    if (this.inviteToken) {
      this.checkingInvite = true;
      this.loadInvitePreview(this.inviteToken);
    } else {
      // Best-effort suggestions; a brand new deployment may have no sectors yet.
      this.companyService.getSectors().subscribe({
        next: (sectors) => this.sectors = sectors,
        error: () => this.sectors = []
      });
    }
  }

  private async loadInvitePreview(token: string): Promise<void> {
    try {
      const res = await fetch(`${environment.apiUrl}/auth/invite/${token}`);
      const preview = await res.json();

      this.checkingInvite = false;
      if (!res.ok || !preview.valid) {
        this.inviteInvalidMessage = preview?.message || 'This invitation link is invalid.';
      } else {
        this.form.email = preview.email || '';
      }
    } catch (err) {
      console.error('Error checking invite', err);
      this.checkingInvite = false;
      this.inviteInvalidMessage = 'Could not reach the server. Please try again.';
    }

    this.cdr.detectChanges();
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

    const payload: RegisterRequest = { ...this.form };
    if (this.inviteToken) {
      payload.inviteToken = this.inviteToken;
      delete payload.companyName;
      delete payload.sector;
    }

    this.submitting = true;
    this.userService.register(payload).subscribe({
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
    this.keycloakService.login(window.location.origin + '/dashboard');
  }
}
