import { Component, input } from '@angular/core';

@Component({
  selector: 'app-media-card',
  standalone: true,
  template: `
    <div class="media-card" [class.media-card--horizontal]="layout() === 'horizontal'">
      <div class="media-card__poster">
        <img [src]="imageUrl()" [alt]="title()" loading="lazy" />
        @if (progress() !== undefined && progress()! < 100) {
          <div class="media-card__progress">
            <div class="media-card__progress-fill" [style.width.%]="progress()"></div>
          </div>
        }
        @if (rating()) {
          <div class="media-card__rating">
            <svg width="10" height="10" viewBox="0 0 24 24" fill="var(--accent-warm)" stroke="none">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
            {{ rating() }}
          </div>
        }
        @if (status() === 'playing' || status() === 'watching' || status() === 'reading' || status() === 'listening') {
          <div class="media-card__live-badge">
            <span class="media-card__live-dot"></span>
            {{ statusLabel() }}
          </div>
        }
        <div class="media-card__overlay"></div>
      </div>
      <div class="media-card__info">
        <h4 class="media-card__title">{{ title() }}</h4>
        @if (subtitle()) {
          <p class="media-card__subtitle">{{ subtitle() }}</p>
        }
      </div>
    </div>
  `,
  styles: [`
    .media-card {
      flex-shrink: 0;
      cursor: pointer;
      transition: all var(--transition-base);
    }

    .media-card:hover {
      transform: translateY(-4px);
    }

    .media-card:hover .media-card__overlay {
      opacity: 1;
    }

    .media-card__poster {
      position: relative;
      border-radius: var(--radius-md);
      overflow: hidden;
      aspect-ratio: 2/3;
      width: 140px;
      background: var(--bg-tertiary);
    }

    .media-card--horizontal .media-card__poster {
      width: 120px;
    }

    .media-card__poster img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform var(--transition-slow);
    }

    .media-card:hover .media-card__poster img {
      transform: scale(1.05);
    }

    .media-card__overlay {
      position: absolute;
      inset: 0;
      background: linear-gradient(to top, rgba(0,0,0,0.6) 0%, transparent 50%);
      opacity: 0;
      transition: opacity var(--transition-base);
    }

    .media-card__progress {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: rgba(0, 0, 0, 0.5);
    }

    .media-card__progress-fill {
      height: 100%;
      background: var(--accent-primary);
      border-radius: 0 2px 0 0;
      transition: width 0.8s ease-out;
      animation: progressFill 1s ease-out;
    }

    .media-card__rating {
      position: absolute;
      top: 8px;
      right: 8px;
      display: flex;
      align-items: center;
      gap: 3px;
      padding: 3px 7px;
      border-radius: var(--radius-sm);
      background: rgba(0, 0, 0, 0.7);
      backdrop-filter: blur(4px);
      font-size: 11px;
      font-weight: 600;
      color: var(--accent-warm);
    }

    .media-card__live-badge {
      position: absolute;
      top: 8px;
      left: 8px;
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 3px 8px;
      border-radius: var(--radius-sm);
      background: rgba(139, 92, 246, 0.8);
      backdrop-filter: blur(4px);
      font-size: 10px;
      font-weight: 600;
      color: white;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .media-card__live-dot {
      width: 5px;
      height: 5px;
      border-radius: 50%;
      background: #10b981;
      animation: glowPulse 2s ease-in-out infinite;
    }

    .media-card__info {
      padding-top: 10px;
      max-width: 140px;
    }

    .media-card--horizontal .media-card__info {
      max-width: 120px;
    }

    .media-card__title {
      font-size: 13px;
      font-weight: 600;
      color: var(--text-primary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .media-card__subtitle {
      font-size: 11px;
      color: var(--text-secondary);
      margin-top: 2px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    @media (max-width: 768px) {
      .media-card__poster {
        width: 110px;
      }

      .media-card__info {
        max-width: 110px;
      }
    }
  `],
})
export class MediaCardComponent {
  title = input.required<string>();
  imageUrl = input.required<string>();
  subtitle = input<string>('');
  progress = input<number | undefined>(undefined);
  rating = input<number | undefined>(undefined);
  status = input<string>('');
  layout = input<'vertical' | 'horizontal'>('vertical');

  statusLabel(): string {
    switch (this.status()) {
      case 'playing': return 'Playing';
      case 'watching': return 'Watching';
      case 'reading': return 'Reading';
      case 'listening': return 'Live';
      default: return '';
    }
  }
}
