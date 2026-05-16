export interface ActivityVisuals {
  icon: string;
  color: string;
  label: string;
}

export const ACTIVITY_TYPE_REGISTRY: Record<string, ActivityVisuals> = {
  'game': { icon: '🎮', color: 'var(--accent-secondary)', label: 'Gaming' },
  'app': { icon: '📱', color: 'var(--accent-primary)', label: 'App' },
  'anime': { icon: '⛩️', color: 'var(--accent-rose)', label: 'Anime' },
  'manga': { icon: '📖', color: 'var(--accent-warm)', label: 'Manga' },
  'book': { icon: '📚', color: 'var(--accent-emerald)', label: 'Reading' },
  'music': { icon: '🎵', color: 'var(--info)', label: 'Music' },
  'vscode': { icon: '💻', color: 'var(--accent-secondary)', label: 'Coding' },
  'spotify': { icon: '🎧', color: 'var(--accent-emerald)', label: 'Listening' },
  'obsidian': { icon: '📓', color: 'var(--accent-primary)', label: 'Writing' },
  'notion': { icon: '📝', color: 'var(--text-secondary)', label: 'Planning' },
  'default': { icon: '⚡', color: 'var(--text-tertiary)', label: 'Activity' }
};

export function getActivityVisuals(type: string, source: string, packageName?: string): ActivityVisuals {
  // Check explicit package overrides
  if (packageName) {
    const pkgLower = packageName.toLowerCase();
    if (pkgLower.includes('spotify')) return ACTIVITY_TYPE_REGISTRY['spotify'];
    if (pkgLower.includes('vscode')) return ACTIVITY_TYPE_REGISTRY['vscode'];
    if (pkgLower.includes('obsidian')) return ACTIVITY_TYPE_REGISTRY['obsidian'];
    if (pkgLower.includes('notion')) return ACTIVITY_TYPE_REGISTRY['notion'];
  }
  
  // Check source overrides
  if (source) {
    const sourceLower = source.toLowerCase();
    if (sourceLower === 'jellyfin' && type !== 'music') return ACTIVITY_TYPE_REGISTRY['anime']; // Example fallback
  }

  // Use base type
  const tLower = type?.toLowerCase() || '';
  return ACTIVITY_TYPE_REGISTRY[tLower] || ACTIVITY_TYPE_REGISTRY['default'];
}
