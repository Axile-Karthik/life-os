import { Component } from '@angular/core';

@Component({
  selector: 'app-quick-actions',
  standalone: true,
  template: `
    <div class="actions-container glass-sm">
      <div class="actions-header">
        <span class="actions-title">Quick Actions</span>
      </div>
      <div class="actions-grid">
        <button class="action-btn hover-lift">
          <div class="action-icon" style="background: var(--accent-primary-dim); color: var(--accent-primary)">▶</div>
          <div class="action-text">Resume Activity</div>
        </button>
        <button class="action-btn hover-lift">
          <div class="action-icon" style="background: var(--accent-secondary-dim); color: var(--accent-secondary)">⏱️</div>
          <div class="action-text">View Timeline</div>
        </button>
        <button class="action-btn hover-lift">
          <div class="action-icon" style="background: var(--accent-emerald-dim); color: var(--accent-emerald)">+</div>
          <div class="action-text">Log Manual</div>
        </button>
        <button class="action-btn hover-lift">
          <div class="action-icon" style="background: var(--accent-warm-dim); color: var(--accent-warm)">⚡</div>
          <div class="action-text">Observatory</div>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .actions-container {
      padding: 16px;
      border-radius: var(--radius-lg);
      height: 100%;
      display: flex;
      flex-direction: column;
    }
    .actions-header {
      margin-bottom: 12px;
    }
    .actions-title {
      font-size: 11px;
      color: var(--text-tertiary);
      text-transform: uppercase;
      letter-spacing: 1px;
      font-weight: 600;
    }
    .actions-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
      flex: 1;
    }
    .action-btn {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 8px;
      padding: 12px;
      background: var(--bg-tertiary);
      border-radius: var(--radius-md);
      border: 1px solid var(--card-border);
      transition: all var(--transition-fast);
    }
    .action-btn:hover {
      background: var(--card-bg-hover);
      border-color: var(--card-border-hover);
    }
    .action-icon {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
    }
    .action-text {
      font-size: 11px;
      color: var(--text-primary);
      font-weight: 500;
    }
  `]
})
export class QuickActionsWidget {
}
