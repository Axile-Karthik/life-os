import { Injectable, signal, effect } from '@angular/core';
import { ThemeConfig } from './theme.model';
import { darkTheme } from './themes/dark.theme';
import { lightTheme } from './themes/light.theme';
import { spaceTheme } from './themes/space.theme';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly STORAGE_KEY = 'life-os-theme';

  readonly themes: ThemeConfig[] = [darkTheme, lightTheme, spaceTheme];
  readonly currentTheme = signal<ThemeConfig>(darkTheme);

  constructor() {
    const savedKey = localStorage.getItem(this.STORAGE_KEY);
    const saved = this.themes.find(t => t.key === savedKey);
    if (saved) {
      this.currentTheme.set(saved);
    }
    this.applyTheme(this.currentTheme());

    effect(() => {
      const theme = this.currentTheme();
      this.applyTheme(theme);
      localStorage.setItem(this.STORAGE_KEY, theme.key);
    });
  }

  setTheme(key: string): void {
    const theme = this.themes.find(t => t.key === key);
    if (theme) {
      this.currentTheme.set(theme);
    }
  }

  cycleTheme(): void {
    const current = this.currentTheme();
    const idx = this.themes.findIndex(t => t.key === current.key);
    const next = this.themes[(idx + 1) % this.themes.length];
    this.currentTheme.set(next);
  }

  private applyTheme(theme: ThemeConfig): void {
    const root = document.documentElement;
    Object.entries(theme.variables).forEach(([prop, value]) => {
      root.style.setProperty(prop, value);
    });
  }
}
