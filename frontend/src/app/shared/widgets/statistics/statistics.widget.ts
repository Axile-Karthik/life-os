import { Component, Input } from '@angular/core';
import { WidgetContainerComponent } from '../../components/widget-container.component';
import { RequestState } from '../../models/request-state.model';
import { CategoryBreakdown } from '../../models/activity.model';

export interface StatisticsData {
  heatmap: number[][];
  categories: CategoryBreakdown[];
}

@Component({
  selector: 'app-statistics',
  standalone: true,
  imports: [WidgetContainerComponent],
  template: `
    <app-widget-container title="Telemetry Analysis" icon="📊">
      @if (state.loading && !state.data) {
        <div class="stats-section">
          <div class="skeleton-shimmer" style="width: 120px; height: 16px; border-radius: 4px; margin-bottom: 12px;"></div>
          <div class="skeleton-glow" style="width: 100%; height: 100px; border-radius: 8px;"></div>
        </div>
        <div class="stats-divider"></div>
        <div class="stats-section">
          <div class="skeleton-shimmer" style="width: 120px; height: 16px; border-radius: 4px; margin-bottom: 12px;"></div>
          <div class="skeleton-glow" style="width: 100%; height: 12px; border-radius: 6px; margin-bottom: 12px;"></div>
          <div style="display: flex; gap: 12px; flex-wrap: wrap;">
            <div class="skeleton-shimmer" style="width: 80px; height: 12px; border-radius: 4px;"></div>
            <div class="skeleton-shimmer" style="width: 80px; height: 12px; border-radius: 4px;"></div>
          </div>
        </div>
      } @else if (state.data) {
        <div class="stats-section">
          <div class="stats-section__header">
            <span class="stats-section__title">Activity Heatmap</span>
            <span class="stats-section__badge">Last 5 Weeks</span>
          </div>
          <div class="heatmap">
            <div class="heatmap__days">
              @for (d of days; track d) { <span class="heatmap__day">{{ d }}</span> }
            </div>
            <div class="heatmap__grid">
              @for (week of state.data.heatmap; track $index) {
                @for (val of week; track $index) {
                  <div class="heatmap__cell" [style.background]="getHeatColor(val)" [title]="val + 'h'"></div>
                }
              }
            </div>
          </div>
        </div>
        
        <div class="stats-divider"></div>
        
        <div class="stats-section">
          <div class="stats-section__header">
            <span class="stats-section__title">Category Distribution</span>
          </div>
          <div class="distribution">
            <div class="distribution__bar-container">
              @for (cat of state.data.categories; track cat.category) {
                <div class="distribution__segment" [style.width.%]="cat.value" [style.background]="cat.color" [title]="cat.category + ' (' + cat.value + '%)'"></div>
              }
            </div>
            <div class="distribution__legend">
              @for (cat of state.data.categories.slice(0, 4); track cat.category) {
                <div class="legend-item">
                  <span class="legend-dot" [style.background]="cat.color"></span>
                  <span class="legend-label">{{ cat.category }}</span>
                </div>
              }
            </div>
          </div>
        </div>
      } @else if (state.error) {
        <div class="empty-state" style="color: var(--error);">Failed to load telemetry analysis.</div>
      }
    </app-widget-container>
  `,
  styles: [`
    .stats-section { margin-bottom: 16px; }
    .stats-section__header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
    .stats-section__title { font-size: 11px; color: var(--text-tertiary); text-transform: uppercase; letter-spacing: 1px; font-weight: 600; }
    .stats-section__badge { font-size: 9px; color: var(--accent-primary); background: var(--accent-primary-dim); padding: 2px 6px; border-radius: 4px; font-weight: 600; }
    .stats-divider { height: 1px; background: var(--card-border); margin: 20px 0; }
    .heatmap__days { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; margin-bottom: 6px; }
    .heatmap__day { font-size: 10px; color: var(--text-tertiary); text-align: center; }
    .heatmap__grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; }
    .heatmap__cell { aspect-ratio: 1; border-radius: 4px; min-height: 20px; transition: all var(--transition-fast); cursor: pointer; }
    .heatmap__cell:hover { transform: scale(1.15); box-shadow: 0 0 12px rgba(139, 92, 246, 0.4); z-index: 10; }
    .distribution__bar-container { height: 12px; border-radius: var(--radius-full); overflow: hidden; display: flex; margin-bottom: 12px; }
    .distribution__segment { height: 100%; transition: opacity var(--transition-fast); }
    .distribution__segment:hover { opacity: 0.8; }
    .distribution__legend { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
    .legend-item { display: flex; align-items: center; gap: 6px; }
    .legend-dot { width: 8px; height: 8px; border-radius: 50%; }
    .legend-label { font-size: 11px; color: var(--text-secondary); }
    .empty-state { padding: 20px; text-align: center; color: var(--text-secondary); font-size: 12px; border: 1px dashed var(--card-border); border-radius: var(--radius-md); }
    
    /* Cinematic Skeletons */
    .skeleton-glow { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); animation: pulse 2s infinite ease-in-out; }
    .skeleton-shimmer { background: linear-gradient(90deg, rgba(255,255,255,0.02) 25%, rgba(255,255,255,0.05) 50%, rgba(255,255,255,0.02) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite linear; }
    @keyframes pulse { 0% { opacity: 0.5; } 50% { opacity: 0.8; } 100% { opacity: 0.5; } }
    @keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
  `]
})
export class StatisticsWidget {
  @Input({ required: true }) state!: RequestState<StatisticsData>;
  
  days = ['M','T','W','T','F','S','S'];

  getHeatColor(val: number): string {
    if (val === 0) return 'var(--bg-tertiary)';
    if (val <= 2) return 'rgba(139,92,246,0.25)';
    if (val <= 4) return 'rgba(139,92,246,0.5)';
    return 'rgba(139,92,246,0.8)';
  }
}
