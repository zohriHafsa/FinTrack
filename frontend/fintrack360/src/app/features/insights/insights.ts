import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InsightService, Insight } from '../../core/services/insight.service';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-insights',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './insights.html',
  styleUrl: './insights.scss',
})
export class Insights implements OnInit {

  insights: Insight[] = [];
  companyId = '';
  loading = false;
  generating = false;

  // Chat
  isChatOpen = false;
  chatMessages: { role: 'ai' | 'user', text: string }[] = [
    { role: 'ai', text: "Hi — ask me anything about your finances. Try: 'How is my cash flow this month?'" }
  ];
  chatInput = '';
  chatLoading = false;

  constructor(
    private insightService: InsightService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.userService.loadCurrentUser().subscribe({
      next: (user: any) => {
        this.companyId = user.companyId;
        this.loadInsights();
      }
    });
  }

  loadInsights(): void {
    this.loading = true;
    this.insightService.getInsights(this.companyId).subscribe({
      next: (page: any) => {
        this.insights = page.content;
        this.loading = false;
      },
      error: (err: any) => {
        console.error('Error loading insights', err);
        this.loading = false;
      }
    });
  }

  generateInsights(): void {
    this.generating = true;
    this.insightService.generateInsights(this.companyId).subscribe({
      next: () => {
        setTimeout(() => {
          this.loadInsights();
          this.generating = false;
        }, 2000);
      },
      error: (err: any) => {
        console.error('Error generating insights', err);
        this.generating = false;
      }
    });
  }

  getTypeBadgeClass(type: string): string {
    switch (type) {
      case 'SPENDING_ANOMALY': return 'badge-anomaly';
      case 'FORECAST': return 'badge-forecast';
      case 'TREND': return 'badge-trend';
      default: return 'badge-trend';
    }
  }

  getTypeLabel(type: string): string {
    switch (type) {
      case 'SPENDING_ANOMALY': return 'Anomaly';
      case 'FORECAST': return 'Forecast';
      case 'TREND': return 'Trend';
      default: return type;
    }
  }

  toggleChat(): void {
    this.isChatOpen = !this.isChatOpen;
  }

  sendMessage(): void {
    if (!this.chatInput.trim()) return;

    const userMessage = this.chatInput.trim();
    this.chatMessages.push({ role: 'user', text: userMessage });
    this.chatInput = '';
    this.chatLoading = true;

    this.insightService.askQuestion(this.companyId, userMessage).subscribe({
      next: (res) => {
        this.chatMessages.push({ role: 'ai', text: res.answer || 'Sorry, I could not answer that.' });
        this.chatLoading = false;
      },
      error: (err: any) => {
        console.error('Error asking AI', err);
        this.chatMessages.push({ role: 'ai', text: 'Sorry, something went wrong.' });
        this.chatLoading = false;
      }
    });
  }

  onChatKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.sendMessage();
    }
  }
}