import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { TaskItem } from '../../../shared/models/activity.model';

@Injectable({ providedIn: 'root' })
export class TaskApiService {
  private http = inject(HttpClient);

  getTasks(): Observable<TaskItem[]> {
    return this.http.get<TaskItem[]>(`${environment.apiUrl}/tasks`).pipe(
      catchError(() => {
        // Fallback to empty array as backend endpoint is not yet implemented
        return of([]);
      })
    );
  }
}
