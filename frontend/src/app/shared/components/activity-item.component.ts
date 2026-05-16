import { Component, input } from '@angular/core';

@Component({
  selector: 'app-activity-item',
  standalone: true,
  template: `
    <div class="activity-item">
      <div class="activity-item__line"></div>
      <div class="activity-item__dot" [style.background]="categoryColor()"></div>
      <div class="activity-item__content">
        <div class="activity-item__header">
          <span class="activity-item__title">{{ title() }}</span>
          <span class="activity-item__badge" [style.background]="categoryBgColor()" [style.color]="categoryColor()">
            {{ category() }}
          </span>
        </div>
        @if (description()) {
          <p class="activity-item__desc">{{ description() }}</p>
        }
        <span class="activity-item__time">{{ timestamp() }}</span>
      </div>
    </div>
  `,
  styles: [`
    .activity-item {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 12px 0;
      position: relative;
    }

    .activity-item__line {
      position: absolute;
      left: 5px;
      top: 24px;
      bottom: -12px;
      width: 1px;
      background: var(--card-border);
    }

    .activity-item:last-child .activity-item__line {
      display: none;
    }

    .activity-item__dot {
      width: 11px;
      height: 11px;
      border-radius: 50%;
      flex-shrink: 0;
      margin-top: 4px;
      box-shadow: 0 0 8px currentColor;
    }

    .activity-item__content {
      flex: 1;
      min-width: 0;
    }

    .activity-item__header {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }

    .activity-item__title {
      font-size: 13px;
      font-weight: 500;
      color: var(--text-primary);
    }

    .activity-item__badge {
      font-size: 10px;
      font-weight: 600;
      padding: 2px 8px;
      border-radius: var(--radius-full);
      text-transform: capitalize;
    }

    .activity-item__desc {
      font-size: 12px;
      color: var(--text-secondary);
      margin-top: 2px;
    }

    .activity-item__time {
      font-size: 11px;
      color: var(--text-tertiary);
      margin-top: 4px;
      display: block;
    }
  `],
})
export class ActivityItemComponent {
  title = input.required<string>();
  category = input.required<string>();
  timestamp = input.required<string>();
  description = input<string>('');

  categoryColor(): string {
    const colors: Record<string, string> = {
      movie: 'var(--accent-primary)',
      game: 'var(--accent-secondary)',
      anime: 'var(--accent-rose)',
      book: 'var(--accent-emerald)',
      manga: 'var(--accent-warm)',
      music: 'var(--accent-cyan)',
      task: 'var(--text-tertiary)',
    };
    return colors[this.category()] || 'var(--accent-primary)';
  }

  categoryBgColor(): string {
    const colors: Record<string, string> = {
      movie: 'var(--accent-primary-dim)',
      game: 'var(--accent-secondary-dim)',
      anime: 'var(--accent-rose-dim)',
      book: 'var(--accent-emerald-dim)',
      manga: 'var(--accent-warm-dim)',
      music: 'var(--accent-cyan-dim)',
      task: 'rgba(100, 116, 139, 0.15)',
    };
    return colors[this.category()] || 'var(--accent-primary-dim)';
  }
}
