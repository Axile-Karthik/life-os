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
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.css',
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
