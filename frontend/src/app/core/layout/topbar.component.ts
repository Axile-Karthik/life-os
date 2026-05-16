import { Component, inject, input, output } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter, map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ThemeToggleComponent } from '../../shared/components/theme-toggle.component';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [ThemeToggleComponent],
  template: `
    <header class="topbar">
      <div class="topbar__left">
        <button class="topbar__menu-btn" (click)="menuToggle.emit()">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" x2="20" y1="12" y2="12"/><line x1="4" x2="20" y1="6" y2="6"/><line x1="4" x2="20" y1="18" y2="18"/></svg>
        </button>
        <h2 class="topbar__title">{{ pageTitle() }}</h2>
      </div>
      <div class="topbar__right">
        <div class="topbar__search">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/></svg>
          <input type="text" placeholder="Search..." class="topbar__search-input" />
        </div>
        <app-theme-toggle />
        <button class="topbar__icon-btn" title="Notifications">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>
          <span class="topbar__notif-dot"></span>
        </button>
        <div class="topbar__avatar">
          <div class="topbar__avatar-circle">K</div>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .topbar { position: sticky; top: 0; z-index: 50; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; height: var(--topbar-height); background: var(--topbar-bg); backdrop-filter: blur(var(--blur-lg)); border-bottom: 1px solid var(--topbar-border); }
    .topbar__left { display: flex; align-items: center; gap: 16px; }
    .topbar__menu-btn { display: none; color: var(--text-secondary); padding: 6px; border-radius: var(--radius-sm); }
    .topbar__menu-btn:hover { background: rgba(139,92,246,0.08); color: var(--text-primary); }
    .topbar__title { font-size: 18px; font-weight: 600; color: var(--text-primary); }
    .topbar__right { display: flex; align-items: center; gap: 12px; }
    .topbar__search { display: flex; align-items: center; gap: 8px; padding: 8px 14px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: var(--radius-md); color: var(--text-tertiary); min-width: 200px; transition: border-color var(--transition-fast); }
    .topbar__search:focus-within { border-color: var(--accent-primary); }
    .topbar__search-input { background: none; border: none; outline: none; color: var(--text-primary); font-size: 13px; width: 100%; font-family: inherit; }
    .topbar__search-input::placeholder { color: var(--text-tertiary); }
    .topbar__icon-btn { position: relative; color: var(--text-secondary); padding: 8px; border-radius: var(--radius-md); transition: all var(--transition-fast); }
    .topbar__icon-btn:hover { background: rgba(139,92,246,0.08); color: var(--text-primary); }
    .topbar__notif-dot { position: absolute; top: 6px; right: 6px; width: 7px; height: 7px; border-radius: 50%; background: var(--accent-rose); border: 2px solid var(--bg-primary); }
    .topbar__avatar-circle { width: 32px; height: 32px; border-radius: 50%; background: var(--gradient-accent); display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; color: white; cursor: pointer; }
    @media (max-width: 768px) {
      .topbar { padding: 0 16px; }
      .topbar__menu-btn { display: flex; }
      .topbar__search { display: none; }
    }
  `],
})
export class TopbarComponent {
  menuToggle = output<void>();
  private router = inject(Router);
  pageTitle = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(e => {
        const segment = e.urlAfterRedirects.split('/')[1] || 'dashboard';
        return segment.charAt(0).toUpperCase() + segment.slice(1);
      })
    ),
    { initialValue: 'Dashboard' }
  );
}
