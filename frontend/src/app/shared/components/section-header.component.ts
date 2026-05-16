import { Component, input } from '@angular/core';

@Component({
  selector: 'app-section-header',
  standalone: true,
  template: `
    <div class="section-header">
      <div class="section-header__left">
        <h2 class="section-header__title">{{ title() }}</h2>
        @if (subtitle()) {
          <p class="section-header__subtitle">{{ subtitle() }}</p>
        }
      </div>
      <div class="section-header__right">
        <ng-content />
      </div>
    </div>
  `,
  styles: [`
    .section-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 16px;
    }

    .section-header__title {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-primary);
      letter-spacing: 0.01em;
    }

    .section-header__subtitle {
      font-size: 13px;
      color: var(--text-secondary);
      margin-top: 2px;
    }

    .section-header__right {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    :host ::ng-deep .view-all {
      font-size: 13px;
      color: var(--accent-primary);
      cursor: pointer;
      transition: color var(--transition-fast);
      font-weight: 500;
    }

    :host ::ng-deep .view-all:hover {
      color: var(--accent-primary-hover);
    }
  `],
})
export class SectionHeaderComponent {
  title = input.required<string>();
  subtitle = input<string>('');
}
