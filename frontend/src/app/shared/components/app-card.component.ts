import { Component, input } from '@angular/core';

@Component({
  selector: 'app-card',
  standalone: true,
  template: `<div
    class="app-card"
    [class.app-card--hoverable]="hoverable()"
    [class.app-card--glow]="variant() === 'glow'"
    [class.app-card--stat]="variant() === 'stat'"
    [class.app-card--compact]="padding() === 'compact'"
    [class.app-card--none]="padding() === 'none'"
  >
    <ng-content />
  </div>`,
  styles: [`
    .app-card {
      background: var(--card-bg);
      backdrop-filter: blur(var(--blur-md));
      -webkit-backdrop-filter: blur(var(--blur-md));
      border: 1px solid var(--card-border);
      border-radius: var(--radius-lg);
      padding: 20px;
      position: relative;
      overflow: hidden;
      transition: all var(--transition-base);
    }

    .app-card--compact {
      padding: 14px;
    }

    .app-card--none {
      padding: 0;
    }

    .app-card--hoverable {
      cursor: pointer;
    }

    .app-card--hoverable:hover {
      background: var(--card-bg-hover);
      border-color: var(--card-border-hover);
      transform: translateY(-2px);
      box-shadow: var(--glow-primary);
    }

    .app-card--glow {
      box-shadow: var(--glow-primary);
    }

    .app-card--glow:hover {
      box-shadow: 0 0 50px rgba(139, 92, 246, 0.2);
    }

    .app-card--stat {
      text-align: center;
    }
  `],
})
export class AppCardComponent {
  variant = input<'default' | 'glow' | 'stat'>('default');
  hoverable = input<boolean>(false);
  padding = input<'default' | 'compact' | 'none'>('default');
}
