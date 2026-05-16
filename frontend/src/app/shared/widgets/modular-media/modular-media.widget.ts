import { Component, Input, computed, signal } from '@angular/core';
import { MediaItem } from '../../models/activity.model';
import { WidgetContainerComponent } from '../../components/widget-container.component';
import { ProgressBarComponent } from '../../components/progress-bar.component';
import { MediaCardComponent } from '../../components/media-card.component';
import { RequestState } from '../../models/request-state.model';

@Component({
  selector: 'app-modular-media',
  standalone: true,
  imports: [WidgetContainerComponent, ProgressBarComponent, MediaCardComponent],
  template: `
    <app-widget-container [title]="title" [icon]="icon">
      <span widget-actions class="view-all-link">View all</span>
      
      @if (state.loading && !state.data) {
        <!-- Cinematic Skeleton -->
        <div class="current-media">
          <div class="skeleton-shimmer" style="width: 100px; height: 12px; margin-bottom: 12px; border-radius: 4px;"></div>
          <div class="current-media__row">
            <div class="current-media__cover skeleton-glow"></div>
            <div class="current-media__info">
              <div class="skeleton-shimmer" style="width: 80%; height: 18px; margin-bottom: 6px; border-radius: 4px;"></div>
              <div class="skeleton-shimmer" style="width: 60%; height: 14px; margin-bottom: 12px; border-radius: 4px;"></div>
              <div class="skeleton-shimmer" style="width: 100%; height: 6px; border-radius: 3px;"></div>
            </div>
          </div>
        </div>
      } @else if (state.data) {
        
        @if (currentItems().length > 0) {
          <div class="current-media">
            <div class="current-media__label">
              Currently {{ statusLabel }}
              @if (state.isStale) { <span class="stale-dot" title="Telemetry delayed"></span> }
            </div>
            <div class="current-media__row">
              @if (currentItems()[0].imageUrl) {
                <img [src]="currentItems()[0].imageUrl" [alt]="currentItems()[0].title" class="current-media__cover hover-lift"/>
              } @else {
                <div class="current-media__cover fallback-cover hover-lift" [style.background]="color">
                  <span class="fallback-icon">{{ icon }}</span>
                </div>
              }
              <div class="current-media__info">
                <h4 class="current-media__title">{{ currentItems()[0].title }}</h4>
                <p class="current-media__subtitle">{{ currentItems()[0].subtitle }}</p>
                
                @if (currentItems()[0].progress !== undefined) {
                  <div class="progress-wrapper">
                    <app-progress-bar [value]="currentItems()[0].progress || 0" [color]="color" />
                    <span class="current-media__pct">{{ currentItems()[0].progress }}%</span>
                  </div>
                }
                
                @if (currentItems()[0].metadata) {
                  <div class="metadata-tags">
                    @for (key of getKeys(currentItems()[0].metadata); track key) {
                      <span class="meta-tag">{{ key }}: {{ currentItems()[0].metadata![key] }}</span>
                    }
                  </div>
                }
              </div>
            </div>
          </div>
        } @else {
          <div class="empty-state">
            No active {{ title.toLowerCase() }} sessions.
          </div>
        }

        @if (recentItems().length > 0) {
          <div class="recent-list">
            <div class="recent-list__label">Recent {{ title }}</div>
            <div class="recent-list__scroll scroll-x hide-scrollbar">
              @for (item of recentItems(); track item.id) {
                <app-media-card 
                  [title]="item.title" 
                  [imageUrl]="item.imageUrl" 
                  [subtitle]="item.subtitle || ''" 
                  [progress]="item.progress" 
                  layout="horizontal" />
              }
            </div>
          </div>
        }

      } @else if (state.error) {
        <div class="empty-state" style="color: var(--error);">
          Failed to load {{ title.toLowerCase() }} data.
        </div>
      }
    </app-widget-container>
  `,
  styles: [`
    .view-all-link { font-size: 11px; color: var(--accent-primary); cursor: pointer; font-weight: 500; text-transform: uppercase; letter-spacing: 0.5px; }
    .view-all-link:hover { text-decoration: underline; }
    .current-media { margin-bottom: 20px; }
    .current-media__label { font-size: 11px; color: var(--text-tertiary); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 12px; font-weight: 600; display: flex; align-items: center; gap: 6px; }
    .stale-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--warning); display: inline-block; }
    .current-media__row { display: flex; gap: 16px; }
    .current-media__cover { width: 72px; height: 100px; border-radius: var(--radius-sm); object-fit: cover; flex-shrink: 0; box-shadow: 0 4px 12px rgba(0,0,0,0.3); }
    .fallback-cover { display: flex; align-items: center; justify-content: center; font-size: 32px; background: var(--bg-tertiary); }
    .current-media__info { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; }
    .current-media__title { font-size: 16px; font-weight: 700; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 2px; }
    .current-media__subtitle { font-size: 13px; color: var(--text-secondary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 12px; }
    .progress-wrapper { margin-bottom: 8px; }
    .current-media__pct { font-size: 11px; color: var(--text-tertiary); margin-top: 6px; display: block; font-weight: 600; }
    .metadata-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: auto; }
    .meta-tag { font-size: 10px; padding: 2px 6px; background: var(--bg-tertiary); border-radius: 4px; color: var(--text-secondary); text-transform: capitalize; }
    .recent-list__label { font-size: 11px; color: var(--text-tertiary); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 12px; font-weight: 600; }
    .recent-list__scroll { display: flex; flex-direction: column; gap: 8px; max-height: 200px; }
    .empty-state { padding: 20px; text-align: center; color: var(--text-secondary); font-size: 12px; border: 1px dashed var(--card-border); border-radius: var(--radius-md); }
    
    /* Cinematic Skeletons */
    .skeleton-glow { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); animation: pulse 2s infinite ease-in-out; }
    .skeleton-shimmer { background: linear-gradient(90deg, rgba(255,255,255,0.02) 25%, rgba(255,255,255,0.05) 50%, rgba(255,255,255,0.02) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite linear; }
    @keyframes pulse { 0% { opacity: 0.5; } 50% { opacity: 0.8; } 100% { opacity: 0.5; } }
    @keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
  `]
})
export class ModularMediaWidget {
  @Input({ required: true }) title!: string;
  @Input({ required: true }) icon!: string;
  @Input() statusLabel: string = 'Playing';
  @Input() color: string = 'var(--accent-primary)';
  
  // We use setter to convert Input into a local signal for computeds
  private _state = signal<RequestState<MediaItem[]>>({ data: null, loading: false, error: null });
  @Input({ required: true }) set state(val: RequestState<MediaItem[]>) {
    this._state.set(val);
  }
  get state() { return this._state(); }

  currentItems = computed(() => {
    const d = this._state().data;
    return d && d.length > 0 ? d.slice(0, 1) : [];
  });
  
  recentItems = computed(() => {
    const d = this._state().data;
    return d && d.length > 1 ? d.slice(1) : [];
  });

  getKeys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }
}
