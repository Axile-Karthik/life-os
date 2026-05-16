import { Component, Input } from '@angular/core';
import { TimelineEvent } from '../../models/activity.model';
import { WidgetContainerComponent } from '../../components/widget-container.component';
import { RequestState } from '../../models/request-state.model';
import { ACTIVITY_TYPE_REGISTRY, getActivityVisuals } from '../../registry/activity-type.registry';
import { RelativeTimePipe } from '../../pipes/relative-time.pipe';

@Component({
  selector: 'app-timeline-feed',
  standalone: true,
  imports: [WidgetContainerComponent, RelativeTimePipe],
  template: `
    <app-widget-container title="Memory Timeline" icon="⏱️">
      <span widget-actions class="view-all-link">Full Log</span>
      <div class="timeline">
        @if (state.loading && !state.data) {
          @for (i of [1, 2, 3, 4]; track i; let last = $last) {
            <div class="timeline-item">
              <div class="timeline-item__icon-wrapper">
                <div class="timeline-item__icon skeleton-glow" style="background: rgba(255,255,255,0.05);"></div>
                @if (!last) { <div class="timeline-item__line"></div> }
              </div>
              <div class="timeline-item__content">
                <div class="skeleton-shimmer" style="width: 120px; height: 16px; border-radius: 4px; margin-bottom: 6px;"></div>
                <div class="skeleton-shimmer" style="width: 180px; height: 12px; border-radius: 4px; margin-bottom: 8px;"></div>
                <div class="skeleton-shimmer" style="width: 50px; height: 14px; border-radius: 4px;"></div>
              </div>
            </div>
          }
        } @else if (state.data) {
          @for (event of state.data.slice(0, limit); track event.id; let last = $last) {
            <div class="timeline-item hover-lift">
              <div class="timeline-item__icon-wrapper">
                <div class="timeline-item__icon" [style.background]="getVisuals(event.category).color + '20'" [style.color]="getVisuals(event.category).color">
                  {{ getVisuals(event.category).icon }}
                </div>
                @if (!last) {
                  <div class="timeline-item__line"></div>
                }
              </div>
              <div class="timeline-item__content">
                <div class="timeline-item__header">
                  <span class="timeline-item__title">{{ event.title }}</span>
                  <span class="timeline-item__time">{{ event.timestamp | relativeTime }}</span>
                </div>
                @if (event.description) {
                  <p class="timeline-item__desc">{{ event.description }}</p>
                }
                <span class="timeline-item__badge" [style.color]="getVisuals(event.category).color">
                  {{ getVisuals(event.category).label }}
                </span>
              </div>
            </div>
          }
          @if (state.data.length === 0) {
            <div class="empty-state">No recent activities found.</div>
          }
        } @else if (state.error) {
          <div class="empty-state" style="color: var(--error);">Failed to load timeline.</div>
        }
      </div>
    </app-widget-container>
  `,
  styles: [`
    .view-all-link { font-size: 11px; color: var(--accent-primary); cursor: pointer; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; }
    .view-all-link:hover { text-decoration: underline; }
    .timeline { display: flex; flex-direction: column; gap: 0; padding-top: 8px; }
    .timeline-item { display: flex; gap: 16px; border-radius: var(--radius-md); padding: 8px; margin-left: -8px; transition: background var(--transition-fast); }
    .timeline-item:hover { background: var(--card-bg-hover); }
    .timeline-item__icon-wrapper { display: flex; flex-direction: column; align-items: center; width: 24px; flex-shrink: 0; }
    .timeline-item__icon { width: 24px; height: 24px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 10px; z-index: 1; }
    .timeline-item__line { width: 1px; flex: 1; background: var(--card-border); margin-top: 4px; margin-bottom: -12px; }
    .timeline-item__content { flex: 1; padding-bottom: 20px; }
    .timeline-item__header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 4px; }
    .timeline-item__title { font-size: 13px; font-weight: 600; color: var(--text-primary); }
    .timeline-item__time { font-size: 10px; color: var(--text-tertiary); white-space: nowrap; }
    .timeline-item__desc { font-size: 12px; color: var(--text-secondary); margin-bottom: 8px; }
    .timeline-item__badge { font-size: 9px; text-transform: uppercase; letter-spacing: 1px; font-weight: 600; display: inline-block; padding: 2px 6px; border-radius: 4px; background: var(--bg-tertiary); }
    .empty-state { padding: 20px; text-align: center; color: var(--text-secondary); font-size: 12px; border: 1px dashed var(--card-border); border-radius: var(--radius-md); margin-top: 8px; }
    
    /* Cinematic Skeletons */
    .skeleton-glow { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); animation: pulse 2s infinite ease-in-out; }
    .skeleton-shimmer { background: linear-gradient(90deg, rgba(255,255,255,0.02) 25%, rgba(255,255,255,0.05) 50%, rgba(255,255,255,0.02) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite linear; }
    @keyframes pulse { 0% { opacity: 0.5; } 50% { opacity: 0.8; } 100% { opacity: 0.5; } }
    @keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
  `]
})
export class TimelineFeedWidget {
  @Input({ required: true }) state!: RequestState<TimelineEvent[]>;
  @Input() limit: number = 4;

  getVisuals(category: string) {
    // If the category passed matches exactly a registry key
    const cLower = category.toLowerCase();
    return ACTIVITY_TYPE_REGISTRY[cLower] || ACTIVITY_TYPE_REGISTRY['default'];
  }
}
