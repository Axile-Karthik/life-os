export interface ActivitySessionDto {
  id: number;
  deviceId: string;
  source: string;
  type: string;
  packageName: string;
  title: string;
  startTime: string; // ISO-8601
  endTime: string;   // ISO-8601
  durationMillis: number;
  createdAt: string; // ISO-8601
}

export interface AppMetricsDto {
  id: number;
  deviceId: string;
  timestamp: string; // ISO-8601
  heapUsedMb: number;
  heapTotalMb: number;
  heapMaxMb: number;
  lastScanDurationMs: number;
  sessionsFoundInScan: number;
  pendingSessionsCount: number;
  pendingLogsCount: number;
  batteryLevel: number;
  isCharging: boolean;
  isWifiConnected: boolean;
  createdAt: string; // ISO-8601
}

export interface HealthResponseDto {
  status: string;
  service: string;
  timestamp: string;
}
