import { Component, inject, OnInit } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { StatsCardComponent } from '../../shared/components/stats-card.component';
import { WidgetContainerComponent } from '../../shared/components/widget-container.component';
import { SessionApiService } from '../../core/api/services/session-api.service';
import { WeeklyData, CategoryBreakdown, StatItem } from '../../shared/models/activity.model';

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
                <div class="bar" [style.height.%]="getBarHeightPct(d.hours)"></div>
                <span class="bar-label">{{ d.day }}</span>
              </div>
            }
            @if (weekly.length === 0) {
              <div class="empty-state">No weekly activity data found.</div>
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
            @if (categories.length === 0) {
              <div class="empty-state">No category data.</div>
            }
          </div>
        </app-widget-container>
        <app-widget-container title="Heatmap">
          <div class="heat-grid">
            @for (week of heatmap; track $index) {
              @for (v of week; track $index) {
                <div class="heat-cell" [style.background]="getHC(v)" [title]="v + ' hours'"></div>
              }
            }
          </div>
        </app-widget-container>
        <app-widget-container title="Rating Distribution">
          <div class="rdist">
            <div class="empty-state">Rating metrics pending API integration.</div>
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
    .bar-chart { display: flex; align-items: flex-end; gap: 12px; height: 140px; width: 100%; justify-content: space-around; }
    .bar-col { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 6px; height: 100%; justify-content: flex-end; }
    .bar { width: 100%; max-width: 24px; background: var(--accent-primary); border-radius: 4px 4px 0 0; min-height: 4px; transition: height .5s ease; }
    .bar-label { font-size: 11px; color: var(--text-tertiary); }
    .cat-list { display: flex; flex-direction: column; gap: 10px; }
    .cat-row { display: flex; align-items: center; gap: 8px; }
    .cat-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
    .cat-name { font-size: 12px; color: var(--text-secondary); width: 65px; }
    .cat-bar { flex: 1; height: 6px; background: var(--bg-tertiary); border-radius: var(--radius-full); overflow: hidden; }
    .cat-fill { height: 100%; border-radius: var(--radius-full); }
    .cat-pct { font-size: 11px; color: var(--text-tertiary); width: 30px; text-align: right; }
    .heat-grid { display: grid; grid-template-columns: repeat(7,1fr); gap: 3px; }
    .heat-cell { aspect-ratio: 1; border-radius: 3px; min-height: 18px; }
    .rdist { display: flex; flex-direction: column; gap: 8px; }
    .empty-state { padding: 40px 20px; text-align: center; color: var(--text-secondary); font-size: 12px; flex: 1; }
    @media (max-width: 768px) { .stat-top { grid-template-columns: repeat(2,1fr); } .stat-grid { grid-template-columns: 1fr; } }
  `],
})
export class StatisticsComponent implements OnInit {
  private sessionApi = inject(SessionApiService);

  tabs = ['This Week','This Month','This Year','All Time'];
  activeTab = 'This Week';
  
  weekly: WeeklyData[] = [];
  categories: CategoryBreakdown[] = [];
  heatmap: number[][] = Array(5).fill(0).map(() => Array(7).fill(0));
  topStats: StatItem[] = [];

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.sessionApi.getSessions().subscribe({
      next: (sessions) => {
        this.processSessions(sessions);
      },
      error: () => {
        this.resetStats();
      }
    });

    this.sessionApi.getWeeklyStats().subscribe({
      next: (weeklyStats) => {
        this.weekly = weeklyStats.slice(-7).map(item => ({
          day: item.week ? item.week.substring(5) : 'Wk', // format "2026-W21" -> "W21"
          hours: Math.round((item.totalDurationMillis / (1000 * 3600)) * 10) / 10
        }));
      },
      error: () => {
        this.weekly = [];
      }
    });
  }

  processSessions(sessions: any[]) {
    // 1. Heatmap (last 5 weeks / 35 days)
    const now = new Date();
    now.setHours(23, 59, 59, 999);
    const startMs = now.getTime() - 34 * 24 * 60 * 60 * 1000;
    
    const dayDurations: Record<string, number> = {};
    let totalDurationMillis = 0;
    for (const s of sessions) {
      const dateStr = new Date(s.startTime).toDateString();
      const dur = s.durationMillis || 0;
      dayDurations[dateStr] = (dayDurations[dateStr] || 0) + (dur / (1000 * 3600));
      totalDurationMillis += dur;
    }

    const newHeatmap: number[][] = Array(5).fill(0).map(() => Array(7).fill(0));
    for (let i = 0; i < 35; i++) {
      const targetDate = new Date(startMs + i * 24 * 60 * 60 * 1000);
      const hours = dayDurations[targetDate.toDateString()] || 0;
      const row = Math.floor(i / 7);
      const col = i % 7;
      newHeatmap[row][col] = Math.round(hours * 10) / 10;
    }
    this.heatmap = newHeatmap;

    // 2. Categories
    const categoryMap: Record<string, number> = {};
    let totalDurationHours = 0;
    for (const s of sessions) {
      const cat = s.type ? s.type.charAt(0).toUpperCase() + s.type.slice(1).toLowerCase() : 'Other';
      const hours = (s.durationMillis || 0) / (1000 * 3600);
      categoryMap[cat] = (categoryMap[cat] || 0) + hours;
      totalDurationHours += hours;
    }

    const colors: Record<string, string> = {
      Coding: 'var(--accent-primary)',
      Game: 'var(--accent-secondary)',
      Movie: 'var(--accent-warm)',
      Music: 'var(--accent-cyan)',
      Book: 'var(--accent-emerald)',
      Manga: 'var(--accent-warm)',
      Anime: 'var(--accent-rose)',
      Other: 'var(--text-tertiary)'
    };

    this.categories = Object.entries(categoryMap).map(([category, val]) => {
      const pct = totalDurationHours > 0 ? Math.round((val / totalDurationHours) * 100) : 0;
      return {
        category,
        value: pct,
        color: colors[category] || colors['Other']
      };
    }).sort((a, b) => b.value - a.value);

    // 3. Top Stats Cards
    const totalHours = Math.round((totalDurationMillis / (1000 * 3600)) * 10) / 10;
    const avgDurationMins = sessions.length > 0 ? Math.round((totalDurationMillis / sessions.length) / 60000) : 0;
    
    // Dynamically derive focus score based on hours logged today
    const startOfToday = new Date();
    startOfToday.setHours(0, 0, 0, 0);
    const todayMillis = sessions
      .filter(s => new Date(s.startTime).getTime() >= startOfToday.getTime())
      .reduce((acc, s) => acc + (s.durationMillis || 0), 0);
    const todayHours = todayMillis / (1000 * 3600);
    const focusScore = Math.min(100, Math.round((todayHours / 8) * 100));

    this.topStats = [
      { label: 'Total Hours', value: `${totalHours}h`, color: 'var(--accent-primary)' },
      { label: 'Activities', value: sessions.length.toLocaleString(), color: 'var(--accent-secondary)' },
      { label: 'Avg Duration', value: `${avgDurationMins}m`, color: 'var(--accent-warm)' },
      { label: 'Focus Score', value: focusScore.toString(), color: 'var(--accent-emerald)' },
    ];
  }

  resetStats() {
    this.weekly = [];
    this.categories = [];
    this.heatmap = Array(5).fill(0).map(() => Array(7).fill(0));
    this.topStats = [
      { label: 'Total Hours', value: '0h', color: 'var(--accent-primary)' },
      { label: 'Activities', value: '0', color: 'var(--accent-secondary)' },
      { label: 'Avg Duration', value: '0m', color: 'var(--accent-warm)' },
      { label: 'Focus Score', value: '0', color: 'var(--accent-emerald)' },
    ];
  }

  getBarHeightPct(hours: number): number {
    const maxHours = Math.max(...this.weekly.map(w => w.hours), 1);
    return (hours / maxHours) * 100;
  }

  getHC(v: number): string {
    if (v === 0) return 'var(--bg-tertiary)';
    if (v <= 2) return 'rgba(139,92,246,0.25)';
    if (v <= 4) return 'rgba(139,92,246,0.5)';
    return 'rgba(139,92,246,0.8)';
  }
}
