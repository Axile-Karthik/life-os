export type ContentType = 'GAME' | 'MOVIE' | 'SERIES' | 'ANIME' | 'BOOK' | 'MUSIC' | 'MANGA' | 'ALL';

export interface SearchResultDto {
  id: string;
  title: string;
  imageUrl: string;
  releaseDate: string;
  contentType: ContentType;
  externalSource: string;
  externalId: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
