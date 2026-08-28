import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../core/services/user.service';

export interface TeamMember {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  status: 'ACTIVE' | 'INVITED';
  companyId: string;
}

@Component({
  selector: 'app-team',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './team.html',
  styleUrl: './team.scss',
})
export class TeamComponent implements OnInit {

  members: TeamMember[] = [];
  companyId = '';
  currentUserId = '';
  loading = false;
  showInviteModal = false;

  inviteForm = {
    name: '',
    email: ''
  };

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user: any) => {
        this.companyId = user.companyId;
        this.currentUserId = user.userId;
        this.loadMembers();
      }
    });
  }

  loadMembers(): void {
    this.loading = true;
    this.userService.getTeamMembers(this.companyId).subscribe({
      next: (members: any) => {
        this.members = members;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading members', err);
        this.loading = false;
      }
    });
  }

  openInviteModal(): void {
    this.inviteForm = { name: '', email: '' };
    this.showInviteModal = true;
  }

  closeInviteModal(): void {
    this.showInviteModal = false;
  }

  sendInvite(): void {
    if (!this.inviteForm.email.trim()) return;

    this.userService.inviteAccountant(this.companyId, this.inviteForm).subscribe({
      next: () => {
        this.loadMembers();
        this.closeInviteModal();
      },
      error: (err: any) => console.error('Error sending invite', err)
    });
  }

  removeMember(memberId: string): void {
    if (!confirm('Are you sure you want to remove this member?')) return;

    this.userService.removeMember(this.companyId, memberId).subscribe({
      next: () => this.loadMembers(),
      error: (err: any) => console.error('Error removing member', err)
    });
  }

  getFullName(member: TeamMember): string {
    return `${member.firstName} ${member.lastName}`.trim() || member.email;
  }

  getRoleBadgeClass(role: string): string {
    return role === 'OWNER' ? 'badge-owner' : 'badge-accountant';
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'badge-active';
      case 'INVITED': return 'badge-invited';
      default: return 'badge-active';
    }
  }

  isOwner(member: TeamMember): boolean {
    return member.role === 'OWNER';
  }
}