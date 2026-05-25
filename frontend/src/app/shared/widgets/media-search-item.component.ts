import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SearchResultDto } from '../../core/api/dto/metadata.dto';

@Component({
  selector: 'app-media-search-item',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="search-item" (click)="onSelect()">
      <div class="search-item__cover">
        @if (item.imageUrl) {
          <img [src]="item.imageUrl" [alt]="item.title" (error)="imageError = true" [class.hidden]="imageError" />
        }
        @if (!item.imageUrl || imageError) {
          <div class="search-item__placeholder">
            <span class="material-symbols-rounded">{{ getIcon() }}</span>
          </div>
        }
      </div>
      <div class="search-item__info">
        <div class="search-item__title">{{ item.title }}</div>
        <div class="search-item__meta">
          <span class="search-item__year">{{ getYear() }}</span>
          <span class="search-item__dot">•</span>
          <span class="search-item__source">{{ item.externalSource }}</span>
        </div>
      </div>
      <div class="search-item__actions">
        <span class="material-symbols-rounded">add_circle</span>
      </div>
    </div>
  `,
  styles: [`
    .search-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 12px;
      border-radius: var(--radius-md);
      transition: all var(--transition-fast);
      cursor: pointer;
      user-select: none;
    }

    .search-item:hover {
      background: var(--card-bg-hover);
      box-shadow: var(--shadow-sm), 0 0 15px var(--accent-primary-dim);
    }

    .search-item__cover {
      width: 40px;
      height: 56px;
      border-radius: var(--radius-xs);
      background: var(--bg-tertiary);
      overflow: hidden;
      flex-shrink: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid var(--card-border);
    }

    .search-item__cover img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .search-item__cover img.hidden { display: none; }

    .search-item__placeholder {
      color: var(--text-tertiary);
      font-size: 20px;
    }

    .search-item__info {
      flex: 1;
      min-width: 0;
    }

    .search-item__title {
      color: var(--text-primary);
      font-size: 14px;
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      margin-bottom: 2px;
    }

    .search-item__meta {
      display: flex;
      align-items: center;
      gap: 4px;
      color: var(--text-tertiary);
      font-size: 11px;
    }

    .search-item__dot { font-size: 8px; opacity: 0.5; }

    .search-item__actions {
      color: var(--text-tertiary);
      opacity: 0;
      transition: opacity var(--transition-fast);
    }

    .search-item:hover .search-item__actions {
      opacity: 1;
      color: var(--accent-primary);
    }

    .material-symbols-rounded { font-size: 20px; }
  `]
})
export class MediaSearchItemComponent {
  @Input({ required: true }) item!: SearchResultDto;
  @Output() select = new EventEmitter<SearchResultDto>();

  imageError = false;

  getYear(): string {
    if (!this.item.releaseDate) return 'N/A';
    try {
      return new Date(this.item.releaseDate).getFullYear().toString();
    } catch {
      return 'N/A';
    }
  }

  getIcon(): string {
    switch (this.item.contentType) {
      case 'GAME': return 'sports_esports';
      case 'MOVIE': return 'movie';
      case 'ANIME': return 'animation';
      case 'BOOK': return 'menu_book';
      default: return 'inventory_2';
    }
  }

  onSelect() {
    this.select.emit(this.item);
  }
}
