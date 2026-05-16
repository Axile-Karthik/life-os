import { Component, Input, ViewChild, ElementRef, computed, signal } from '@angular/core';
import { MediaItem } from '../../models/activity.model';
import { MediaCardComponent } from '../../components/media-card.component';
import { SectionHeaderComponent } from '../../components/section-header.component';
import { RequestState } from '../../models/request-state.model';

@Component({
  selector: 'app-media-strip',
  standalone: true,
  imports: [MediaCardComponent, SectionHeaderComponent],
  template: `
    <div class="media-strip">
      <app-section-header [title]="title">
        <div class="strip-controls">
          <button class="strip-btn" (click)="scrollLeft()" aria-label="Scroll left">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
          </button>
          <button class="strip-btn" (click)="scrollRight()" aria-label="Scroll right">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg>
          </button>
        </div>
      </app-section-header>
      
      <div class="strip-container" #scrollContainer>
        <div class="strip-track">
          @if (state.loading && !state.data) {
            @for (i of [1, 2, 3, 4, 5]; track i) {
              <div class="strip-item">
                <div class="skeleton-glow" style="width: 100%; aspect-ratio: 2/3; border-radius: var(--radius-md);"></div>
                <div class="skeleton-shimmer" style="width: 80%; height: 16px; border-radius: 4px; margin-top: 12px; margin-bottom: 6px;"></div>
                <div class="skeleton-shimmer" style="width: 50%; height: 12px; border-radius: 4px;"></div>
              </div>
            }
          } @else if (state.data) {
            @for (item of state.data; track item.id) {
              <div class="strip-item hover-lift">
                <app-media-card 
                  [title]="item.title" 
                  [imageUrl]="item.imageUrl" 
                  [subtitle]="item.subtitle || ''" 
                  [progress]="item.progress" 
                  [rating]="item.rating" 
                  [status]="item.status || ''" />
              </div>
            }
            @if (state.data.length === 0) {
              <div class="empty-state">No items found for {{ title.toLowerCase() }}.</div>
            }
          } @else if (state.error) {
            <div class="empty-state" style="color: var(--error);">Failed to load {{ title.toLowerCase() }}.</div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [`
    .media-strip { margin-bottom: 32px; }
    .strip-controls { display: flex; gap: 8px; }
    .strip-btn { width: 32px; height: 32px; border-radius: 50%; background: var(--bg-tertiary); display: flex; align-items: center; justify-content: center; color: var(--text-secondary); border: 1px solid transparent; transition: all var(--transition-fast); }
    .strip-btn:hover { background: var(--card-bg-hover); color: var(--text-primary); border-color: var(--card-border-hover); }
    .strip-container { width: 100%; overflow-x: auto; scrollbar-width: none; -ms-overflow-style: none; scroll-behavior: smooth; padding: 8px 0 24px 0; margin: -8px 0 -24px 0; }
    .strip-container::-webkit-scrollbar { display: none; }
    .strip-track { display: flex; gap: 20px; padding: 0 4px; }
    .strip-item { flex-shrink: 0; width: 220px; }
    .empty-state { padding: 40px; text-align: center; color: var(--text-secondary); font-size: 14px; width: 100%; }
    
    /* Cinematic Skeletons */
    .skeleton-glow { background: rgba(255,255,255,0.02); border: 1px solid rgba(255,255,255,0.05); animation: pulse 2s infinite ease-in-out; }
    .skeleton-shimmer { background: linear-gradient(90deg, rgba(255,255,255,0.02) 25%, rgba(255,255,255,0.05) 50%, rgba(255,255,255,0.02) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite linear; }
    @keyframes pulse { 0% { opacity: 0.5; } 50% { opacity: 0.8; } 100% { opacity: 0.5; } }
    @keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
    
    @media (max-width: 768px) {
      .strip-item { width: 160px; }
      .strip-controls { display: none; }
    }
  `]
})
export class MediaStripWidget {
  @Input({ required: true }) title!: string;
  @Input({ required: true }) state!: RequestState<MediaItem[]>;
  @ViewChild('scrollContainer') scrollContainer!: ElementRef<HTMLDivElement>;

  scrollLeft() {
    if (this.scrollContainer) {
      this.scrollContainer.nativeElement.scrollBy({ left: -300, behavior: 'smooth' });
    }
  }

  scrollRight() {
    if (this.scrollContainer) {
      this.scrollContainer.nativeElement.scrollBy({ left: 300, behavior: 'smooth' });
    }
  }
}
