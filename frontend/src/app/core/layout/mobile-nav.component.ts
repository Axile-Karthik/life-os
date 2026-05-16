import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-mobile-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <nav class="mobile-nav">
      @for (item of items; track item.route) {
        <a class="mobile-nav__item" [routerLink]="item.route" routerLinkActive="mobile-nav__item--active">
          <span class="mobile-nav__icon" [innerHTML]="item.icon"></span>
          <span class="mobile-nav__label">{{ item.label }}</span>
        </a>
      }
    </nav>
  `,
  styles: [`
    .mobile-nav { display: none; position: fixed; bottom: 0; left: 0; right: 0; z-index: 100; background: var(--sidebar-bg); backdrop-filter: blur(var(--blur-lg)); border-top: 1px solid var(--sidebar-border); padding: 6px 0 env(safe-area-inset-bottom, 8px); }
    .mobile-nav__item { display: flex; flex-direction: column; align-items: center; gap: 2px; flex: 1; padding: 6px 0; color: var(--text-tertiary); transition: color var(--transition-fast); text-decoration: none; }
    .mobile-nav__item--active { color: var(--accent-primary); }
    .mobile-nav__icon { width: 22px; height: 22px; display: flex; align-items: center; justify-content: center; }
    .mobile-nav__icon :deep(svg) { width: 22px; height: 22px; }
    .mobile-nav__label { font-size: 10px; font-weight: 500; }
    @media (max-width: 768px) { .mobile-nav { display: flex; } }
  `],
})
export class MobileNavComponent {
  items = [
    { label: 'Dashboard', route: '/dashboard', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/></svg>' },
    { label: 'Library', route: '/library', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m16 6 4 14"/><path d="M12 6v14"/><path d="M8 8v12"/><path d="M4 4v16"/></svg>' },
    { label: 'Timeline', route: '/timeline', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>' },
    { label: 'Tasks', route: '/tasks', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/><path d="m9 12 2 2 4-4"/></svg>' },
    { label: 'Statistics', route: '/statistics', icon: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 3v18h18"/><path d="m19 9-5 5-4-4-3 3"/></svg>' },
  ];
}
