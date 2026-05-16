import {
  MediaItem,
  StatItem,
  TimelineEvent,
  HeroStats,
  TaskItem,
  Achievement,
  WeeklyData,
  CategoryBreakdown,
  ObservabilityStatus,
} from '../models/activity.model';

/* ===== HERO STATS ===== */
export const MOCK_HERO_STATS: HeroStats = {
  focusScore: 82,
  hoursToday: 4.5,
  currentStreak: 17,
  totalActivities: 1247,
  watched: 127,
  hoursThisYear: 312,
  avgRating: 4.2,
};

/* ===== RECENT MEDIA ===== */
export const MOCK_RECENT_MEDIA: MediaItem[] = [
  {
    id: '1',
    title: 'Interstellar',
    subtitle: '2014 · Sci-Fi',
    type: 'movie',
    imageUrl: 'https://image.tmdb.org/t/p/w300/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg',
    rating: 4.7,
    status: 'completed',
  },
  {
    id: '2',
    title: 'Elden Ring',
    subtitle: 'FromSoftware',
    type: 'game',
    imageUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.webp',
    progress: 68,
    status: 'playing',
    metadata: { hoursPlayed: '312h' },
  },
  {
    id: '3',
    title: 'Dune: Part Two',
    subtitle: '2024 · Sci-Fi',
    type: 'movie',
    imageUrl: 'https://image.tmdb.org/t/p/w300/8b8R8l88Qje9dn9OE8PY05Nez7p.jpg',
    rating: 4.5,
    status: 'completed',
  },
  {
    id: '4',
    title: 'Chainsaw Man',
    subtitle: 'MAPPA',
    type: 'anime',
    imageUrl: 'https://cdn.myanimelist.net/images/anime/1806/126216l.jpg',
    progress: 85,
    status: 'watching',
  },
  {
    id: '5',
    title: 'The Dark Knight',
    subtitle: '2008 · Action',
    type: 'movie',
    imageUrl: 'https://image.tmdb.org/t/p/w300/qJ2tW6WMUDux911BTUgMe1nUBp8.jpg',
    rating: 5.0,
    status: 'completed',
  },
  {
    id: '6',
    title: 'Baldur\'s Gate 3',
    subtitle: 'Larian Studios',
    type: 'game',
    imageUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co670h.webp',
    progress: 45,
    status: 'playing',
  },
];

/* ===== WATCHLIST ===== */
export const MOCK_WATCHLIST: MediaItem[] = [
  {
    id: 'w1',
    title: 'The Witcher',
    type: 'game',
    imageUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1wyy.webp',
    status: 'planned',
  },
  {
    id: 'w2',
    title: 'Attack on Titan',
    subtitle: 'S4',
    type: 'anime',
    imageUrl: 'https://cdn.myanimelist.net/images/anime/1000/110531l.jpg',
    status: 'planned',
  },
  {
    id: 'w3',
    title: 'Solo Leveling',
    type: 'anime',
    imageUrl: 'https://cdn.myanimelist.net/images/anime/1766/138408l.jpg',
    status: 'planned',
  },
];

/* ===== GAMES ===== */
export const MOCK_GAMES: MediaItem[] = [
  {
    id: 'g1',
    title: 'Elden Ring',
    subtitle: 'FromSoftware',
    type: 'game',
    imageUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.webp',
    progress: 68,
    status: 'playing',
    metadata: { hoursPlayed: '312h' },
  },
  {
    id: 'g2',
    title: 'God of War',
    subtitle: 'Santa Monica',
    type: 'game',
    imageUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co1tmu.webp',
    progress: 100,
    status: 'completed',
  },
  {
    id: 'g3',
    title: 'Hollow Knight',
    subtitle: 'Team Cherry',
    type: 'game',
    imageUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co6bx3.webp',
    progress: 55,
    status: 'playing',
  },
  {
    id: 'g4',
    title: 'Hades',
    subtitle: 'Supergiant',
    type: 'game',
    imageUrl: 'https://images.igdb.com/igdb/image/upload/t_cover_big/co2q89.webp',
    progress: 100,
    status: 'completed',
  },
];

/* ===== BOOKS ===== */
export const MOCK_BOOKS: MediaItem[] = [
  {
    id: 'b1',
    title: 'Sapiens',
    subtitle: 'A Brief History of Humankind',
    type: 'book',
    imageUrl: 'https://covers.openlibrary.org/b/id/8479576-L.jpg',
    progress: 72,
    status: 'reading',
  },
  {
    id: 'b2',
    title: 'Atomic Habits',
    subtitle: 'James Clear',
    type: 'book',
    imageUrl: 'https://covers.openlibrary.org/b/id/12547283-L.jpg',
    progress: 100,
    status: 'completed',
    rating: 4.5,
  },
  {
    id: 'b3',
    title: 'Kafka on the Shore',
    subtitle: 'Haruki Murakami',
    type: 'book',
    imageUrl: 'https://covers.openlibrary.org/b/id/8231856-L.jpg',
    progress: 30,
    status: 'reading',
  },
];

/* ===== MANGA ===== */
export const MOCK_MANGA: MediaItem[] = [
  {
    id: 'm1',
    title: 'One Piece',
    subtitle: 'Eiichiro Oda',
    type: 'manga',
    imageUrl: 'https://cdn.myanimelist.net/images/manga/2/253146l.jpg',
    progress: 75,
    status: 'reading',
    metadata: { chapter: '1102' },
  },
  {
    id: 'm2',
    title: 'Berserk',
    subtitle: 'Kentaro Miura',
    type: 'manga',
    imageUrl: 'https://cdn.myanimelist.net/images/manga/1/157897l.jpg',
    progress: 90,
    status: 'reading',
  },
  {
    id: 'm3',
    title: 'Vagabond',
    subtitle: 'Takehiko Inoue',
    type: 'manga',
    imageUrl: 'https://cdn.myanimelist.net/images/manga/2/259525l.jpg',
    progress: 60,
    status: 'reading',
  },
];

/* ===== ANIME ===== */
export const MOCK_ANIME: MediaItem[] = [
  {
    id: 'a1',
    title: 'Attack on Titan',
    subtitle: 'Season 4 Part 3',
    type: 'anime',
    imageUrl: 'https://cdn.myanimelist.net/images/anime/1000/110531l.jpg',
    progress: 45,
    status: 'watching',
    metadata: { episode: '22' },
  },
  {
    id: 'a2',
    title: 'Demon Slayer',
    subtitle: 'Swordsmith Village Arc',
    type: 'anime',
    imageUrl: 'https://cdn.myanimelist.net/images/anime/1765/135099l.jpg',
    progress: 100,
    status: 'completed',
    rating: 4.3,
  },
  {
    id: 'a3',
    title: 'Jujutsu Kaisen',
    subtitle: 'Season 2',
    type: 'anime',
    imageUrl: 'https://cdn.myanimelist.net/images/anime/1792/138022l.jpg',
    progress: 88,
    status: 'watching',
  },
];

/* ===== MUSIC ===== */
export const MOCK_MUSIC: MediaItem[] = [
  {
    id: 'mu1',
    title: 'Time',
    subtitle: 'Hans Zimmer — Inception OST',
    type: 'music',
    imageUrl: 'https://i.scdn.co/image/ab67616d0000b273e14f11f796cef9f9a82691a7',
    status: 'listening',
  },
  {
    id: 'mu2',
    title: 'Dream Is Collapsing',
    subtitle: 'Hans Zimmer',
    type: 'music',
    imageUrl: 'https://i.scdn.co/image/ab67616d0000b273e14f11f796cef9f9a82691a7',
  },
  {
    id: 'mu3',
    title: 'Cornfield Chase',
    subtitle: 'Hans Zimmer — Interstellar',
    type: 'music',
    imageUrl: 'https://i.scdn.co/image/ab67616d0000b2735a52b93f03365ab32c498e38',
  },
];

/* ===== STATS OVERVIEW ===== */
export const MOCK_STATS: StatItem[] = [
  { label: 'Watched', value: 127, change: '+15 this year', trend: 'up', color: 'var(--accent-primary)' },
  { label: 'Hours', value: '258h', change: '+48h this year', trend: 'up', color: 'var(--accent-secondary)' },
  { label: 'Hours This Year', value: '312h', change: '+8h this week', trend: 'up', color: 'var(--accent-cyan)' },
  { label: 'Rating', value: '4.2', change: 'avg rating', trend: 'neutral', color: 'var(--accent-warm)' },
];

/* ===== STATS OVERVIEW CARDS ===== */
export const MOCK_OVERVIEW_STATS: StatItem[] = [
  { label: 'Movies', value: 127, color: 'var(--accent-primary)' },
  { label: 'TV', value: 45, color: 'var(--accent-secondary)' },
  { label: 'Books', value: 28, color: 'var(--accent-emerald)' },
  { label: 'Manga', value: 36, color: 'var(--accent-warm)' },
];

/* ===== TIMELINE EVENTS ===== */
export const MOCK_TIMELINE: TimelineEvent[] = [
  {
    id: 't1',
    title: 'Watched Interstellar',
    category: 'movie',
    timestamp: '2 hours ago',
    description: 'Rewatched for the 3rd time',
  },
  {
    id: 't2',
    title: 'Played Elden Ring',
    category: 'game',
    timestamp: '5 hours ago',
    description: 'Defeated Malenia',
  },
  {
    id: 't3',
    title: 'Read Sapiens',
    category: 'book',
    timestamp: '1 day ago',
    description: 'Chapters 12-15',
  },
  {
    id: 't4',
    title: 'Watched Attack on Titan',
    category: 'anime',
    timestamp: '1 day ago',
    description: 'Episode 22',
  },
  {
    id: 't5',
    title: 'Listened to Time (Inception OST)',
    category: 'music',
    timestamp: '2 days ago',
  },
  {
    id: 't6',
    title: 'Read One Piece Ch. 1102',
    category: 'manga',
    timestamp: '2 days ago',
  },
];

/* ===== WEEKLY DATA ===== */
export const MOCK_WEEKLY_DATA: WeeklyData[] = [
  { day: 'Mon', hours: 3.5 },
  { day: 'Tue', hours: 2.0 },
  { day: 'Wed', hours: 5.0 },
  { day: 'Thu', hours: 1.5 },
  { day: 'Fri', hours: 4.0 },
  { day: 'Sat', hours: 6.5 },
  { day: 'Sun', hours: 4.5 },
];

/* ===== CATEGORY BREAKDOWN ===== */
export const MOCK_CATEGORY_BREAKDOWN: CategoryBreakdown[] = [
  { category: 'Coding', value: 35, color: 'var(--accent-primary)' },
  { category: 'Games', value: 25, color: 'var(--accent-secondary)' },
  { category: 'Movies', value: 15, color: 'var(--accent-warm)' },
  { category: 'Music', value: 12, color: 'var(--accent-cyan)' },
  { category: 'Reading', value: 8, color: 'var(--accent-emerald)' },
  { category: 'Others', value: 5, color: 'var(--text-tertiary)' },
];

/* ===== HEATMAP DATA (5 weeks x 7 days) ===== */
export const MOCK_HEATMAP_DATA: number[][] = [
  [2, 3, 0, 4, 1, 5, 3],
  [1, 4, 2, 0, 3, 6, 2],
  [3, 1, 5, 2, 4, 3, 1],
  [0, 2, 3, 5, 1, 4, 6],
  [4, 3, 1, 2, 5, 3, 2],
];

/* ===== TASKS ===== */
export const MOCK_TASKS: TaskItem[] = [
  { id: 'LIFE-142', title: 'Metadata sync', completed: false, priority: 'high', category: 'Backend', dueDate: 'Today' },
  { id: 'LIFE-143', title: 'Timeline replay', completed: false, priority: 'medium', category: 'Frontend', dueDate: 'This week' },
  { id: 'LIFE-144', title: 'Observatory polish', completed: false, priority: 'low', category: 'Frontend' },
  { id: 'LIFE-145', title: 'Session healing logic', completed: false, priority: 'medium', category: 'Backend', dueDate: 'Tomorrow' },
  { id: 'LIFE-139', title: 'Tailscale VPN setup', completed: true, priority: 'high', category: 'Infrastructure' },
  { id: 'LIFE-140', title: 'Grafana dashboards', completed: true, priority: 'medium', category: 'Observability' },
];

/* ===== ACHIEVEMENTS ===== */
export const MOCK_ACHIEVEMENTS: Achievement[] = [
  {
    id: 'ach1',
    title: 'Century Coder',
    description: '100+ hours of coding',
    icon: '💻',
    unlocked: true,
    unlockedAt: '2 months ago',
  },
  {
    id: 'ach2',
    title: 'Gaming Addict',
    description: '500+ hours of gaming',
    icon: '🎮',
    unlocked: true,
    unlockedAt: '1 month ago',
  },
  {
    id: 'ach3',
    title: 'Movie Buff',
    description: 'Watched 100 movies',
    icon: '🎬',
    unlocked: true,
    progress: 127,
    maxProgress: 100,
  },
  {
    id: 'ach4',
    title: 'Bookworm',
    description: 'Read 50 books',
    icon: '📚',
    unlocked: false,
    progress: 28,
    maxProgress: 50,
  },
  {
    id: 'ach5',
    title: 'Anime Master',
    description: 'Watch 200 anime series',
    icon: '⛩️',
    unlocked: false,
    progress: 85,
    maxProgress: 200,
  },
  {
    id: 'ach6',
    title: 'Manga Explorer',
    description: 'Read 100 manga',
    icon: '📖',
    unlocked: false,
    progress: 36,
    maxProgress: 100,
  },
];

/* ===== FOCUS ANALYTICS ===== */
export const MOCK_FOCUS_ANALYTICS = {
  focusTime: '1h 42m',
  sessions: 28,
  avgFocus: '39m',
  focusScore: 127,
};

/* ===== RATING DISTRIBUTION ===== */
export const MOCK_RATING_DISTRIBUTION = [
  { rating: 5, count: 32 },
  { rating: 4, count: 48 },
  { rating: 3, count: 25 },
  { rating: 2, count: 12 },
  { rating: 1, count: 5 },
];

/* ===== OBSERVABILITY ===== */
export const MOCK_OBSERVABILITY_DATA: ObservabilityStatus = {
  syncHealth: 'healthy',
  trackerStatus: 'active',
  queueSize: 14,
  activeProducers: 3,
  deviceUptime: '14d 6h 22m',
  telemetryPulse: 142,
};
