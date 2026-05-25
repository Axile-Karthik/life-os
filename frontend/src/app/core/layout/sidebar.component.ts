import { Component, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NavItem } from '../../shared/models/activity.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <aside class="sidebar" [class.sidebar--collapsed]="collapsed()" [class.sidebar--open]="mobileOpen()">
      <div class="sidebar__brand">
        <div class="sidebar__logo">
          <div class="sidebar__logo-orb"></div>
        </div>
        @if (!collapsed()) {
          <div class="sidebar__brand-text">
            <h1 class="sidebar__title">LIFE OS</h1>
            <p class="sidebar__subtitle">Your Universe. Your System.</p>
          </div>
        }
      </div>
      <nav class="sidebar__nav">
        @for (item of navItems; track item.route) {
          <a class="sidebar__link" [routerLink]="item.route" routerLinkActive="sidebar__link--active" [title]="item.label">
            <span class="sidebar__link-icon" [innerHTML]="getIcon(item.icon)"></span>
            @if (!collapsed()) { <span class="sidebar__link-label">{{ item.label }}</span> }
          </a>
        }
      </nav>
      <div class="sidebar__footer">
        <button class="sidebar__collapse-btn" (click)="toggleCollapse.emit()" [title]="collapsed() ? 'Expand' : 'Collapse'">
          <span class="sidebar__link-icon" [innerHTML]="collapsed() ? expandIcon : collapseIcon"></span>
          @if (!collapsed()) { <span class="sidebar__link-label">Collapse</span> }
        </button>
      </div>
    </aside>
    @if (mobileOpen()) {
      <div class="sidebar__backdrop" (click)="closeMobile.emit()"></div>
    }
  `,
  styles: [`
    .sidebar { position: fixed; top: 0; left: 0; bottom: 0; width: var(--sidebar-width); background: var(--sidebar-bg); backdrop-filter: blur(var(--blur-lg)); border-right: 1px solid var(--sidebar-border); display: flex; flex-direction: column; z-index: 100; transition: width var(--transition-base), transform var(--transition-base); overflow: hidden; }
    .sidebar--collapsed { width: var(--sidebar-width-collapsed); }
    .sidebar__brand { display: flex; align-items: center; gap: 12px; padding: 20px; border-bottom: 1px solid var(--sidebar-border); min-height: 72px; }
    .sidebar__logo { flex-shrink: 0; }
    .sidebar__logo-orb { width: 32px; height: 32px; border-radius: 50%; background: var(--gradient-accent); box-shadow: 0 0 20px rgba(139,92,246,0.4); animation: orbFloat 6s ease-in-out infinite; }
    .sidebar__brand-text { overflow: hidden; white-space: nowrap; }
    .sidebar__title { font-size: 16px; font-weight: 800; letter-spacing: 0.1em; color: var(--text-primary); }
    .sidebar__subtitle { font-size: 10px; color: var(--text-tertiary); letter-spacing: 0.05em; margin-top: 1px; }
    .sidebar__nav { flex: 1; padding: 12px 8px; display: flex; flex-direction: column; gap: 2px; overflow-y: auto; }
    .sidebar__link { display: flex; align-items: center; gap: 12px; padding: 10px 12px; border-radius: var(--radius-lg); color: var(--text-secondary); transition: all var(--transition-fast); text-decoration: none; font-size: 14px; font-weight: 500; white-space: nowrap; }
    .sidebar__link:hover { color: var(--text-primary); background: rgba(139,92,246,0.08); }
    .sidebar__link--active { color: var(--accent-primary); background: var(--accent-primary-dim); }
    .sidebar__link--active .sidebar__link-icon { color: var(--accent-primary); }
    .sidebar__link-icon { flex-shrink: 0; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; }
    .sidebar__link-icon :deep(svg) { width: 20px; height: 20px; }
    .sidebar__link-label { overflow: hidden; text-overflow: ellipsis; }
    .sidebar__footer { padding: 12px 8px; border-top: 1px solid var(--sidebar-border); }
    .sidebar__collapse-btn { display: flex; align-items: center; gap: 12px; padding: 10px 12px; border-radius: var(--radius-lg); color: var(--text-tertiary); transition: all var(--transition-fast); width: 100%; font-size: 13px; }
    .sidebar__collapse-btn:hover { color: var(--text-primary); background: rgba(139,92,246,0.08); }
    .sidebar__backdrop { display: none; }
    @media (max-width: 768px) {
      .sidebar { transform: translateX(-100%); width: 280px; }
      .sidebar--open { transform: translateX(0); }
      .sidebar--collapsed { width: 280px; }
      .sidebar__backdrop { display: block; position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 99; }
    }
  `],
})
export class SidebarComponent {
  collapsed = input<boolean>(false);
  mobileOpen = input<boolean>(false);
  toggleCollapse = output<void>();
  closeMobile = output<void>();

  navItems: NavItem[] = [
    { label: 'Dashboard', route: '/dashboard', icon: 'dashboard' },
    { label: 'Timeline', route: '/timeline', icon: 'timeline' },
    { label: 'Library', route: '/library', icon: 'library' },
    { label: 'Observatory', route: '/observatory', icon: 'observatory' },
    { label: 'Statistics', route: '/statistics', icon: 'statistics' },
    { label: 'Tasks', route: '/tasks', icon: 'tasks' },
    { label: 'Achievements', route: '/achievements', icon: 'achievements' },
    { label: 'Settings', route: '/settings', icon: 'settings' },
  ];

  collapseIcon = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m11 17-5-5 5-5"/><path d="m18 17-5-5 5-5"/></svg>';
  expandIcon = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m6 17 5-5-5-5"/><path d="m13 17 5-5-5-5"/></svg>';

  getIcon(name: string): string {
    const icons: Record<string, string> = {
      dashboard: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/></svg>',
      timeline: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
      library: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m16 6 4 14"/><path d="M12 6v14"/><path d="M8 8v12"/><path d="M4 4v16"/></svg>',
      observatory: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20"/><path d="M2 12h20"/></svg>',
      statistics: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 3v18h18"/><path d="m19 9-5 5-4-4-3 3"/></svg>',
      tasks: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22c5.523 0 10-4.477 10-10S17.523 2 12 2 2 6.477 2 12s4.477 10 10 10z"/><path d="m9 12 2 2 4-4"/></svg>',
      achievements: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/><path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/><path d="M18 2H6v7a6 6 0 0 0 12 0V2Z"/></svg>',
      settings: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z"/><circle cx="12" cy="12" r="3"/></svg>',
    };
    return icons[name] || '';
  }
}
