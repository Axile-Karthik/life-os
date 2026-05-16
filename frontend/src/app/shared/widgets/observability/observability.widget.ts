import { Component, Input } from '@angular/core';
import { ObservabilityStatus } from '../../models/activity.model';
import { WidgetContainerComponent } from '../../components/widget-container.component';
import { RequestState } from '../../models/request-state.model';

@Component({
  selector: 'app-observability',
  standalone: true,
  imports: [WidgetContainerComponent],
  template: `
    <app-widget-container title="System Health" icon="⚡">
      @if (state.loading && !state.data) {
        <div class="observability-grid">
          <div class="obs-card glass-sm skeleton-glow" style="height: 80px;"></div>
          <div class="obs-card glass-sm skeleton-glow" style="height: 80px;"></div>
          <div class="obs-card glass-sm skeleton-glow" style="height: 80px;"></div>
          <div class="obs-card glass-sm skeleton-glow" style="height: 80px;"></div>
        </div>
      } @else if (state.data) {
        <div class="observability-grid">
          <div class="obs-card glass-sm hover-lift">
            <div class="obs-card__header">
              <span class="obs-card__title">Sync Status</span>
              <span class="status-dot" [class]="'status-dot--' + state.data.syncHealth"></span>
            </div>
            <div class="obs-card__val">
              {{ state.data.syncHealth }}
              @if (state.isStale) { <span class="stale-dot" title="Delayed"></span> }
            </div>
            <div class="obs-chart-placeholder">
              <div class="pulse-line animate-gradient-shift"></div>
            </div>
          </div>

          <div class="obs-card glass-sm hover-lift">
            <div class="obs-card__header">
              <span class="obs-card__title">Tracker</span>
              <span class="status-dot" [class.status-dot--active]="state.data.trackerStatus === 'active'"></span>
            </div>
            <div class="obs-card__val">{{ state.data.trackerStatus }}</div>
            <div class="obs-card__sub">{{ state.data.deviceUptime || 'Unknown' }} uptime</div>
          </div>

          <div class="obs-card glass-sm hover-lift">
            <div class="obs-card__header">
              <span class="obs-card__title">Queue Size</span>
            </div>
            <div class="obs-card__val">{{ state.data.queueSize }}</div>
            <div class="obs-card__sub">Pending items</div>
          </div>

          <div class="obs-card glass-sm hover-lift">
            <div class="obs-card__header">
              <span class="obs-card__title">Telemetry</span>
            </div>
            <div class="obs-card__val">{{ state.data.telemetryPulse }} <span style="font-size:12px; color:var(--text-tertiary)">req/min</span></div>
            <div class="obs-card__sub">Active telemetry</div>
          </div>
        </div>
      } @else if (state.error) {
        <div class="empty-state" style="color: var(--error);">
          System health telemetry offline.
        </div>
      }
    </app-widget-container>
  `,
  styles: [`
    .observability-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 12px;
    }
    .obs-card {
      padding: 14px;
      display: flex;
      flex-direction: column;
    }
    .obs-card__header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }
    .obs-card__title {
      font-size: 11px;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      font-weight: 600;
    }
    .obs-card__val {
      font-size: 20px;
      font-weight: 700;
      color: var(--text-primary);
      text-transform: capitalize;
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .obs-card__sub {
      font-size: 10px;
      color: var(--text-tertiary);
      margin-top: 4px;
    }
    .status-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--bg-tertiary);
    }
    .stale-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--warning); display: inline-block; }
    .status-dot--healthy, .status-dot--active {
      background: var(--success);
      box-shadow: 0 0 8px var(--success);
      animation: pulse 2s infinite;
    }
    .status-dot--degraded {
      background: var(--warning);
      box-shadow: 0 0 8px var(--warning);
    }
    .status-dot--offline {
      background: var(--error);
      box-shadow: 0 0 8px var(--error);
    }
    .obs-chart-placeholder {
      margin-top: 12px;
      height: 24px;
      display: flex;
      align-items: flex-end;
    }
    .pulse-line {
      height: 2px;
      width: 100%;
      background: linear-gradient(90deg, transparent, var(--success), transparent);
      background-size: 200% 100%;
    }
    .empty-state { padding: 20px; text-align: center; color: var(--text-secondary); font-size: 12px; border: 1px dashed var(--card-border); border-radius: var(--radius-md); }
    
    /* Cinematic Skeletons */
    .skeleton-glow { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); animation: pulse 2s infinite ease-in-out; }
    @keyframes pulse {
      0% { opacity: 0.5; }
      50% { opacity: 1; }
      100% { opacity: 0.5; }
    }
  `]
})
export class ObservabilityWidget {
  @Input({ required: true }) state!: RequestState<ObservabilityStatus>;
}
