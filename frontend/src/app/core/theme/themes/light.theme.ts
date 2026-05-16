import { ThemeConfig } from '../theme.model';

export const lightTheme: ThemeConfig = {
  key: 'light',
  name: 'Daylight',
  variables: {
    '--bg-primary': '#f0f2f8',
    '--bg-secondary': '#e8ecf4',
    '--bg-tertiary': '#dde3f0',
    '--bg-surface': '#ffffff',

    '--card-bg': 'rgba(255, 255, 255, 0.8)',
    '--card-bg-solid': '#ffffff',
    '--card-bg-hover': 'rgba(255, 255, 255, 0.95)',
    '--card-border': 'rgba(139, 92, 246, 0.12)',
    '--card-border-hover': 'rgba(139, 92, 246, 0.25)',
    '--card-shadow': '0 4px 24px rgba(0, 0, 0, 0.06)',

    '--text-primary': '#1e293b',
    '--text-secondary': '#475569',
    '--text-tertiary': '#94a3b8',
    '--text-muted': '#cbd5e1',

    '--accent-primary': '#7c3aed',
    '--accent-primary-hover': '#6d28d9',
    '--accent-primary-dim': 'rgba(124, 58, 237, 0.1)',
    '--accent-secondary': '#2563eb',
    '--accent-secondary-dim': 'rgba(37, 99, 235, 0.1)',
    '--accent-warm': '#d97706',
    '--accent-warm-dim': 'rgba(217, 119, 6, 0.1)',
    '--accent-cyan': '#0891b2',
    '--accent-cyan-dim': 'rgba(8, 145, 178, 0.1)',
    '--accent-rose': '#e11d48',
    '--accent-rose-dim': 'rgba(225, 29, 72, 0.1)',
    '--accent-emerald': '#059669',
    '--accent-emerald-dim': 'rgba(5, 150, 105, 0.1)',

    '--glow-primary': '0 0 30px rgba(124, 58, 237, 0.08)',
    '--glow-secondary': '0 0 30px rgba(37, 99, 235, 0.06)',
    '--glow-warm': '0 0 30px rgba(217, 119, 6, 0.06)',

    '--gradient-hero': 'linear-gradient(135deg, #f0f2f8 0%, #e8e0f8 40%, #e8ecf4 70%, #e0ecf8 100%)',
    '--gradient-card': 'linear-gradient(135deg, rgba(124, 58, 237, 0.03) 0%, rgba(37, 99, 235, 0.02) 100%)',
    '--gradient-accent': 'linear-gradient(135deg, #7c3aed 0%, #2563eb 100%)',

    '--sidebar-bg': 'rgba(255, 255, 255, 0.85)',
    '--sidebar-border': 'rgba(139, 92, 246, 0.1)',
    '--sidebar-width': '260px',
    '--sidebar-width-collapsed': '72px',

    '--topbar-bg': 'rgba(255, 255, 255, 0.8)',
    '--topbar-border': 'rgba(139, 92, 246, 0.1)',
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

    '--scrollbar-bg': 'rgba(240, 242, 248, 0.5)',
    '--scrollbar-thumb': 'rgba(124, 58, 237, 0.2)',
    '--scrollbar-thumb-hover': 'rgba(124, 58, 237, 0.4)',

    '--success': '#059669',
    '--warning': '#d97706',
    '--error': '#dc2626',
    '--info': '#2563eb',
  },
};
