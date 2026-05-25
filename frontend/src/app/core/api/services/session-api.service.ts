import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { ActivitySessionDto } from '../dto/api.dto';
import { ConnectivityService } from '../../services/connectivity.service';

@Injectable({ providedIn: 'root' })
export class SessionApiService {
  private http = inject(HttpClient);
  private connectivity = inject(ConnectivityService);

  getSessions(deviceId?: string, type?: string, limit: number = 50): Observable<ActivitySessionDto[]> {
    let params = new HttpParams();
    if (deviceId) params = params.set('deviceId', deviceId);
    if (type) params = params.set('type', type);
    // limit is not explicitly supported by backend in the snippet, but we can pass it if supported later.

    return this.http.get<ActivitySessionDto[]>(`${environment.apiUrl}/sessions`, { params }).pipe(
      catchError(err => {
        this.connectivity.setBackendReachability(false);
        return throwError(() => err);
      })
    );
  }

  getDailyStats(deviceId?: string, startDate?: string, endDate?: string): Observable<any[]> {
    let params = new HttpParams();
    if (deviceId) params = params.set('deviceId', deviceId);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    return this.http.get<any[]>(`${environment.apiUrl}/sessions/stats/daily`, { params }).pipe(
      catchError(err => {
        this.connectivity.setBackendReachability(false);
        return throwError(() => err);
      })
    );
  }

  getWeeklyStats(deviceId?: string, startDate?: string, endDate?: string): Observable<any[]> {
    let params = new HttpParams();
    if (deviceId) params = params.set('deviceId', deviceId);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    return this.http.get<any[]>(`${environment.apiUrl}/sessions/stats/weekly`, { params }).pipe(
      catchError(err => {
        this.connectivity.setBackendReachability(false);
        return throwError(() => err);
      })
    );
  }

  getPerGameStats(deviceId?: string, type?: string, startDate?: string, endDate?: string): Observable<any[]> {
    let params = new HttpParams();
    if (deviceId) params = params.set('deviceId', deviceId);
    if (type) params = params.set('type', type);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);

    return this.http.get<any[]>(`${environment.apiUrl}/sessions/stats/pergame`, { params }).pipe(
      catchError(err => {
        this.connectivity.setBackendReachability(false);
        return throwError(() => err);
      })
    );
  }
}
