import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { LibraryItemDto } from '../models/library-item.dto';
import { PageResponse } from '../dto/metadata.dto';

@Injectable({ providedIn: 'root' })
export class LibraryApiService {
  private http = inject(HttpClient);

  getLibraryItems(status?: string, type?: string, sort?: string, page = 0, size = 50): Observable<PageResponse<LibraryItemDto>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status && status !== 'All') {
      params = params.set('status', status.toUpperCase());
    }
    if (type && type !== 'ALL' && type !== 'All') {
      params = params.set('type', type.toUpperCase());
    }
    if (sort) {
      params = params.set('sort', sort);
    }

    return this.http.get<PageResponse<LibraryItemDto>>(`${environment.apiUrl}/library`, { params });
  }

  addToLibrary(payload: {
    externalSource: string;
    externalId: string;
    status: string;
    title?: string;
    imageUrl?: string;
    contentType?: string;
    releaseDate?: string;
  }): Observable<LibraryItemDto> {
    return this.http.post<LibraryItemDto>(`${environment.apiUrl}/library/items`, payload);
  }
}
