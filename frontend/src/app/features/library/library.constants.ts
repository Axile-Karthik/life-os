// Helper to resolve high-fidelity Unsplash cover images based on title
export function getMediaImage(title: string, type: string): string {
  const t = title.toLowerCase();

  // Dynamic high-fidelity cover image mapping for actual telemetry data
  if (t.includes('shadow hunter')) return 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=400&q=80';
  if (t.includes('free fire')) return 'https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=400&q=80';
  if (t.includes('limbo')) return 'https://images.unsplash.com/photo-1508739773434-c26b3d09e071?auto=format&fit=crop&w=400&q=80';
  if (t.includes('blasphemous')) return 'https://images.unsplash.com/photo-1519074069444-1ba4e6663104?auto=format&fit=crop&w=400&q=80';

  // Mockup fallback covers
  if (t.includes('elden')) return 'https://images.unsplash.com/photo-1655821888788-6107699e173b?auto=format&fit=crop&w=400&q=80';
  if (t.includes('baldur')) return 'https://images.unsplash.com/photo-1519074069444-1ba4e6663104?auto=format&fit=crop&w=400&q=80';
  if (t.includes('dune')) return 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=400&q=80';
  if (t.includes('chainsaw')) return 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=400&q=80';
  if (t.includes('interstellar')) return 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=400&q=80';
  if (t.includes('dark knight') || t.includes('batman')) return 'https://images.unsplash.com/photo-1509248961158-e54f6934749c?auto=format&fit=crop&w=400&q=80';
  if (t.includes('god of war')) return 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=400&q=80';
  if (t.includes('one piece')) return 'https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=400&q=80';
  if (t.includes('berserk')) return 'https://images.unsplash.com/photo-1618336753974-aae8e04506aa?auto=format&fit=crop&w=400&q=80';
  if (t.includes('atomic')) return 'https://images.unsplash.com/photo-1484480974693-2ca0a72f3a4b?auto=format&fit=crop&w=400&q=80';
  if (t.includes('hades')) return 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=400&q=80';
  if (t.includes('hollow')) return 'https://images.unsplash.com/photo-1508739773434-c26b3d09e071?auto=format&fit=crop&w=400&q=80';
  if (t.includes('sapiens')) return 'https://images.unsplash.com/photo-1447069387593-a5de0862481e?auto=format&fit=crop&w=400&q=80';
  if (t.includes('vagabond')) return 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=400&q=80';
  if (t.includes('jujutsu')) return 'https://images.unsplash.com/photo-1501854140801-50d01698950b?auto=format&fit=crop&w=400&q=80';
  if (t.includes('demon')) return 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=400&q=80';

  if (type === 'game') return 'https://images.unsplash.com/photo-1538481199705-c710c4e965fc?auto=format&fit=crop&w=400&q=80';
  if (type === 'movie') return 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=400&q=80';
  if (type === 'anime') return 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=400&q=80';
  if (type === 'book' || type === 'manga') return 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=400&q=80';
  if (type === 'music') return 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=400&q=80';
  return 'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=400&q=80';
}

export function getStatusFromType(type: string): string {
  if (type === 'game') return 'playing';
  if (type === 'book' || type === 'manga') return 'reading';
  if (type === 'anime' || type === 'movie') return 'watching';
  return 'listening';
}

export function getRelativeTimeString(timeMs: number): string {
  const diff = Date.now() - timeMs;
  const mins = Math.floor(diff / 60000);
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days === 1) return 'Yesterday';
  return `${days} days ago`;
}

// Exact mockup database items to merge with real telemetry logs
export const MOCK_CONTINUE_PLAYING = [
  {
    id: 'elden-ring',
    title: 'Elden Ring',
    subtitle: 'FromSoftware',
    imageUrl: 'https://images.unsplash.com/photo-1655821888788-6107699e173b?auto=format&fit=crop&w=400&q=80',
    progress: 65,
    type: 'game',
    status: 'playing',
    latestTime: Date.now() - 3600000 * 2
  },
  {
    id: 'baldurs-gate-3',
    title: "Baldur's Gate 3",
    subtitle: 'Larian Studios',
    imageUrl: 'https://images.unsplash.com/photo-1519074069444-1ba4e6663104?auto=format&fit=crop&w=400&q=80',
    progress: 40,
    type: 'game',
    status: 'playing',
    latestTime: Date.now() - 3600000 * 5
  },
  {
    id: 'dune-part-two',
    title: 'Dune: Part Two',
    subtitle: '2024 • Sci-Fi',
    imageUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=400&q=80',
    progress: 72,
    type: 'movie',
    status: 'watching',
    latestTime: Date.now() - 3600000 * 24
  },
  {
    id: 'chainsaw-man',
    title: 'Chainsaw Man',
    subtitle: 'MAPPA',
    imageUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=400&q=80',
    progress: 28,
    type: 'anime',
    status: 'watching',
    latestTime: Date.now() - 3600000 * 30
  }
];

export const MOCK_LIBRARY = [
  {
    id: 'interstellar',
    title: 'Interstellar',
    subtitle: '2014 • Sci-Fi',
    imageUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=400&q=80',
    rating: 4.7,
    status: 'watching',
    type: 'movie',
    latestTime: Date.now() - 3600000 * 10
  },
  {
    id: 'the-dark-knight',
    title: 'The Dark Knight',
    subtitle: '2008 • Action',
    imageUrl: 'https://images.unsplash.com/photo-1509248961158-e54f6934749c?auto=format&fit=crop&w=400&q=80',
    rating: 4.8,
    status: 'watching',
    type: 'movie',
    latestTime: Date.now() - 3600000 * 15
  },
  {
    id: 'god-of-war',
    title: 'God of War',
    subtitle: 'Santa Monica Studio',
    imageUrl: 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=400&q=80',
    rating: 4.9,
    status: 'playing',
    type: 'game',
    latestTime: Date.now() - 3600000 * 20
  },
  {
    id: 'one-piece',
    title: 'One Piece',
    subtitle: 'Eiichiro Oda',
    imageUrl: 'https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=400&q=80',
    rating: 4.8,
    status: 'reading',
    type: 'manga',
    latestTime: Date.now() - 3600000 * 25
  },
  {
    id: 'berserk',
    title: 'Berserk',
    subtitle: 'Kentaro Miura',
    imageUrl: 'https://images.unsplash.com/photo-1618336753974-aae8e04506aa?auto=format&fit=crop&w=400&q=80',
    rating: 4.9,
    status: 'reading',
    type: 'manga',
    latestTime: Date.now() - 3600000 * 35
  },
  {
    id: 'atomic-habits',
    title: 'Atomic Habits',
    subtitle: 'James Clear',
    imageUrl: 'https://images.unsplash.com/photo-1484480974693-2ca0a72f3a4b?auto=format&fit=crop&w=400&q=80',
    rating: 4.5,
    status: 'reading',
    type: 'book',
    latestTime: Date.now() - 3600000 * 40
  },
  {
    id: 'hades',
    title: 'Hades',
    subtitle: 'Supergiant',
    imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=400&q=80',
    rating: 4.7,
    status: 'playing',
    type: 'game',
    latestTime: Date.now() - 3600000 * 45
  },
  {
    id: 'hollow-knight',
    title: 'Hollow Knight',
    subtitle: 'Team Cherry',
    imageUrl: 'https://images.unsplash.com/photo-1508739773434-c26b3d09e071?auto=format&fit=crop&w=400&q=80',
    rating: 4.6,
    status: 'playing',
    type: 'game',
    latestTime: Date.now() - 3600000 * 50
  },
  {
    id: 'sapiens',
    title: 'Sapiens',
    subtitle: 'Yuval Noah Harari',
    imageUrl: 'https://images.unsplash.com/photo-1447069387593-a5de0862481e?auto=format&fit=crop&w=400&q=80',
    rating: 4.5,
    status: 'reading',
    type: 'book',
    latestTime: Date.now() - 3600000 * 55
  },
  {
    id: 'vagabond',
    title: 'Vagabond',
    subtitle: 'Takehiko Inoue',
    imageUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=400&q=80',
    rating: 4.8,
    status: 'reading',
    type: 'manga',
    latestTime: Date.now() - 3600000 * 60
  },
  {
    id: 'jujutsu-kaisen',
    title: 'Jujutsu Kaisen',
    subtitle: 'MAPPA',
    imageUrl: 'https://images.unsplash.com/photo-1501854140801-50d01698950b?auto=format&fit=crop&w=400&q=80',
    rating: 4.7,
    status: 'watching',
    type: 'anime',
    latestTime: Date.now() - 3600000 * 65
  },
  {
    id: 'demon-slayer',
    title: 'Demon Slayer',
    subtitle: 'ufotable',
    imageUrl: 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=400&q=80',
    rating: 4.8,
    status: 'watching',
    type: 'anime',
    latestTime: Date.now() - 3600000 * 70
  }
];
