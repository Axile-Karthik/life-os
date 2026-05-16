import { Component } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { MediaCardComponent } from '../../shared/components/media-card.component';
import { MOCK_RECENT_MEDIA, MOCK_GAMES, MOCK_BOOKS, MOCK_MANGA, MOCK_ANIME, MOCK_MUSIC } from '../../shared/data/mock-data';
import { MediaItem } from '../../shared/models/activity.model';

@Component({
  selector: 'app-library',
  standalone: true,
  imports: [PageContainerComponent, SectionHeaderComponent, MediaCardComponent],
  template: `
    <app-page-container>
      <app-section-header title="Library" subtitle="Your media universe" />
      <div class="lib-tabs">
        @for (t of tabs; track t) {
          <button class="tab-btn" [class.tab-btn--active]="t === activeTab" (click)="activeTab = t">{{ t }}</button>
        }
      </div>
      <div class="lib-search">
        <input type="text" placeholder="Search your library..." class="lib-search__input" />
      </div>
      <div class="lib-grid">
        @for (item of currentItems(); track item.id) {
          <app-media-card [title]="item.title" [imageUrl]="item.imageUrl" [subtitle]="item.subtitle || ''" [progress]="item.progress" [rating]="item.rating" [status]="item.status || ''" />
        }
      </div>
    </app-page-container>
  `,
  styles: [`
    .lib-tabs { display: flex; gap: 8px; margin-bottom: 16px; flex-wrap: wrap; }
    .tab-btn { padding: 8px 20px; border-radius: var(--radius-full); background: var(--card-bg); border: 1px solid var(--card-border); color: var(--text-secondary); font-size: 13px; font-weight: 500; transition: all var(--transition-fast); cursor: pointer; }
    .tab-btn:hover { border-color: var(--card-border-hover); color: var(--text-primary); }
    .tab-btn--active { background: var(--accent-primary-dim); border-color: var(--accent-primary); color: var(--accent-primary); }
    .lib-search { margin-bottom: 20px; }
    .lib-search__input { width: 100%; max-width: 400px; padding: 10px 16px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: var(--radius-md); color: var(--text-primary); font-size: 14px; outline: none; font-family: inherit; transition: border-color var(--transition-fast); }
    .lib-search__input:focus { border-color: var(--accent-primary); }
    .lib-search__input::placeholder { color: var(--text-tertiary); }
    .lib-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(140px, 1fr)); gap: 20px; }
    @media (max-width: 768px) { .lib-grid { grid-template-columns: repeat(auto-fill, minmax(110px, 1fr)); gap: 12px; } }
  `],
})
export class LibraryComponent {
  tabs = ['All', 'Movies', 'Games', 'Anime', 'Books', 'Manga', 'Music'];
  activeTab = 'All';
  all = [...MOCK_RECENT_MEDIA, ...MOCK_GAMES, ...MOCK_BOOKS, ...MOCK_MANGA, ...MOCK_ANIME, ...MOCK_MUSIC];
  dataMap: Record<string, MediaItem[]> = {
    All: this.all, Movies: MOCK_RECENT_MEDIA.filter(m => m.type === 'movie'),
    Games: MOCK_GAMES, Anime: MOCK_ANIME, Books: MOCK_BOOKS, Manga: MOCK_MANGA, Music: MOCK_MUSIC,
  };
  currentItems(): MediaItem[] { return this.dataMap[this.activeTab] || this.all; }
}
