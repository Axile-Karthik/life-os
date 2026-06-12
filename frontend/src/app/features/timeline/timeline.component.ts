import { Component, inject, computed, signal } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { AppCardComponent } from '../../shared/components/app-card.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { ActivityItemComponent } from '../../shared/components/activity-item.component';
import { DashboardState } from '../../core/state/dashboard.state';
import { SessionAdapter } from '../../shared/adapters/session.adapter';
import { RequestState } from '../../shared/models/request-state.model';
import { TimelineEvent } from '../../shared/models/activity.model';

@Component({
  selector: 'app-timeline',
  standalone: true,
  imports: [PageContainerComponent, AppCardComponent, SectionHeaderComponent, ActivityItemComponent],
  templateUrl: './timeline.component.html',
  styleUrl: './timeline.component.css',
})
export class TimelineComponent {
  private dashboard = inject(DashboardState);
  
  filters = ['All', 'Movie', 'Game', 'Anime', 'Book', 'Manga', 'Music'];
  activeFilter = signal('All');

  timelineState = computed((): RequestState<TimelineEvent[]> => {
    const state = this.dashboard.sessions();
    if (state.loading && !state.data) return { ...state, data: null } as any;
    if (state.error && !state.data) return { ...state, data: null } as any;
    if (state.data) {
       const events = state.data.map(s => SessionAdapter.toTimelineEvent(s));
       const filter = this.activeFilter();
       const filtered = filter === 'All' ? events : events.filter(e => e.category.toLowerCase() === filter.toLowerCase());
       return { ...state, data: filtered };
    }
    return { loading: false, error: null, data: [], isStale: false };
  });

  setFilter(f: string) {
    this.activeFilter.set(f);
  }
}
