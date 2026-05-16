import { Component, signal, HostListener } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './sidebar.component';
import { TopbarComponent } from './topbar.component';
import { MobileNavComponent } from './mobile-nav.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, TopbarComponent, MobileNavComponent],
  template: `
    <div class="shell" [class.shell--collapsed]="sidebarCollapsed()">
      <app-sidebar [collapsed]="sidebarCollapsed()" [mobileOpen]="mobileMenuOpen()" (toggleCollapse)="toggleSidebar()" (closeMobile)="mobileMenuOpen.set(false)" />
      <div class="shell__main">
        <app-topbar (menuToggle)="mobileMenuOpen.set(!mobileMenuOpen())" />
        <main class="shell__content">
          <router-outlet />
        </main>
      </div>
      <app-mobile-nav />
    </div>
  `,
  styles: [`
    .shell { display: flex; min-height: 100vh; background: var(--bg-primary); }
    .shell__main { flex: 1; margin-left: var(--sidebar-width); transition: margin-left var(--transition-base); display: flex; flex-direction: column; min-height: 100vh; }
    .shell--collapsed .shell__main { margin-left: var(--sidebar-width-collapsed); }
    .shell__content { flex: 1; overflow-y: auto; }
    @media (max-width: 768px) {
      .shell__main { margin-left: 0 !important; }
      .shell__content { padding-bottom: 70px; }
    }
  `],
})
export class ShellComponent {
  sidebarCollapsed = signal(false);
  mobileMenuOpen = signal(false);

  toggleSidebar(): void {
    this.sidebarCollapsed.update(v => !v);
  }

  @HostListener('window:resize')
  onResize(): void {
    if (window.innerWidth > 768) {
      this.mobileMenuOpen.set(false);
    }
  }
}
