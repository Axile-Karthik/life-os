import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { Achievement } from '../../../shared/models/activity.model';

@Injectable({ providedIn: 'root' })
export class AchievementsApiService {
  private http = inject(HttpClient);

  getAchievements(): Observable<Achievement[]> {
    return this.http.get<Achievement[]>(`${environment.apiUrl}/achievements`).pipe(
      catchError(() => {
        // Fallback to empty array as backend endpoint is not yet implemented
        return of([]);
      })
    );
  }
}
