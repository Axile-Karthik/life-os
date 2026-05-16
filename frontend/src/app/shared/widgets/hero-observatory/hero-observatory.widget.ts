import { Component, Input } from '@angular/core';
import { HeroStats } from '../../models/activity.model';
import { RequestState } from '../../models/request-state.model';

@Component({
  selector: 'app-hero-observatory',
  standalone: true,
  template: `
    <div class="hero">
      <div class="hero__bg animate-gradient-shift">
        <div class="hero__orb hero__orb--1 animate-orb-float"></div>
        <div class="hero__orb hero__orb--2 animate-ambient-pulse"></div>
        <div class="hero__noise"></div>
      </div>
      <div class="hero__content">
        @if (state.loading && !state.data) {
          <!-- Cinematic Skeleton -->
          <div class="hero__focus skeleton-glow" style="width: 160px; height: 160px; border-radius: 50%;"></div>
          <div class="hero__stats">
            <div class="skeleton-shimmer" style="width: 250px; height: 36px; border-radius: 4px; margin-bottom: 8px;"></div>
            <div class="skeleton-shimmer" style="width: 300px; height: 20px; border-radius: 4px; margin-bottom: 24px;"></div>
            <div class="hero__metrics-grid">
              <div class="metric-card glass-sm skeleton-glow" style="height: 70px;"></div>
              <div class="metric-card glass-sm skeleton-glow" style="height: 70px;"></div>
              <div class="metric-card glass-sm skeleton-glow" style="height: 70px;"></div>
            </div>
          </div>
        } @else if (state.data) {
          <!-- Real Content -->
          <div class="hero__focus hover-lift">
            <div class="hero__focus-ring">
              <svg viewBox="0 0 120 120" class="hero__focus-svg">
                <circle cx="60" cy="60" r="52" fill="none" stroke="var(--bg-tertiary)" stroke-width="8"/>
                <circle cx="60" cy="60" r="52" fill="none" stroke="url(#focusGrad)" stroke-width="8"
                  stroke-dasharray="326.7" [attr.stroke-dashoffset]="326.7 - (326.7 * state.data.focusScore / 100)"
                  stroke-linecap="round" transform="rotate(-90 60 60)"/>
                <defs><linearGradient id="focusGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="var(--accent-primary)"/><stop offset="100%" stop-color="var(--accent-secondary)"/>
                </linearGradient></defs>
              </svg>
              <div class="hero__focus-value">
                <span class="hero__focus-num">{{ state.data.focusScore }}<span style="font-size: 14px">%</span></span>
                <span class="hero__focus-label">Focus Score</span>
              </div>
            </div>
          </div>
          
          <div class="hero__stats">
            <div class="hero__title-area">
              <h1 class="hero__title">Life Observatory</h1>
              <p class="hero__subtitle">
                System nominal. Ready for next activity.
                @if (state.isStale) { <span class="stale-indicator">• Telemetry delayed</span> }
              </p>
            </div>
            
            <div class="hero__metrics-grid">
              <div class="metric-card glass-sm hover-lift">
                <div class="metric-card__val">{{ state.data.hoursToday }}h</div>
                <div class="metric-card__label">Active Today</div>
              </div>
              <div class="metric-card glass-sm hover-lift">
                <div class="metric-card__val text-gradient">{{ state.data.currentStreak }}</div>
                <div class="metric-card__label">Day Streak</div>
              </div>
              <div class="metric-card glass-sm hover-lift">
                <div class="metric-card__val">{{ state.data.totalActivities }}</div>
                <div class="metric-card__label">Total Logs</div>
              </div>
            </div>
          </div>
        } @else if (state.error) {
          <!-- Error State -->
          <div class="hero__stats" style="align-items: center; text-align: center; width: 100%;">
            <div class="hero__title" style="color: var(--error);">Observatory Offline</div>
            <div class="hero__subtitle">{{ state.error }}</div>
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    /* Same styles as before */
    .hero { position: relative; border-radius: var(--radius-xl); overflow: hidden; padding: 40px; background: var(--bg-observatory); border: 1px solid var(--observatory-border); box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4); }
    .hero__bg { position: absolute; inset: 0; background: var(--gradient-cinematic); pointer-events: none; z-index: 0; }
    .hero__noise { position: absolute; inset: 0; background-image: url('data:image/svg+xml,%3Csvg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg"%3E%3Cfilter id="noiseFilter"%3E%3CfeTurbulence type="fractalNoise" baseFrequency="0.65" numOctaves="3" stitchTiles="stitch"/%3E%3C/filter%3E%3Crect width="100%25" height="100%25" filter="url(%23noiseFilter)" opacity="0.05"/%3E%3C/svg%3E'); mix-blend-mode: overlay; opacity: 0.3; }
    .hero__orb { position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.5; }
    .hero__orb--1 { width: 300px; height: 300px; background: var(--accent-primary); top: -100px; right: -50px; }
    .hero__orb--2 { width: 250px; height: 250px; background: var(--accent-secondary); bottom: -120px; left: 10%; animation-delay: 2s; }
    .hero__content { position: relative; z-index: 1; display: flex; align-items: center; gap: 48px; }
    .hero__focus { flex-shrink: 0; background: var(--card-bg); backdrop-filter: blur(var(--blur-lg)); border: 1px solid var(--card-border); border-radius: 50%; padding: 16px; box-shadow: var(--glow-ambient); }
    .hero__focus-ring { position: relative; width: 160px; height: 160px; }
    .hero__focus-svg { width: 100%; height: 100%; drop-shadow: 0 0 12px var(--accent-primary); }
    .hero__focus-value { position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; }
    .hero__focus-num { font-size: 36px; font-weight: 800; color: var(--text-primary); letter-spacing: -1px; }
    .hero__focus-label { font-size: 12px; color: var(--text-secondary); font-weight: 500; text-transform: uppercase; letter-spacing: 1px; margin-top: 2px; }
    .hero__stats { flex: 1; display: flex; flex-direction: column; gap: 24px; }
    .hero__title { font-size: 32px; font-weight: 700; color: var(--text-primary); letter-spacing: -0.5px; margin-bottom: 8px; }
    .hero__subtitle { font-size: 16px; color: var(--text-secondary); font-weight: 400; display: flex; align-items: center; gap: 8px; }
    .hero__metrics-grid { display: flex; gap: 16px; }
    .metric-card { padding: 16px 24px; border-radius: var(--radius-lg); min-width: 140px; }
    .metric-card__val { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 4px; }
    .metric-card__label { font-size: 11px; color: var(--text-tertiary); text-transform: uppercase; letter-spacing: 0.5px; font-weight: 600; }
    .stale-indicator { font-size: 12px; color: var(--warning); font-weight: 500; }
    
    /* Cinematic Skeletons */
    .skeleton-glow { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); animation: pulse 2s infinite ease-in-out; }
    .skeleton-shimmer { background: linear-gradient(90deg, rgba(255,255,255,0.02) 25%, rgba(255,255,255,0.05) 50%, rgba(255,255,255,0.02) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite linear; }
    @keyframes pulse { 0% { opacity: 0.5; } 50% { opacity: 0.8; } 100% { opacity: 0.5; } }
    @keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
    
    @media (max-width: 768px) {
      .hero { padding: 24px; }
      .hero__content { flex-direction: column; text-align: center; gap: 32px; }
      .hero__metrics-grid { flex-wrap: wrap; justify-content: center; }
      .metric-card { flex: 1; min-width: 120px; }
    }
  `]
})
export class HeroObservatoryWidget {
  @Input({ required: true }) state!: RequestState<HeroStats>;
}
