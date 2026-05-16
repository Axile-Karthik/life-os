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
  template: `
    <app-page-container>
      <app-section-header title="Activity Timeline" subtitle="Your chronological memory" />
      <div class="timeline-filters">
        @for (f of filters; track f) {
          <button class="filter-btn" [class.filter-btn--active]="f === activeFilter()" (click)="setFilter(f)">{{ f }}</button>
        }
      </div>
      <app-card>
        @if (timelineState().loading && !timelineState().data) {
          @for (i of [1, 2, 3, 4, 5, 6]; track i) {
            <div class="skeleton-activity">
              <div class="skeleton-dot skeleton-glow"></div>
              <div class="skeleton-content">
                <div class="skeleton-shimmer" style="width: 200px; height: 16px; border-radius: 4px; margin-bottom: 8px;"></div>
                <div class="skeleton-shimmer" style="width: 100px; height: 12px; border-radius: 4px;"></div>
              </div>
            </div>
          }
        } @else if (timelineState().data) {
          @for (event of timelineState().data; track event.id) {
            <app-activity-item 
              [title]="event.title" 
              [category]="event.category" 
              [timestamp]="event.timestamp" 
              [description]="event.description || ''" />
          }
          @if (timelineState().data!.length === 0) {
            <div class="empty-state">No activities match the current filter.</div>
          }
        } @else if (timelineState().error) {
          <div class="empty-state error-state">Failed to load timeline events.</div>
        }
      </app-card>
    </app-page-container>
  `,
  styles: [`
    .timeline-filters { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
    .filter-btn { padding: 6px 16px; border-radius: var(--radius-full); background: var(--card-bg); border: 1px solid var(--card-border); color: var(--text-secondary); font-size: 13px; font-weight: 500; transition: all var(--transition-fast); cursor: pointer; }
    .filter-btn:hover { border-color: var(--card-border-hover); color: var(--text-primary); }
    .filter-btn--active { background: var(--accent-primary-dim); border-color: var(--accent-primary); color: var(--accent-primary); }
    
    .empty-state { padding: 40px 20px; text-align: center; color: var(--text-secondary); font-size: 14px; }
    .error-state { color: var(--error); }

    /* Skeletons */
    .skeleton-activity { display: flex; align-items: flex-start; gap: 12px; padding: 12px 0; }
    .skeleton-dot { width: 11px; height: 11px; border-radius: 50%; margin-top: 4px; flex-shrink: 0; background: rgba(255,255,255,0.05); }
    .skeleton-content { flex: 1; }
    
    .skeleton-glow { background: rgba(255,255,255,0.02); animation: pulse 2s infinite ease-in-out; }
    .skeleton-shimmer { background: linear-gradient(90deg, rgba(255,255,255,0.02) 25%, rgba(255,255,255,0.05) 50%, rgba(255,255,255,0.02) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite linear; }
    @keyframes pulse { 0% { opacity: 0.5; } 50% { opacity: 0.8; } 100% { opacity: 0.5; } }
    @keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
  `],
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
