import { Component, inject } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { AppCardComponent } from '../../shared/components/app-card.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { ThemeService } from '../../core/theme/theme.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [PageContainerComponent, AppCardComponent, SectionHeaderComponent],
  template: `
    <app-page-container>
      <app-section-header title="Settings" subtitle="Customize your experience" />
      <app-card>
        <h3 class="set-section">Theme</h3>
        <div class="theme-grid">
          @for (theme of themeService.themes; track theme.key) {
            <button class="theme-option" [class.theme-option--active]="themeService.currentTheme().key === theme.key" (click)="themeService.setTheme(theme.key)">
              <div class="theme-preview" [style.background]="theme.variables['--bg-primary']">
                <div class="theme-preview__accent" [style.background]="theme.variables['--accent-primary']"></div>
              </div>
              <span class="theme-option__name">{{ theme.name }}</span>
            </button>
          }
        </div>
      </app-card>
      <app-card>
        <h3 class="set-section">Display</h3>
        <div class="set-row"><span>Animations</span><span class="set-badge">Enabled</span></div>
        <div class="set-row"><span>Compact Mode</span><span class="set-badge set-badge--off">Disabled</span></div>
        <div class="set-row"><span>Sidebar Auto-collapse</span><span class="set-badge set-badge--off">Disabled</span></div>
      </app-card>
      <app-card>
        <h3 class="set-section">Data</h3>
        <div class="set-row"><span>Export Data</span><span class="set-action">Coming Soon</span></div>
        <div class="set-row"><span>Import Data</span><span class="set-action">Coming Soon</span></div>
        <div class="set-row"><span>Clear Cache</span><span class="set-action">Coming Soon</span></div>
      </app-card>
      <app-card>
        <h3 class="set-section">About</h3>
        <div class="set-row"><span>Version</span><span class="set-val">1.0.0-alpha</span></div>
        <div class="set-row"><span>Phase</span><span class="set-val">Phase 1 — Frontend Foundation</span></div>
      </app-card>
    </app-page-container>
  `,
  styles: [`
    .set-section { font-size: 16px; font-weight: 600; color: var(--text-primary); margin-bottom: 16px; }
    app-card + app-card { margin-top: 16px; }
    .theme-grid { display: flex; gap: 16px; flex-wrap: wrap; }
    .theme-option { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 12px; border-radius: var(--radius-md); border: 2px solid var(--card-border); background: transparent; cursor: pointer; transition: all var(--transition-fast); }
    .theme-option:hover { border-color: var(--card-border-hover); }
    .theme-option--active { border-color: var(--accent-primary); background: var(--accent-primary-dim); }
    .theme-preview { width: 80px; height: 50px; border-radius: var(--radius-sm); position: relative; overflow: hidden; }
    .theme-preview__accent { position: absolute; bottom: 0; left: 0; right: 0; height: 6px; }
    .theme-option__name { font-size: 12px; font-weight: 500; color: var(--text-secondary); }
    .set-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; border-bottom: 1px solid var(--card-border); font-size: 14px; color: var(--text-primary); }
    .set-row:last-child { border-bottom: none; }
    .set-badge { font-size: 12px; padding: 2px 10px; border-radius: var(--radius-full); background: var(--accent-emerald-dim); color: var(--accent-emerald); font-weight: 500; }
    .set-badge--off { background: var(--bg-tertiary); color: var(--text-tertiary); }
    .set-action { font-size: 12px; color: var(--text-tertiary); font-style: italic; }
    .set-val { font-size: 13px; color: var(--text-secondary); }
  `],
})
export class SettingsComponent {
  themeService = inject(ThemeService);
}
