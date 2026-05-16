import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AppMetricsDto } from '../dto/api.dto';
import { ConnectivityService } from '../../services/connectivity.service';

@Injectable({ providedIn: 'root' })
export class MetricsApiService {
  private http = inject(HttpClient);
  private connectivity = inject(ConnectivityService);

  getMetrics(deviceId?: string): Observable<AppMetricsDto[]> {
    let params = new HttpParams();
    if (deviceId) params = params.set('deviceId', deviceId);

    return this.http.get<AppMetricsDto[]>(`${environment.apiUrl}/metrics`, { params }).pipe(
      catchError(err => {
        this.connectivity.setBackendReachability(false);
        return throwError(() => err);
      })
    );
  }
}
