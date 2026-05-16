import { Component, input } from '@angular/core';

@Component({
  selector: 'app-stats-card',
  standalone: true,
  template: `
    <div class="stats-card">
      <div class="stats-card__value" [style.color]="color()">{{ value() }}</div>
      <div class="stats-card__label">{{ label() }}</div>
      @if (change()) {
        <div class="stats-card__change" [class.stats-card__change--up]="trend() === 'up'" [class.stats-card__change--down]="trend() === 'down'">
          {{ change() }}
        </div>
      }
    </div>
  `,
  styles: [`
    .stats-card {
      text-align: center;
      padding: 16px 12px;
      background: var(--card-bg);
      backdrop-filter: blur(var(--blur-sm));
      border: 1px solid var(--card-border);
      border-radius: var(--radius-md);
      transition: all var(--transition-base);
    }

    .stats-card:hover {
      border-color: var(--card-border-hover);
      transform: translateY(-2px);
      box-shadow: var(--glow-primary);
    }

    .stats-card__value {
      font-size: 28px;
      font-weight: 700;
      line-height: 1.2;
    }

    .stats-card__label {
      font-size: 12px;
      color: var(--text-secondary);
      margin-top: 4px;
      font-weight: 500;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .stats-card__change {
      font-size: 11px;
      color: var(--text-tertiary);
      margin-top: 6px;
    }

    .stats-card__change--up {
      color: var(--accent-emerald);
    }

    .stats-card__change--down {
      color: var(--accent-rose);
    }
  `],
})
export class StatsCardComponent {
  label = input.required<string>();
  value = input.required<string | number>();
  change = input<string>('');
  trend = input<'up' | 'down' | 'neutral'>('neutral');
  color = input<string>('var(--text-primary)');
}
