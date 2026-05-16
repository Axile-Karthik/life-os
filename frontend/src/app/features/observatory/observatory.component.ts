import { Component, inject, computed } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { AppCardComponent } from '../../shared/components/app-card.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { DashboardState } from '../../core/state/dashboard.state';

@Component({
  selector: 'app-observatory',
  standalone: true,
  imports: [PageContainerComponent, AppCardComponent, SectionHeaderComponent],
  template: `
    <app-page-container>
      <app-section-header title="Observatory" subtitle="System health & tracker status" />
      
      @if (metricsState().loading && !metricsState().data) {
        <div class="obs-grid">
          @for (i of [1,2,3,4]; track i) {
            <app-card [hoverable]="false">
              <div class="obs-metric">
                <div class="skeleton-dot skeleton-glow" style="width: 28px; height: 28px;"></div>
                <div class="obs-metric__info">
                  <div class="skeleton-shimmer" style="width: 60px; height: 12px; border-radius: 4px; margin-bottom: 6px;"></div>
                  <div class="skeleton-shimmer" style="width: 80px; height: 16px; border-radius: 4px;"></div>
                </div>
              </div>
            </app-card>
          }
        </div>
      } @else if (metricsState().data) {
        <div class="obs-grid">
          @for (metric of displayMetrics(); track metric.label) {
            <app-card [hoverable]="true">
              <div class="obs-metric">
                <div class="obs-metric__icon" [style.color]="metric.color">{{ metric.icon }}</div>
                <div class="obs-metric__info">
                  <div class="obs-metric__label">{{ metric.label }}</div>
                  <div class="obs-metric__value" [style.color]="metric.color">{{ metric.value }}</div>
                </div>
                <div class="obs-metric__status" [style.color]="metric.statusColor">{{ metric.status }}</div>
              </div>
            </app-card>
          }
        </div>
      } @else if (metricsState().error) {
        <div class="empty-state error-state">Failed to load system metrics.</div>
      }
      
      <app-section-header title="Activity Pulse" subtitle="Real-time tracker activity" />
      <app-card>
        <div class="obs-pulse">
          @for (bar of pulseData; track $index) {
            <div class="obs-pulse__bar" [style.height.%]="bar" [style.background]="'var(--accent-primary)'"></div>
          }
        </div>
      </app-card>
      
      <app-section-header title="Connections" />
      <div class="obs-connections">
        @for (conn of connections(); track conn.name) {
          <app-card [hoverable]="true" padding="compact">
            <div class="obs-conn">
              <span class="obs-conn__dot" [style.background]="conn.connected ? 'var(--accent-emerald)' : 'var(--accent-rose)'"></span>
              <span class="obs-conn__name">{{ conn.name }}</span>
              <span class="obs-conn__status">{{ conn.connected ? 'Connected' : 'Offline' }}</span>
            </div>
          </app-card>
        }
      </div>
    </app-page-container>
  `,
  styles: [`
    .obs-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .obs-metric { display: flex; align-items: center; gap: 12px; }
    .obs-metric__icon { font-size: 28px; }
    .obs-metric__info { flex: 1; }
    .obs-metric__label { font-size: 12px; color: var(--text-secondary); }
    .obs-metric__value { font-size: 20px; font-weight: 700; }
    .obs-metric__status { font-size: 11px; font-weight: 600; }
    .obs-pulse { display: flex; align-items: flex-end; gap: 3px; height: 120px; padding: 16px 0; }
    .obs-pulse__bar { flex: 1; border-radius: 2px 2px 0 0; opacity: 0.7; min-height: 4px; transition: height 0.3s ease; }
    .obs-connections { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 12px; }
    .obs-conn { display: flex; align-items: center; gap: 10px; }
    .obs-conn__dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
    .obs-conn__name { font-size: 14px; font-weight: 500; color: var(--text-primary); flex: 1; }
    .obs-conn__status { font-size: 12px; color: var(--text-tertiary); }
    .empty-state { padding: 40px 20px; text-align: center; color: var(--text-secondary); font-size: 14px; border: 1px dashed var(--card-border); border-radius: var(--radius-md); }
    .error-state { color: var(--error); border-color: rgba(239, 68, 68, 0.3); }

    /* Skeletons */
    .skeleton-dot { border-radius: 50%; }
    .skeleton-glow { background: rgba(255,255,255,0.02); animation: pulse 2s infinite ease-in-out; }
    .skeleton-shimmer { background: linear-gradient(90deg, rgba(255,255,255,0.02) 25%, rgba(255,255,255,0.05) 50%, rgba(255,255,255,0.02) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite linear; }
    @keyframes pulse { 0% { opacity: 0.5; } 50% { opacity: 0.8; } 100% { opacity: 0.5; } }
    @keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
  `],
})
export class ObservatoryComponent {
  private dashboard = inject(DashboardState);
  
  metricsState = this.dashboard.metrics;

  displayMetrics = computed(() => {
    const data = this.metricsState().data;
    if (!data || data.length === 0) return [];
    const latest = data[0]; // assuming index 0 is latest
    
    const queueSize = latest.pendingSessionsCount + latest.pendingLogsCount;
    const memoryPct = latest.heapMaxMb > 0 ? (latest.heapUsedMb / latest.heapMaxMb) * 100 : 0;

    return [
      { label: 'System Uptime', value: 'Active', icon: '🟢', color: 'var(--accent-emerald)', status: 'Healthy', statusColor: 'var(--accent-emerald)' },
      { label: 'Sync Queue', value: queueSize.toString(), icon: '🔄', color: 'var(--accent-secondary)', status: queueSize > 0 ? 'Processing' : 'Idle', statusColor: queueSize > 0 ? 'var(--warning)' : 'var(--accent-emerald)' },
      { label: 'Memory Usage', value: memoryPct.toFixed(1) + '%', icon: '🧠', color: 'var(--accent-primary)', status: 'Stable', statusColor: 'var(--accent-primary)' },
      { label: 'Active Producers', value: '1', icon: '📡', color: 'var(--accent-cyan)', status: 'Online', statusColor: 'var(--accent-cyan)' },
    ];
  });

  connections = computed(() => {
    const data = this.metricsState().data;
    const isOnline = !!data && !this.metricsState().error;
    
    return [
      { name: 'Android Tracker', connected: isOnline },
      { name: 'Core API Server', connected: isOnline },
      { name: 'Media Scanner', connected: false },
      { name: 'Observability Exporter', connected: isOnline },
    ];
  });

  pulseData = Array.from({ length: 40 }, () => Math.floor(Math.random() * 80) + 20);
}
