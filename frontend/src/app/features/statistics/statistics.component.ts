import { Component } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { AppCardComponent } from '../../shared/components/app-card.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { StatsCardComponent } from '../../shared/components/stats-card.component';
import { WidgetContainerComponent } from '../../shared/components/widget-container.component';
import { MOCK_WEEKLY_DATA, MOCK_CATEGORY_BREAKDOWN, MOCK_RATING_DISTRIBUTION, MOCK_FOCUS_ANALYTICS, MOCK_HEATMAP_DATA } from '../../shared/data/mock-data';

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [PageContainerComponent, SectionHeaderComponent, StatsCardComponent, WidgetContainerComponent],
  template: `
    <app-page-container>
      <app-section-header title="Statistics" subtitle="Analytics layer" />
      <div class="stat-tabs">
        @for (t of tabs; track t) {
          <button class="stab" [class.stab--active]="t === activeTab" (click)="activeTab = t">{{ t }}</button>
        }
      </div>
      <div class="stat-top">
        @for (s of topStats; track s.label) {
          <app-stats-card [label]="s.label" [value]="s.value" [change]="s.change||''" [trend]="s.trend||'neutral'" [color]="s.color||''" />
        }
      </div>
      <div class="stat-grid">
        <app-widget-container title="Weekly Activity">
          <div class="bar-chart">
            @for (d of weekly; track d.day) {
              <div class="bar-col">
                <div class="bar" [style.height.%]="(d.hours/7)*100"></div>
                <span class="bar-label">{{ d.day }}</span>
              </div>
            }
          </div>
        </app-widget-container>
        <app-widget-container title="Category Breakdown">
          <div class="cat-list">
            @for (c of categories; track c.category) {
              <div class="cat-row">
                <span class="cat-dot" [style.background]="c.color"></span>
                <span class="cat-name">{{ c.category }}</span>
                <div class="cat-bar"><div class="cat-fill" [style.width.%]="c.value" [style.background]="c.color"></div></div>
                <span class="cat-pct">{{ c.value }}%</span>
              </div>
            }
          </div>
        </app-widget-container>
        <app-widget-container title="Heatmap">
          <div class="heat-grid">
            @for (week of heatmap; track $index) {
              @for (v of week; track $index) {
                <div class="heat-cell" [style.background]="getHC(v)"></div>
              }
            }
          </div>
        </app-widget-container>
        <app-widget-container title="Rating Distribution">
          <div class="rdist">
            @for (r of ratings; track r.rating) {
              <div class="rdist-row"><span class="rdist-star">{{ r.rating }}★</span><div class="rdist-bar"><div class="rdist-fill" [style.width.%]="(r.count/maxR)*100"></div></div><span class="rdist-ct">{{ r.count }}</span></div>
            }
          </div>
        </app-widget-container>
      </div>
    </app-page-container>
  `,
  styles: [`
    .stat-tabs { display: flex; gap: 8px; margin-bottom: 20px; }
    .stab { padding: 6px 16px; border-radius: var(--radius-full); background: var(--card-bg); border: 1px solid var(--card-border); color: var(--text-secondary); font-size: 13px; cursor: pointer; transition: all var(--transition-fast); }
    .stab--active { background: var(--accent-primary-dim); border-color: var(--accent-primary); color: var(--accent-primary); }
    .stat-top { display: grid; grid-template-columns: repeat(4,1fr); gap: 12px; margin-bottom: 24px; }
    .stat-grid { display: grid; grid-template-columns: repeat(2,1fr); gap: 16px; }
    .bar-chart { display: flex; align-items: flex-end; gap: 12px; height: 140px; }
    .bar-col { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px; height: 100%; justify-content: flex-end; }
    .bar { width: 100%; background: var(--accent-primary); border-radius: 4px 4px 0 0; min-height: 4px; transition: height .5s ease; }
    .bar-label { font-size: 11px; color: var(--text-tertiary); }
    .cat-list { display: flex; flex-direction: column; gap: 10px; }
    .cat-row { display: flex; align-items: center; gap: 8px; }
    .cat-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
    .cat-name { font-size: 12px; color: var(--text-secondary); width: 55px; }
    .cat-bar { flex: 1; height: 6px; background: var(--bg-tertiary); border-radius: var(--radius-full); overflow: hidden; }
    .cat-fill { height: 100%; border-radius: var(--radius-full); }
    .cat-pct { font-size: 11px; color: var(--text-tertiary); width: 30px; text-align: right; }
    .heat-grid { display: grid; grid-template-columns: repeat(7,1fr); gap: 3px; }
    .heat-cell { aspect-ratio: 1; border-radius: 3px; min-height: 18px; }
    .rdist { display: flex; flex-direction: column; gap: 8px; }
    .rdist-row { display: flex; align-items: center; gap: 8px; }
    .rdist-star { font-size: 12px; color: var(--accent-warm); width: 28px; }
    .rdist-bar { flex: 1; height: 8px; background: var(--bg-tertiary); border-radius: var(--radius-full); overflow: hidden; }
    .rdist-fill { height: 100%; background: var(--accent-warm); border-radius: var(--radius-full); }
    .rdist-ct { font-size: 11px; color: var(--text-secondary); width: 24px; text-align: right; }
    @media (max-width: 768px) { .stat-top { grid-template-columns: repeat(2,1fr); } .stat-grid { grid-template-columns: 1fr; } }
  `],
})
export class StatisticsComponent {
  tabs = ['This Week','This Month','This Year','All Time'];
  activeTab = 'This Week';
  weekly = MOCK_WEEKLY_DATA;
  categories = MOCK_CATEGORY_BREAKDOWN;
  heatmap = MOCK_HEATMAP_DATA;
  ratings = MOCK_RATING_DISTRIBUTION;
  focus = MOCK_FOCUS_ANALYTICS;
  maxR = Math.max(...MOCK_RATING_DISTRIBUTION.map(r => r.count));
  topStats = [
    { label: 'Total Hours', value: '827h', change: '+48h', trend: 'up' as const, color: 'var(--accent-primary)' },
    { label: 'Activities', value: '1,247', change: '+23', trend: 'up' as const, color: 'var(--accent-secondary)' },
    { label: 'Avg Rating', value: '4.2', color: 'var(--accent-warm)' },
    { label: 'Focus Score', value: '127', change: '+12', trend: 'up' as const, color: 'var(--accent-emerald)' },
  ];
  getHC(v: number): string {
    if (v === 0) return 'var(--bg-tertiary)';
    if (v <= 2) return 'rgba(139,92,246,0.25)';
    if (v <= 4) return 'rgba(139,92,246,0.5)';
    return 'rgba(139,92,246,0.8)';
  }
}
