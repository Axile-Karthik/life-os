import { Component, input } from '@angular/core';

@Component({
  selector: 'app-widget-container',
  standalone: true,
  template: `
    <div class="widget">
      <div class="widget__header">
        <div class="widget__header-left">
          @if (icon()) { <span class="widget__icon">{{ icon() }}</span> }
          <h3 class="widget__title">{{ title() }}</h3>
          @if (badge()) { <span class="widget__badge">{{ badge() }}</span> }
        </div>
        <ng-content select="[widget-actions]" />
      </div>
      <div class="widget__content"><ng-content /></div>
    </div>
  `,
  styles: [`
    .widget { background: var(--card-bg); backdrop-filter: blur(var(--blur-md)); border: 1px solid var(--card-border); border-radius: var(--radius-lg); overflow: hidden; transition: border-color var(--transition-base); }
    .widget:hover { border-color: var(--card-border-hover); }
    .widget__header { display: flex; align-items: center; justify-content: space-between; padding: 16px 20px 0; }
    .widget__header-left { display: flex; align-items: center; gap: 8px; }
    .widget__icon { font-size: 16px; }
    .widget__title { font-size: 15px; font-weight: 600; color: var(--text-primary); }
    .widget__badge { font-size: 10px; font-weight: 600; padding: 2px 8px; border-radius: var(--radius-full); background: var(--accent-primary-dim); color: var(--accent-primary); }
    .widget__content { padding: 16px 20px 20px; }
  `],
})
export class WidgetContainerComponent {
  title = input.required<string>();
  icon = input<string>('');
  badge = input<string>('');
}
