import { ActivitySessionDto } from '../../core/api/dto/api.dto';
import { MediaItem, TimelineEvent } from '../models/activity.model';
import { getActivityVisuals } from '../registry/activity-type.registry';
import { formatDuration } from '../utils/time/time.utils';

export class SessionAdapter {
  static toMediaItem(dto: ActivitySessionDto): MediaItem {
    const visuals = getActivityVisuals(dto.type, dto.source, dto.packageName);
    
    return {
      id: dto.id.toString(),
      title: dto.title || dto.packageName,
      subtitle: formatDuration(dto.durationMillis),
      imageUrl: '', // Backend doesn't provide images yet, relying on fallback
      progress: 0,
      type: (dto.type.toLowerCase() || 'task') as any,
      status: 'completed', // Defaults to completed since we pull historical sessions mostly
    };
  }

  static toTimelineEvent(dto: ActivitySessionDto): TimelineEvent {
    const visuals = getActivityVisuals(dto.type, dto.source, dto.packageName);

    return {
      id: dto.id.toString(),
      title: dto.title || dto.packageName,
      category: (dto.type.toLowerCase() || 'task') as any,
      timestamp: dto.endTime || dto.startTime,
      description: `Logged ${formatDuration(dto.durationMillis)} on ${dto.source}`,
    };
  }
}
