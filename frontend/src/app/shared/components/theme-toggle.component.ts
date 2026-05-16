import { Component, inject } from '@angular/core';
import { ThemeService } from '../../core/theme/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  template: `
    <button class="theme-toggle" (click)="themeService.cycleTheme()" title="Switch theme">
      <div class="theme-toggle__icon">
        @switch (themeService.currentTheme().key) {
          @case ('dark') {
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>
            </svg>
          }
          @case ('light') {
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="4"/><path d="M12 2v2"/><path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/><path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/><path d="m19.07 4.93-1.41 1.41"/>
            </svg>
          }
          @case ('space') {
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/>
              <path d="M5 3v4"/><path d="M19 17v4"/><path d="M3 5h4"/><path d="M17 19h4"/>
            </svg>
          }
        }
      </div>
      <span class="theme-toggle__label">{{ themeService.currentTheme().name }}</span>
    </button>
  `,
  styles: [`
    .theme-toggle {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 12px;
      border-radius: var(--radius-md);
      background: var(--card-bg);
      border: 1px solid var(--card-border);
      color: var(--text-secondary);
      cursor: pointer;
      transition: all var(--transition-base);
      font-size: 13px;
    }

    .theme-toggle:hover {
      background: var(--card-bg-hover);
      border-color: var(--card-border-hover);
      color: var(--text-primary);
    }

    .theme-toggle__icon {
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .theme-toggle__label {
      font-weight: 500;
    }

    @media (max-width: 768px) {
      .theme-toggle__label {
        display: none;
      }

      .theme-toggle {
        padding: 8px;
      }
    }
  `],
})
export class ThemeToggleComponent {
  themeService = inject(ThemeService);
}
