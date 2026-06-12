import { Component, inject, signal, computed, ElementRef, ViewChild, HostListener, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, switchMap, tap, of, catchError } from 'rxjs';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { MediaSearchItemComponent } from '../../shared/widgets/media-search-item.component';
import { MetadataApiService } from '../../core/api/services/metadata-api.service';
import { SearchResultDto } from '../../core/api/dto/metadata.dto';
import { DashboardState } from '../../core/state/dashboard.state';
import { formatDuration } from '../../shared/utils/time/time.utils';
import { LibraryApiService } from '../../core/api/services/library-api.service';
import { LibraryItemDto } from '../../core/api/models/library-item.dto';
import { LibraryStatus } from '../../core/api/models/library-status';
import { getMediaImage, getRelativeTimeString } from './library.constants';

@Component({
  selector: 'app-library',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    PageContainerComponent,
    MediaSearchItemComponent
  ],
  templateUrl: './library.component.html',
  styleUrl: './library.component.css',
})
export class LibraryComponent {
  private dashboard = inject(DashboardState);
  private metadataApi = inject(MetadataApiService);
  private libraryApi = inject(LibraryApiService);

  @ViewChild('searchContainer') searchContainer!: ElementRef;

  tabs = ['All', 'Movies', 'Games', 'Anime', 'Books', 'Manga', 'Music'];
  activeTab = signal('All');

  searchControl = new FormControl('');
  searchResults = signal<SearchResultDto[]>([]);
  loading = signal(false);
  error = signal(false);
  showResults = false;

  libraryItems = signal<LibraryItemDto[]>([]);

  handleImageError(event: any) {
    if (event.target) {
      event.target.style.display = 'none';
    }
  }

  // Retrieve sessions from DashboardState
  allSessions = computed(() => this.dashboard.sessions().data || []);

  // Compute Total Duration Formatted
  totalDurationFormatted = computed(() => {
    const sessions = this.allSessions();
    const totalMillis = sessions.reduce((acc, curr) => acc + (curr.durationMillis || 0), 0);
    // Combine mock database durations (approx 26.5 hours) with user logged sessions
    const totalCombinedMillis = totalMillis + (26.75 * 3600 * 1000);
    return formatDuration(totalCombinedMillis);
  });

  // Sessions count combined
  sessionsCount = computed(() => {
    return this.allSessions().length + 18; // Combined with mock sessions count
  });

  // Calculate Streak days
  streakDays = computed(() => {
    const stats = this.dashboard.heroStats();
    return stats ? stats.currentStreak : 12;
  });

  // Calculate Completed Count (completed items have status COMPLETED)
  completedCount = computed(() => {
    return this.libraryItems().filter(item => item.status === 'COMPLETED').length;
  });

  // Unique media count
  uniqueMediaCount = computed(() => {
    return this.libraryItems().length;
  });

  // Category counts
  categoryCounts = computed(() => {
    const items = this.libraryItems();
    const counts = { games: 0, movies: 0, anime: 0, books: 0 };
    for (const item of items) {
      const type = item.contentType;
      if (type === 'GAME') counts.games++;
      else if (type === 'MOVIE' || type === 'SERIES') counts.movies++;
      else if (type === 'ANIME') counts.anime++;
      else if (type === 'BOOK' || type === 'MANGA') counts.books++;
    }
    return counts;
  });

  // Continue playing items (mapped from libraryItems matching active statuses)
  continuePlayingItems = computed(() => {
    return this.libraryItems()
      .filter(item => ['PLAYING', 'WATCHING', 'READING'].includes(item.status))
      .sort((a, b) => {
        const timeA = a.lastActivityAt ? new Date(a.lastActivityAt).getTime() : 0;
        const timeB = b.lastActivityAt ? new Date(b.lastActivityAt).getTime() : 0;
        return timeB - timeA;
      })
      .map(item => ({
        id: item.id,
        title: item.title,
        subtitle: this.getSubtitle(item.contentType),
        imageUrl: item.imageUrl || getMediaImage(item.title, item.contentType.toLowerCase()),
        progress: item.progressPercent || 0,
        type: item.contentType.toLowerCase(),
        status: item.status
      }))
      .slice(0, 4);
  });

  // Library grid items: filter by activeTab, sort by lastActivityAt DESC
  libraryGridItems = computed(() => {
    const tab = this.activeTab();
    let filtered = this.libraryItems();
    if (tab !== 'All') {
      const typeMap: Record<string, string> = {
        'Movies': 'MOVIE',
        'Games': 'GAME',
        'Anime': 'ANIME',
        'Books': 'BOOK',
        'Manga': 'MANGA',
        'Music': 'MUSIC'
      };
      const targetType = typeMap[tab];
      filtered = filtered.filter(item => item.contentType === targetType);
    }
    return filtered.map(item => ({
      id: item.id,
      title: item.title,
      subtitle: this.getSubtitle(item.contentType),
      imageUrl: item.imageUrl || getMediaImage(item.title, item.contentType.toLowerCase()),
      progress: item.progressPercent || 0,
      type: item.contentType.toLowerCase(),
      status: item.status,
      rating: item.rating || 0.0,
      favorite: item.favorite
    }));
  });

  getSubtitle(type: string): string {
    if (!type) return '';
    const clean = type.toUpperCase();
    if (clean === 'GAME') return 'Game';
    if (clean === 'MOVIE') return 'Movie';
    if (clean === 'SERIES') return 'Series';
    if (clean === 'ANIME') return 'Anime';
    if (clean === 'BOOK') return 'Book';
    if (clean === 'MANGA') return 'Manga';
    if (clean === 'MUSIC') return 'Music';
    return type;
  }

  // Recent Activities (merge user logs with mockup activities)
  recentActivities = computed(() => {
    const sessions = this.allSessions();
    const sortedUser = [...sessions].sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime());

    const userAct = sortedUser.map(s => {
      const type = s.type ? s.type.toLowerCase() : '';
      let actionLabel = 'Logged';
      if (type === 'game') actionLabel = 'Played';
      else if (type === 'movie' || type === 'anime') actionLabel = 'Watched';
      else if (type === 'book' || type === 'manga') actionLabel = 'Read';

      return {
        id: s.id.toString(),
        title: s.title || s.packageName,
        desc: `${actionLabel} for ${formatDuration(s.durationMillis)} on ${s.source}`,
        time: getRelativeTimeString(new Date(s.startTime).getTime()),
        imageUrl: getMediaImage(s.title || s.packageName, type),
        latestTime: new Date(s.startTime).getTime()
      };
    });

    const mockAct = [
      {
        id: 'mock-1',
        title: 'Elden Ring',
        desc: 'Played for 2h 15m',
        time: '2h ago',
        imageUrl: 'https://images.unsplash.com/photo-1655821888788-6107699e173b?auto=format&fit=crop&w=400&q=80',
        latestTime: Date.now() - 3600000 * 2
      },
      {
        id: 'mock-2',
        title: "Baldur's Gate 3",
        desc: 'Played for 1h 20m',
        time: '5h ago',
        imageUrl: 'https://images.unsplash.com/photo-1519074069444-1ba4e6663104?auto=format&fit=crop&w=400&q=80',
        latestTime: Date.now() - 3600000 * 5
      },
      {
        id: 'mock-3',
        title: 'Dune: Part Two',
        desc: 'Watched for 1h 48m',
        time: 'Yesterday',
        imageUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=400&q=80',
        latestTime: Date.now() - 3600000 * 24
      },
      {
        id: 'mock-4',
        title: 'Atomic Habits',
        desc: 'Read for 35m',
        time: 'Yesterday',
        imageUrl: 'https://images.unsplash.com/photo-1484480974693-2ca0a72f3a4b?auto=format&fit=crop&w=400&q=80',
        latestTime: Date.now() - 3600000 * 28
      },
      {
        id: 'mock-5',
        title: 'One Piece Vol. 106',
        desc: 'Read for 20m',
        time: '2 days ago',
        imageUrl: 'https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=400&q=80',
        latestTime: Date.now() - 3600000 * 48
      }
    ];

    const combined = [...userAct];
    for (const m of mockAct) {
      if (!combined.some(u => u.title.toLowerCase() === m.title.toLowerCase())) {
        combined.push(m);
      }
    }

    return combined.sort((a, b) => b.latestTime - a.latestTime).slice(0, 5);
  });

  loadLibrary() {
    this.libraryApi.getLibraryItems(undefined, undefined, 'LAST_ACTIVE', 0, 200).subscribe({
      next: (res) => {
        this.libraryItems.set(res.content || []);
      },
      error: () => {
        this.libraryItems.set([]);
      }
    });
  }

  constructor() {
    effect(() => {
      this.activeTab();
      this.loadLibrary();
    });

    this.searchControl.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      tap(() => {
        this.loading.set(true);
        this.error.set(false);
      }),
      switchMap(val => {
        if (!val || val.length < 3) {
          return of({ content: [] });
        }
        return this.metadataApi.search(val, 'GAME').pipe(
          catchError(() => {
            this.error.set(true);
            return of({ content: [] });
          })
        );
      }),
      tap(() => this.loading.set(false))
    ).subscribe(res => {
      this.searchResults.set(res.content);
      if (this.searchControl.value && this.searchControl.value.length >= 3) {
        this.showResults = true;
      }
    });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (this.searchContainer && !this.searchContainer.nativeElement.contains(event.target)) {
      this.showResults = false;
    }
  }

  onExternalSelect(item: SearchResultDto) {
    this.showResults = false;

    let defaultStatus: LibraryStatus = 'WISHLIST';
    if (item.contentType === 'GAME') defaultStatus = 'PLAYING';
    else if (item.contentType === 'MOVIE' || item.contentType === 'SERIES' || item.contentType === 'ANIME') defaultStatus = 'WATCHING';
    else if (item.contentType === 'BOOK' || item.contentType === 'MANGA') defaultStatus = 'READING';

    this.libraryApi.addToLibrary({
      externalSource: item.externalSource,
      externalId: item.externalId,
      status: defaultStatus,
      title: item.title,
      imageUrl: item.imageUrl,
      contentType: item.contentType,
      releaseDate: item.releaseDate
    }).subscribe({
      next: () => {
        this.loadLibrary();
      },
      error: (err) => {
        console.error('Failed to add item to library:', err);
      }
    });
  }
}
