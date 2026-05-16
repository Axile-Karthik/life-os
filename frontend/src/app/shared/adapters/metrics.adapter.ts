import { AppMetricsDto } from '../../core/api/dto/api.dto';
import { ObservabilityStatus } from '../models/activity.model';

export class MetricsAdapter {
  static toObservabilityStatus(dto: AppMetricsDto | null): ObservabilityStatus {
    if (!dto) {
      return {
        syncHealth: 'offline',
        trackerStatus: 'inactive',
        queueSize: 0,
        deviceUptime: '0m',
        telemetryPulse: 0,
        activeProducers: 0
      };
    }

    // Determine sync health based on pending queues or timestamp freshness
    // For now, if there's recent metrics, it's healthy
    const isStale = (Date.now() - new Date(dto.timestamp).getTime()) > 5 * 60000; // > 5 mins
    const syncHealth = isStale ? 'degraded' : 'healthy';

    return {
      syncHealth: syncHealth,
      trackerStatus: dto.batteryLevel > 0 ? 'active' : 'inactive',
      queueSize: dto.pendingSessionsCount + dto.pendingLogsCount,
      deviceUptime: 'Active', // Needs derivation if backend doesn't provide uptime natively
      telemetryPulse: dto.sessionsFoundInScan,
      activeProducers: 1 // Default to 1 producer (Android Tracker) for now
    };
  }
}
