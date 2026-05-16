import { Injectable, computed, signal, inject, DestroyRef } from '@angular/core';
import { TelemetryRefreshService } from '../services/telemetry-refresh.service';
import { ActivitySessionDto, AppMetricsDto } from '../api/dto/api.dto';
import { RequestState, initialRequestState, loadedRequestState, errorRequestState, loadingStaleRequestState } from '../../shared/models/request-state.model';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class DashboardState {
  private refresh = inject(TelemetryRefreshService);
  private destroyRef = inject(DestroyRef);

  // Private raw state
  private _sessions = signal<RequestState<ActivitySessionDto[]>>(initialRequestState());
  private _metrics = signal<RequestState<AppMetricsDto[]>>(initialRequestState());

  // Public exposed streams
  public readonly sessions = this._sessions.asReadonly();
  public readonly metrics = this._metrics.asReadonly();

  constructor() {
    this.initSessionStream();
    this.initMetricsStream();
  }

  private initSessionStream() {
    // When a refresh triggers, if we already have data, we transition to "loading but stale"
    // However, the poller itself manages the stream. We just subscribe to the emission.
    this.refresh.sessions30s$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this._sessions.set(loadedRequestState(data)),
      error: (err) => this._sessions.set(errorRequestState(err.message || 'Failed to fetch sessions', this._sessions().data))
    });
  }

  private initMetricsStream() {
    this.refresh.metrics15s$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => this._metrics.set(loadedRequestState(data)),
      error: (err) => this._metrics.set(errorRequestState(err.message || 'Failed to fetch metrics', this._metrics().data))
    });
  }

  // Derived Analytics (Frontend-derived as requested)
  public readonly heroStats = computed(() => {
    const s = this._sessions().data;
    if (!s || s.length === 0) return null;

    // Simple placeholder algorithm: 
    // Calculate total duration in last 24h as "focus score"
    const now = Date.now();
    const recent = s.filter(x => now - new Date(x.endTime).getTime() < 86400000);
    const totalMillis = recent.reduce((acc, curr) => acc + curr.durationMillis, 0);
    
    // Fake score: cap at 100 based on some arbitrary max hours (e.g., 8 hours = 100)
    const hours = totalMillis / (1000 * 3600);
    const focusScore = Math.min(100, Math.round((hours / 8) * 100));

    return {
      focusScore,
      currentStreak: 12, // Mocked for now, requires deeper historical analysis
      totalHours: hours.toFixed(1)
    };
  });
}
