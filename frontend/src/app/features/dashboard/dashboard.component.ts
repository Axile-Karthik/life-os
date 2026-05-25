import { Component, inject, computed, signal, OnInit } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { HeroObservatoryWidget } from '../../shared/widgets/hero-observatory/hero-observatory.widget';
import { MediaStripWidget } from '../../shared/widgets/media-strip/media-strip.widget';
import { ModularMediaWidget } from '../../shared/widgets/modular-media/modular-media.widget';
import { ObservabilityWidget } from '../../shared/widgets/observability/observability.widget';
import { TimelineFeedWidget } from '../../shared/widgets/timeline-feed/timeline-feed.widget';
import { StatisticsWidget } from '../../shared/widgets/statistics/statistics.widget';
import { QuickActionsWidget } from '../../shared/widgets/quick-actions/quick-actions.widget';
import { TasksPreviewWidget } from '../../shared/widgets/tasks-preview/tasks-preview.widget';

import { DashboardState } from '../../core/state/dashboard.state';
import { SessionAdapter } from '../../shared/adapters/session.adapter';
import { MetricsAdapter } from '../../shared/adapters/metrics.adapter';
import { RequestState, initialRequestState, loadedRequestState } from '../../shared/models/request-state.model';
import { MediaItem, TimelineEvent, HeroStats, TaskItem } from '../../shared/models/activity.model';
import { TaskApiService } from '../../core/api/services/task-api.service';

const DEFAULT_HERO_STATS: HeroStats = {
  focusScore: 0,
  hoursToday: 0,
  currentStreak: 0,
  totalActivities: 0,
  watched: 0,
  hoursThisYear: 0,
  avgRating: 0
};

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    PageContainerComponent,
    HeroObservatoryWidget,
    MediaStripWidget,
    ModularMediaWidget,
    ObservabilityWidget,
    TimelineFeedWidget,
    StatisticsWidget,
    QuickActionsWidget,
    TasksPreviewWidget
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnInit {
  private dashboard = inject(DashboardState);
  private taskApi = inject(TaskApiService);

  private _tasksState = signal<RequestState<TaskItem[]>>(initialRequestState());
  tasksState = this._tasksState.asReadonly();

  ngOnInit() {
    this.taskApi.getTasks().subscribe({
      next: (data) => this._tasksState.set(loadedRequestState(data)),
      error: (err) => this._tasksState.set(initialRequestState())
    });
  }

  // Helper to map sessions RequestState to a filtered RequestState<MediaItem[]>
  private getMediaState(type: string): RequestState<MediaItem[]> {
    const state = this.dashboard.sessions();
    if (state.loading && !state.data) return { ...state, data: null } as any;
    if (state.error) return { ...state, data: null } as any;
    if (state.data) {
      const filtered = state.data.filter(s => s.type.toLowerCase() === type).map(s => SessionAdapter.toMediaItem(s));
      return { ...state, data: filtered };
    }
    return initialRequestState();
  }

  heroState = computed(() => {
    const state = this.dashboard.sessions();
    const stats = this.dashboard.heroStats();
    if (state.loading && !stats) return { ...state, data: null } as any;
    if (state.error && !stats) return { ...state, data: null } as any;
    if (stats) {
       return { ...state, data: { ...DEFAULT_HERO_STATS, focusScore: stats.focusScore, hoursToday: Number(stats.totalHours), currentStreak: stats.currentStreak } as HeroStats }; 
    }
    return initialRequestState();
  });

  observabilityState = computed(() => {
     const state = this.dashboard.metrics();
     if (state.loading && !state.data) return { ...state, data: null } as any;
     if (state.error && !state.data) return { ...state, data: null } as any;
     if (state.data) {
       const latest = state.data.length > 0 ? state.data[0] : null;
       return { ...state, data: MetricsAdapter.toObservabilityStatus(latest) };
     }
     return initialRequestState();
  });

  timelineState = computed(() => {
     const state = this.dashboard.sessions();
     if (state.loading && !state.data) return { ...state, data: null } as any;
     if (state.error && !state.data) return { ...state, data: null } as any;
     if (state.data) {
       const events = state.data.map(s => SessionAdapter.toTimelineEvent(s));
       return { ...state, data: events };
     }
     return initialRequestState();
  });

  gamesState = computed(() => this.getMediaState('game'));
  booksState = computed(() => this.getMediaState('book'));
  mangaState = computed(() => this.getMediaState('manga'));
  animeState = computed(() => this.getMediaState('anime'));
  musicState = computed(() => this.getMediaState('music'));

  statisticsState = computed(() => {
    const state = this.dashboard.sessions();
    if (state.loading && !state.data) return { ...state, data: null } as any;
    if (state.error) return { ...state, data: null } as any;
    if (state.data) {
      // 1. Calculate Heatmap (5 weeks x 7 days)
      const now = new Date();
      now.setHours(23, 59, 59, 999);
      const heatmap: number[][] = Array(5).fill(0).map(() => Array(7).fill(0));
      const startMs = now.getTime() - 34 * 24 * 60 * 60 * 1000;
      
      const dayDurations: Record<string, number> = {};
      for (const s of state.data) {
        const dateStr = new Date(s.startTime).toDateString();
        const durHours = s.durationMillis / (1000 * 3600);
        dayDurations[dateStr] = (dayDurations[dateStr] || 0) + durHours;
      }
      
      for (let i = 0; i < 35; i++) {
        const targetDate = new Date(startMs + i * 24 * 60 * 60 * 1000);
        const hours = dayDurations[targetDate.toDateString()] || 0;
        const row = Math.floor(i / 7);
        const col = i % 7;
        heatmap[row][col] = Math.round(hours * 10) / 10;
      }

      // 2. Calculate Category Breakdown
      const categoryMap: Record<string, number> = {};
      let totalDuration = 0;
      for (const s of state.data) {
        const cat = s.type ? s.type.charAt(0).toUpperCase() + s.type.slice(1).toLowerCase() : 'Other';
        const durHours = s.durationMillis / (1000 * 3600);
        categoryMap[cat] = (categoryMap[cat] || 0) + durHours;
        totalDuration += durHours;
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

      const categories = Object.entries(categoryMap).map(([category, value]) => {
        const pct = totalDuration > 0 ? Math.round((value / totalDuration) * 100) : 0;
        return {
          category,
          value: pct,
          color: colors[category] || colors['Other']
        };
      }).sort((a, b) => b.value - a.value);

      return {
        ...state,
        data: {
          heatmap,
          categories: categories.length > 0 ? categories : [{ category: 'None', value: 100, color: 'var(--text-tertiary)' }]
        }
      };
    }
    return initialRequestState();
  });

  recentMediaState = computed(() => {
    const state = this.dashboard.sessions();
    if (state.data && state.data.length > 0) {
        return { ...state, data: state.data.map(s => SessionAdapter.toMediaItem(s)) };
    }
    if (state.loading) return { ...state, data: null } as any;
    return { loading: false, error: null, data: [], isStale: false };
  });
}
