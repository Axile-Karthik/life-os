import { Component, inject, computed } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { AppCardComponent } from '../../shared/components/app-card.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { DashboardState } from '../../core/state/dashboard.state';

@Component({
  selector: 'app-observatory',
  standalone: true,
  imports: [PageContainerComponent, AppCardComponent, SectionHeaderComponent],
  templateUrl: './observatory.component.html',
  styleUrl: './observatory.component.css',
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
