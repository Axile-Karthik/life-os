import { ContentType } from '../dto/metadata.dto';
import { LibraryStatus } from './library-status';

export interface LibraryItemDto {
  id: string; // UUID
  metadataId: string;
  title: string;
  imageUrl: string;
  contentType: ContentType;
  status: LibraryStatus;
  progressPercent: number | null;
  rating: number | null;
  favorite: boolean;
  startedAt: string | null;
  completedAt: string | null;
  lastActivityAt: string | null;
}
