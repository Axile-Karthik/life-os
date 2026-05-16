import { Component, inject, computed } from '@angular/core';
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
import { MediaItem, TimelineEvent, HeroStats } from '../../shared/models/activity.model';

import {
  MOCK_HERO_STATS, MOCK_RECENT_MEDIA, MOCK_WATCHLIST, MOCK_TASKS,
  MOCK_HEATMAP_DATA, MOCK_CATEGORY_BREAKDOWN
} from '../../shared/data/mock-data';

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
export class DashboardComponent {
  private dashboard = inject(DashboardState);

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
       return { ...state, data: { ...MOCK_HERO_STATS, focusScore: stats.focusScore, hoursToday: Number(stats.totalHours) } as HeroStats }; 
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

  // The rest can be mock data wrapped in loadedRequestState for now until APIs exist
  tasksState = computed(() => loadedRequestState(MOCK_TASKS));
  statisticsState = computed(() => loadedRequestState({ heatmap: MOCK_HEATMAP_DATA, categories: MOCK_CATEGORY_BREAKDOWN }));
  recentMediaState = computed(() => {
    // If sessions loaded, use them, otherwise use mock
    const state = this.dashboard.sessions();
    if (state.data && state.data.length > 0) {
        return { ...state, data: state.data.map(s => SessionAdapter.toMediaItem(s)) };
    }
    if (state.loading) return { ...state, data: null } as any;
    return loadedRequestState(MOCK_RECENT_MEDIA);
  });
}
