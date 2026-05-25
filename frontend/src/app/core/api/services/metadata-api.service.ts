import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SearchResultDto, PageResponse, ContentType } from '../dto/metadata.dto';
import { ConnectivityService } from '../../services/connectivity.service';

@Injectable({ providedIn: 'root' })
export class MetadataApiService {
  private http = inject(HttpClient);
  private connectivity = inject(ConnectivityService);

  search(query: string, type: ContentType = 'ALL', page = 0, size = 20): Observable<PageResponse<SearchResultDto>> {
    const params = new HttpParams()
      .set('query', query)
      .set('type', type)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PageResponse<SearchResultDto>>(`${environment.apiUrl}/metadata/search`, { params }).pipe(
      catchError(err => {
        this.connectivity.setBackendReachability(false);
        return throwError(() => err);
      })
    );
  }
}
