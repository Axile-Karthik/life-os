export interface MediaItem {
  id: string;
  title: string;
  subtitle?: string;
  type: 'movie' | 'game' | 'anime' | 'book' | 'manga' | 'music';
  imageUrl: string;
  progress?: number;
  rating?: number;
  status?: 'watching' | 'playing' | 'reading' | 'listening' | 'completed' | 'planned';
  metadata?: Record<string, string | number>;
}

export interface StatItem {
  label: string;
  value: string | number;
  change?: string;
  trend?: 'up' | 'down' | 'neutral';
  icon?: string;
  color?: string;
}

export interface TimelineEvent {
  id: string;
  title: string;
  category: 'movie' | 'game' | 'anime' | 'book' | 'manga' | 'music' | 'task';
  timestamp: string;
  icon?: string;
  description?: string;
}

export interface HeroStats {
  focusScore: number;
  hoursToday: number;
  currentStreak: number;
  totalActivities: number;
  watched: number;
  hoursThisYear: number;
  avgRating: number;
}

export interface TaskItem {
  id: string;
  title: string;
  completed: boolean;
  priority: 'low' | 'medium' | 'high';
  category: string;
  dueDate?: string;
}

export interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: string;
  unlocked: boolean;
  progress?: number;
  maxProgress?: number;
  unlockedAt?: string;
}

export interface NavItem {
  label: string;
  route: string;
  icon: string;
}

export interface WeeklyData {
  day: string;
  hours: number;
}

export interface CategoryBreakdown {
  category: string;
  value: number;
  color: string;
}

export interface ObservabilityStatus {
  syncHealth: 'healthy' | 'degraded' | 'offline';
  trackerStatus: 'active' | 'inactive';
  queueSize: number;
  activeProducers: number;
  deviceUptime: string;
  telemetryPulse: number; // requests per minute
}
