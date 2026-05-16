import { Injectable, inject } from '@angular/core';
import { Observable, timer, fromEvent, merge, of, NEVER } from 'rxjs';
import { switchMap, exhaustMap, shareReplay, map, filter, startWith } from 'rxjs/operators';
import { SessionApiService } from '../api/services/session-api.service';
import { MetricsApiService } from '../api/services/metrics-api.service';
import { ActivitySessionDto, AppMetricsDto } from '../api/dto/api.dto';

@Injectable({ providedIn: 'root' })
export class TelemetryRefreshService {
  private sessionApi = inject(SessionApiService);
  private metricsApi = inject(MetricsApiService);

  private isVisible$ = fromEvent(document, 'visibilitychange').pipe(
    map(() => !document.hidden),
    startWith(!document.hidden)
  );

  // Reusable polling logic
  private createPoller<T>(intervalMs: number, fetchFn: () => Observable<T>): Observable<T> {
    return this.isVisible$.pipe(
      switchMap(isVisible => {
        if (!isVisible) {
          return NEVER; // Pause polling when hidden
        }
        // When visible, start polling immediately and then on interval
        return timer(0, intervalMs).pipe(
          exhaustMap(() => fetchFn()) // Prevent overlapping requests
        );
      }),
      shareReplay(1) // Multicast to all subscribers (deduplication)
    );
  }

  // Dashboard usually wants sessions every 30s
  public readonly sessions30s$ = this.createPoller<ActivitySessionDto[]>(
    30000, 
    () => this.sessionApi.getSessions()
  );

  // Observability wants metrics every 15s
  public readonly metrics15s$ = this.createPoller<AppMetricsDto[]>(
    15000, 
    () => this.metricsApi.getMetrics()
  );
}
