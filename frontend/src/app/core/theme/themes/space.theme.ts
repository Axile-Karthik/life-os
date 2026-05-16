import { ThemeConfig } from '../theme.model';

export const spaceTheme: ThemeConfig = {
  key: 'space',
  name: 'Deep Space',
  variables: {
    '--bg-primary': '#030712',
    '--bg-secondary': '#0a0f1e',
    '--bg-tertiary': '#111827',
    '--bg-surface': '#1a2035',

    '--card-bg': 'rgba(10, 15, 30, 0.8)',
    '--card-bg-solid': '#0d1425',
    '--card-bg-hover': 'rgba(15, 20, 40, 0.9)',
    '--card-border': 'rgba(6, 182, 212, 0.12)',
    '--card-border-hover': 'rgba(6, 182, 212, 0.3)',
    '--card-shadow': '0 4px 24px rgba(0, 0, 0, 0.6)',

    '--text-primary': '#e2e8f0',
    '--text-secondary': '#94a3b8',
    '--text-tertiary': '#64748b',
    '--text-muted': '#475569',

    '--accent-primary': '#06b6d4',
    '--accent-primary-hover': '#22d3ee',
    '--accent-primary-dim': 'rgba(6, 182, 212, 0.15)',
    '--accent-secondary': '#8b5cf6',
    '--accent-secondary-dim': 'rgba(139, 92, 246, 0.15)',
    '--accent-warm': '#f59e0b',
    '--accent-warm-dim': 'rgba(245, 158, 11, 0.15)',
    '--accent-cyan': '#06b6d4',
    '--accent-cyan-dim': 'rgba(6, 182, 212, 0.15)',
    '--accent-rose': '#f43f5e',
    '--accent-rose-dim': 'rgba(244, 63, 94, 0.15)',
    '--accent-emerald': '#10b981',
    '--accent-emerald-dim': 'rgba(16, 185, 129, 0.15)',

    '--glow-primary': '0 0 50px rgba(6, 182, 212, 0.12)',
    '--glow-secondary': '0 0 40px rgba(139, 92, 246, 0.08)',
    '--glow-warm': '0 0 40px rgba(245, 158, 11, 0.08)',

    '--gradient-hero': 'linear-gradient(135deg, #030712 0%, #0a1628 40%, #0a0f1e 70%, #030712 100%)',
    '--gradient-card': 'linear-gradient(135deg, rgba(6, 182, 212, 0.04) 0%, rgba(139, 92, 246, 0.02) 100%)',
    '--gradient-accent': 'linear-gradient(135deg, #06b6d4 0%, #8b5cf6 100%)',

    '--sidebar-bg': 'rgba(3, 7, 18, 0.95)',
    '--sidebar-border': 'rgba(6, 182, 212, 0.08)',
    '--sidebar-width': '260px',
    '--sidebar-width-collapsed': '72px',

    '--topbar-bg': 'rgba(3, 7, 18, 0.88)',
    '--topbar-border': 'rgba(6, 182, 212, 0.08)',
    '--topbar-height': '64px',

    '--radius-sm': '8px',
    '--radius-md': '12px',
    '--radius-lg': '16px',
    '--radius-xl': '20px',
    '--radius-full': '9999px',

    '--blur-sm': '8px',
    '--blur-md': '16px',
    '--blur-lg': '24px',

    '--transition-fast': '150ms cubic-bezier(0.4, 0, 0.2, 1)',
    '--transition-base': '250ms cubic-bezier(0.4, 0, 0.2, 1)',
    '--transition-slow': '400ms cubic-bezier(0.4, 0, 0.2, 1)',

    '--scrollbar-bg': 'rgba(3, 7, 18, 0.5)',
    '--scrollbar-thumb': 'rgba(6, 182, 212, 0.3)',
    '--scrollbar-thumb-hover': 'rgba(6, 182, 212, 0.5)',

    '--success': '#10b981',
    '--warning': '#f59e0b',
    '--error': '#ef4444',
    '--info': '#06b6d4',
  },
};
