import { Component, inject, signal, computed, ElementRef, ViewChild, HostListener, effect, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, switchMap, tap, of, catchError } from 'rxjs';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { MediaSearchItemComponent } from '../../shared/widgets/media-search-item.component';
import { MetadataApiService } from '../../core/api/services/metadata-api.service';
import { SearchResultDto } from '../../core/api/dto/metadata.dto';
import { DashboardState } from '../../core/state/dashboard.state';
import { formatDuration } from '../../shared/utils/time/time.utils';
import { LibraryApiService } from '../../core/api/services/library-api.service';
import { LibraryItemDto } from '../../core/api/models/library-item.dto';
import { LibraryStatus } from '../../core/api/models/library-status';

// Helper to resolve high-fidelity Unsplash cover images based on title
function getMediaImage(title: string, type: string): string {
  const t = title.toLowerCase();

  // Dynamic high-fidelity cover image mapping for actual telemetry data
  if (t.includes('shadow hunter')) return 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=400&q=80';
  if (t.includes('free fire')) return 'https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=400&q=80';
  if (t.includes('limbo')) return 'https://images.unsplash.com/photo-1508739773434-c26b3d09e071?auto=format&fit=crop&w=400&q=80';
  if (t.includes('blasphemous')) return 'https://images.unsplash.com/photo-1519074069444-1ba4e6663104?auto=format&fit=crop&w=400&q=80';

  // Mockup fallback covers
  if (t.includes('elden')) return 'https://images.unsplash.com/photo-1655821888788-6107699e173b?auto=format&fit=crop&w=400&q=80';
  if (t.includes('baldur')) return 'https://images.unsplash.com/photo-1519074069444-1ba4e6663104?auto=format&fit=crop&w=400&q=80';
  if (t.includes('dune')) return 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=400&q=80';
  if (t.includes('chainsaw')) return 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=400&q=80';
  if (t.includes('interstellar')) return 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=400&q=80';
  if (t.includes('dark knight') || t.includes('batman')) return 'https://images.unsplash.com/photo-1509248961158-e54f6934749c?auto=format&fit=crop&w=400&q=80';
  if (t.includes('god of war')) return 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=400&q=80';
  if (t.includes('one piece')) return 'https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=400&q=80';
  if (t.includes('berserk')) return 'https://images.unsplash.com/photo-1618336753974-aae8e04506aa?auto=format&fit=crop&w=400&q=80';
  if (t.includes('atomic')) return 'https://images.unsplash.com/photo-1484480974693-2ca0a72f3a4b?auto=format&fit=crop&w=400&q=80';
  if (t.includes('hades')) return 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=400&q=80';
  if (t.includes('hollow')) return 'https://images.unsplash.com/photo-1508739773434-c26b3d09e071?auto=format&fit=crop&w=400&q=80';
  if (t.includes('sapiens')) return 'https://images.unsplash.com/photo-1447069387593-a5de0862481e?auto=format&fit=crop&w=400&q=80';
  if (t.includes('vagabond')) return 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=400&q=80';
  if (t.includes('jujutsu')) return 'https://images.unsplash.com/photo-1501854140801-50d01698950b?auto=format&fit=crop&w=400&q=80';
  if (t.includes('demon')) return 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=400&q=80';

  if (type === 'game') return 'https://images.unsplash.com/photo-1538481199705-c710c4e965fc?auto=format&fit=crop&w=400&q=80';
  if (type === 'movie') return 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=400&q=80';
  if (type === 'anime') return 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=400&q=80';
  if (type === 'book' || type === 'manga') return 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=400&q=80';
  if (type === 'music') return 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?auto=format&fit=crop&w=400&q=80';
  return 'https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=400&q=80';
}

function getStatusFromType(type: string): string {
  if (type === 'game') return 'playing';
  if (type === 'book' || type === 'manga') return 'reading';
  if (type === 'anime' || type === 'movie') return 'watching';
  return 'listening';
}

function getRelativeTimeString(timeMs: number): string {
  const diff = Date.now() - timeMs;
  const mins = Math.floor(diff / 60000);
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days === 1) return 'Yesterday';
  return `${days} days ago`;
}

// Exact mockup database items to merge with real telemetry logs
const MOCK_CONTINUE_PLAYING = [
  {
    id: 'elden-ring',
    title: 'Elden Ring',
    subtitle: 'FromSoftware',
    imageUrl: 'https://images.unsplash.com/photo-1655821888788-6107699e173b?auto=format&fit=crop&w=400&q=80',
    progress: 65,
    type: 'game',
    status: 'playing',
    latestTime: Date.now() - 3600000 * 2
  },
  {
    id: 'baldurs-gate-3',
    title: "Baldur's Gate 3",
    subtitle: 'Larian Studios',
    imageUrl: 'https://images.unsplash.com/photo-1519074069444-1ba4e6663104?auto=format&fit=crop&w=400&q=80',
    progress: 40,
    type: 'game',
    status: 'playing',
    latestTime: Date.now() - 3600000 * 5
  },
  {
    id: 'dune-part-two',
    title: 'Dune: Part Two',
    subtitle: '2024 • Sci-Fi',
    imageUrl: 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?auto=format&fit=crop&w=400&q=80',
    progress: 72,
    type: 'movie',
    status: 'watching',
    latestTime: Date.now() - 3600000 * 24
  },
  {
    id: 'chainsaw-man',
    title: 'Chainsaw Man',
    subtitle: 'MAPPA',
    imageUrl: 'https://images.unsplash.com/photo-1578632767115-351597cf2477?auto=format&fit=crop&w=400&q=80',
    progress: 28,
    type: 'anime',
    status: 'watching',
    latestTime: Date.now() - 3600000 * 30
  }
];

const MOCK_LIBRARY = [
  {
    id: 'interstellar',
    title: 'Interstellar',
    subtitle: '2014 • Sci-Fi',
    imageUrl: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=400&q=80',
    rating: 4.7,
    status: 'watching',
    type: 'movie',
    latestTime: Date.now() - 3600000 * 10
  },
  {
    id: 'the-dark-knight',
    title: 'The Dark Knight',
    subtitle: '2008 • Action',
    imageUrl: 'https://images.unsplash.com/photo-1509248961158-e54f6934749c?auto=format&fit=crop&w=400&q=80',
    rating: 4.8,
    status: 'watching',
    type: 'movie',
    latestTime: Date.now() - 3600000 * 15
  },
  {
    id: 'god-of-war',
    title: 'God of War',
    subtitle: 'Santa Monica Studio',
    imageUrl: 'https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=400&q=80',
    rating: 4.9,
    status: 'playing',
    type: 'game',
    latestTime: Date.now() - 3600000 * 20
  },
  {
    id: 'one-piece',
    title: 'One Piece',
    subtitle: 'Eiichiro Oda',
    imageUrl: 'https://images.unsplash.com/photo-1505118380757-91f5f5632de0?auto=format&fit=crop&w=400&q=80',
    rating: 4.8,
    status: 'reading',
    type: 'manga',
    latestTime: Date.now() - 3600000 * 25
  },
  {
    id: 'berserk',
    title: 'Berserk',
    subtitle: 'Kentaro Miura',
    imageUrl: 'https://images.unsplash.com/photo-1618336753974-aae8e04506aa?auto=format&fit=crop&w=400&q=80',
    rating: 4.9,
    status: 'reading',
    type: 'manga',
    latestTime: Date.now() - 3600000 * 35
  },
  {
    id: 'atomic-habits',
    title: 'Atomic Habits',
    subtitle: 'James Clear',
    imageUrl: 'https://images.unsplash.com/photo-1484480974693-2ca0a72f3a4b?auto=format&fit=crop&w=400&q=80',
    rating: 4.5,
    status: 'reading',
    type: 'book',
    latestTime: Date.now() - 3600000 * 40
  },
  {
    id: 'hades',
    title: 'Hades',
    subtitle: 'Supergiant',
    imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=400&q=80',
    rating: 4.7,
    status: 'playing',
    type: 'game',
    latestTime: Date.now() - 3600000 * 45
  },
  {
    id: 'hollow-knight',
    title: 'Hollow Knight',
    subtitle: 'Team Cherry',
    imageUrl: 'https://images.unsplash.com/photo-1508739773434-c26b3d09e071?auto=format&fit=crop&w=400&q=80',
    rating: 4.6,
    status: 'playing',
    type: 'game',
    latestTime: Date.now() - 3600000 * 50
  },
  {
    id: 'sapiens',
    title: 'Sapiens',
    subtitle: 'Yuval Noah Harari',
    imageUrl: 'https://images.unsplash.com/photo-1447069387593-a5de0862481e?auto=format&fit=crop&w=400&q=80',
    rating: 4.5,
    status: 'reading',
    type: 'book',
    latestTime: Date.now() - 3600000 * 55
  },
  {
    id: 'vagabond',
    title: 'Vagabond',
    subtitle: 'Takehiko Inoue',
    imageUrl: 'https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=400&q=80',
    rating: 4.8,
    status: 'reading',
    type: 'manga',
    latestTime: Date.now() - 3600000 * 60
  },
  {
    id: 'jujutsu-kaisen',
    title: 'Jujutsu Kaisen',
    subtitle: 'MAPPA',
    imageUrl: 'https://images.unsplash.com/photo-1501854140801-50d01698950b?auto=format&fit=crop&w=400&q=80',
    rating: 4.7,
    status: 'watching',
    type: 'anime',
    latestTime: Date.now() - 3600000 * 65
  },
  {
    id: 'demon-slayer',
    title: 'Demon Slayer',
    subtitle: 'ufotable',
    imageUrl: 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=400&q=80',
    rating: 4.8,
    status: 'watching',
    type: 'anime',
    latestTime: Date.now() - 3600000 * 70
  }
];

@Component({
  selector: 'app-library',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    PageContainerComponent,
    MediaSearchItemComponent
  ],
  template: `
    <app-page-container>
      
      <div class="library-layout">
        
        <!-- MAIN CONTENT COLUMN -->
        <div class="library-main">
          
          <!-- Library Header -->
          <div class="library-header">
            <h1 class="library-title">Library</h1>
            <p class="library-subtitle">Your media universe</p>
          </div>

          <!-- SEARCH BAR CONTAINER -->
          <div class="search-container" #searchContainer>
            <div class="search-bar">
              <span class="material-symbols-rounded" style="margin-right: 14px; font-size: 24px; color: rgba(255,255,255,0.42);">search</span>
              <input 
                type="text" 
                [formControl]="searchControl"
                placeholder="Search for games, movies, anime, books and more..." 
                class="search-input" 
                (focus)="showResults = true"
              />
              @if (loading()) {
                <div class="search-pill__loader" style="margin-right: 14px;"></div>
              }
              <button class="search-pill__add-btn" style="background: transparent; border: none; color: rgba(255,255,255,0.6); cursor: pointer; display: flex; align-items: center;">
                <span class="material-symbols-rounded">add</span>
              </button>
            </div>

            <!-- Discovery Overlay -->
            @if (showResults && (searchResults().length > 0 || loading() || error())) {
              <div class="search-overlay">
                <div class="search-overlay__header">
                  <span class="search-overlay__title">Discovery Results</span>
                  <button class="search-overlay__close" (click)="showResults = false">
                    <span class="material-symbols-rounded">close</span>
                  </button>
                </div>
                <div class="search-overlay__content">
                  @if (loading()) {
                    <div class="search-skeleton">
                      @for (i of [1,2,3]; track i) {
                        <div class="skeleton-item">
                          <div class="skeleton-thumb"></div>
                          <div class="skeleton-info">
                            <div class="skeleton-line line-title"></div>
                            <div class="skeleton-line line-meta"></div>
                          </div>
                        </div>
                      }
                    </div>
                  } @else if (error()) {
                    <div class="search-empty">
                      <span class="material-symbols-rounded">error</span>
                      <p>Failed to fetch results. Check connection.</p>
                    </div>
                  } @else if (searchResults().length === 0) {
                    <div class="search-empty">
                      <span class="material-symbols-rounded">search_off</span>
                      <p>No external results found for "{{ searchControl.value }}"</p>
                    </div>
                  } @else {
                    <div class="search-list">
                      @for (item of searchResults(); track item.id) {
                        <app-media-search-item [item]="item" (select)="onExternalSelect($event)" />
                      }
                    </div>
                  }
                </div>
              </div>
            }
          </div>

          <!-- TABS (Centered) -->
          <div class="filter-row" style="justify-content: center;">
            @for (t of tabs; track t) {
              <button class="filter-chip" [class.active]="t === activeTab()" (click)="activeTab.set(t)">{{ t }}</button>
            }
          </div>

          <!-- CONTINUE PLAYING (Horizontal Scroll) -->
          @if (continuePlayingItems().length > 0 && activeTab() === 'All') {
            <section style="margin-bottom: 40px; position: relative;">
              <h3 class="section-title">Continue Playing</h3>
              <div class="continue-row">
                @for (item of continuePlayingItems(); track item.id) {
                  <div class="continue-card">
                    <div style="position: relative; overflow: hidden;">
                      <img class="continue-poster" [src]="item.imageUrl" [alt]="item.title" (error)="handleImageError($event)" loading="lazy" />
                      <span class="status-badge" [attr.data-status]="item.status" style="position: absolute; top: 12px; left: 12px; z-index: 2;">{{ item.status }}</span>
                    </div>
                    <div class="continue-content">
                      <h4 class="continue-title">{{ item.title }}</h4>
                      <p class="continue-subtitle">{{ item.subtitle }}</p>
                      <div class="progress-bar">
                        <div class="progress-fill" [style.width.%]="item.progress"></div>
                      </div>
                    </div>
                  </div>
                }
              </div>
            </section>
          }

          <!-- YOUR LIBRARY (Grid) -->
          <section style="position: relative;">
            <h3 class="section-title">Your Library</h3>
            <div class="library-grid">
              @for (item of libraryGridItems(); track item.id) {
                <div class="library-card">
                  <div style="position: relative; overflow: hidden;">
                    <img class="library-poster" [src]="item.imageUrl" [alt]="item.title" (error)="handleImageError($event)" loading="lazy" />
                    <span class="status-badge" [attr.data-status]="item.status" style="position: absolute; top: 12px; left: 12px; z-index: 2;">{{ item.status }}</span>
                    @if (item.rating) {
                      <span class="rating-badge" style="position: absolute; top: 12px; right: 12px; z-index: 2;">★ {{ item.rating }}</span>
                    }
                  </div>
                  <div class="library-card-content">
                    <h4 class="library-card-title">{{ item.title }}</h4>
                    <p class="library-card-subtitle">{{ item.subtitle }}</p>
                  </div>
                </div>
              }
            </div>
            @if (libraryGridItems().length === 0) {
              <div class="empty-state" style="text-align: center; color: rgba(255,255,255,0.4); padding: 40px; background: rgba(255,255,255,0.02); border-radius: 24px; border: 1px dashed rgba(255,255,255,0.05); margin-top: 24px;">No media logs found. Log activities to populate your library.</div>
            }
            @if (libraryGridItems().length > 0) {
              <div class="load-more-container" style="display: flex; justify-content: center; margin-top: 32px;">
                <button class="load-more-btn">
                  Load More <span class="material-symbols-rounded">expand_more</span>
                </button>
              </div>
            }
          </section>
        </div>

        <!-- RIGHT SIDEBAR COLUMN -->
        <div class="library-sidebar">
          
          <!-- ACTIVITY OVERVIEW -->
          <div class="sidebar-widget">
            <div class="sidebar-title" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
              <span>Activity Overview</span>
              <select class="widget-select" style="background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.08); color: white; border-radius: 8px; padding: 4px 8px; font-size: 12px; outline: none; cursor: pointer;">
                <option style="background: #0f172a;">This Week</option>
                <option style="background: #0f172a;">This Month</option>
              </select>
            </div>
            
            <div class="overview-list" style="display: flex; flex-direction: column; gap: 18px;">
              <div class="stat-item" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0;">
                <div>
                  <div class="stat-label">Total Time</div>
                  <div class="stat-value">{{ totalDurationFormatted() }}</div>
                </div>
                <svg class="overview-sparkline" viewBox="0 0 100 30" style="width: 80px; height: 30px; stroke: #a855f7; stroke-width: 2; fill: none;">
                  <path d="M0,25 Q15,5 30,20 T60,10 T90,22 T100,5"></path>
                </svg>
              </div>
              <div class="stat-item" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0;">
                <div>
                  <div class="stat-label">Sessions</div>
                  <div class="stat-value">{{ sessionsCount() }}</div>
                </div>
                <svg class="overview-sparkline" viewBox="0 0 100 30" style="width: 80px; height: 30px; stroke: #a855f7; stroke-width: 2; fill: none;">
                  <path d="M0,15 Q20,25 40,10 T80,20 T100,5"></path>
                </svg>
              </div>
              <div class="stat-item" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0;">
                <div>
                  <div class="stat-label">Completed</div>
                  <div class="stat-value">{{ completedCount() }}</div>
                </div>
                <svg class="overview-sparkline" viewBox="0 0 100 30" style="width: 80px; height: 30px; stroke: #a855f7; stroke-width: 2; fill: none;">
                  <path d="M0,20 Q25,5 50,22 T80,12 T100,25"></path>
                </svg>
              </div>
              <div class="stat-item" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0;">
                <div>
                  <div class="stat-label">Streak</div>
                  <div class="stat-value">{{ streakDays() }} days</div>
                </div>
                <svg class="overview-sparkline" viewBox="0 0 100 30" style="width: 80px; height: 30px; stroke: #a855f7; stroke-width: 2; fill: none;">
                  <path d="M0,22 Q30,12 60,25 T100,5"></path>
                </svg>
              </div>
            </div>
          </div>

          <!-- RECENT ACTIVITY -->
          <div class="sidebar-widget">
            <div class="sidebar-title">Recent Activity</div>
            <div class="recent-list" style="display: flex; flex-direction: column; gap: 14px;">
              @for (act of recentActivities(); track act.id) {
                <div class="recent-item" style="display: flex; align-items: center; gap: 12px;">
                  <img class="recent-thumb" [src]="act.imageUrl" [alt]="act.title" (error)="handleImageError($event)" style="width: 38px; height: 50px; object-fit: cover; border-radius: 8px; border: 1px solid rgba(255,255,255,0.05);" />
                  <div class="recent-info" style="flex: 1; display: flex; flex-direction: column; gap: 2px;">
                    <span class="recent-name" style="font-size: 13.5px; font-weight: 600; color: white;">{{ act.title }}</span>
                    <span class="recent-desc" style="font-size: 11px; color: rgba(255,255,255,0.5);">{{ act.desc }}</span>
                  </div>
                  <span class="recent-time" style="font-size: 10.5px; color: rgba(255,255,255,0.4);">{{ act.time }}</span>
                </div>
              }
              @if (recentActivities().length === 0) {
                <div class="recent-empty" style="text-align: center; color: rgba(255,255,255,0.4); padding: 12px; font-size: 13px;">No recent logs.</div>
              }
            </div>
          </div>

          <!-- QUICK STATS -->
          <div class="sidebar-widget">
            <div class="sidebar-title">Quick Stats</div>
            <div class="quick-grid" style="display: grid; grid-template-columns: 1fr 1fr; gap: 14px; margin-bottom: 20px;">
              <div class="quick-card" style="display: flex; align-items: center; gap: 10px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.05); padding: 12px; border-radius: 16px;">
                <span class="quick-icon-wrapper" style="font-size: 20px;">🎮</span>
                <div class="quick-info" style="display: flex; flex-direction: column; gap: 1px;">
                  <span class="quick-label" style="font-size: 11px; color: rgba(255,255,255,0.5);">Games</span>
                  <span class="quick-count" style="font-size: 16px; font-weight: 700;">{{ categoryCounts().games }}</span>
                </div>
              </div>
              <div class="quick-card" style="display: flex; align-items: center; gap: 10px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.05); padding: 12px; border-radius: 16px;">
                <span class="quick-icon-wrapper" style="font-size: 20px;">🎬</span>
                <div class="quick-info" style="display: flex; flex-direction: column; gap: 1px;">
                  <span class="quick-label" style="font-size: 11px; color: rgba(255,255,255,0.5);">Movies</span>
                  <span class="quick-count" style="font-size: 16px; font-weight: 700;">{{ categoryCounts().movies }}</span>
                </div>
              </div>
              <div class="quick-card" style="display: flex; align-items: center; gap: 10px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.05); padding: 12px; border-radius: 16px;">
                <span class="quick-icon-wrapper" style="font-size: 20px;">⛩️</span>
                <div class="quick-info" style="display: flex; flex-direction: column; gap: 1px;">
                  <span class="quick-label" style="font-size: 11px; color: rgba(255,255,255,0.5);">Anime</span>
                  <span class="quick-count" style="font-size: 16px; font-weight: 700;">{{ categoryCounts().anime }}</span>
                </div>
              </div>
              <div class="quick-card" style="display: flex; align-items: center; gap: 10px; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.05); padding: 12px; border-radius: 16px;">
                <span class="quick-icon-wrapper" style="font-size: 20px;">📚</span>
                <div class="quick-info" style="display: flex; flex-direction: column; gap: 1px;">
                  <span class="quick-label" style="font-size: 11px; color: rgba(255,255,255,0.5);">Books</span>
                  <span class="quick-count" style="font-size: 16px; font-weight: 700;">{{ categoryCounts().books }}</span>
                </div>
              </div>
            </div>
            
            <div class="quick-footer" style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid rgba(255,255,255,0.05); padding-top: 16px;">
              <div class="quick-footer__total" style="display: flex; flex-direction: column; gap: 2px;">
                <span class="total-label" style="font-size: 11px; color: rgba(255,255,255,0.5);">Total Media</span>
                <span class="total-val" style="font-size: 22px; font-weight: 800; color: white;">{{ uniqueMediaCount() }}</span>
              </div>
              <button class="total-action-btn" style="width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, rgba(168,85,247,0.8), rgba(139,92,246,0.8)); border: none; color: white; cursor: pointer; display: flex; align-items: center; justify-content: center; box-shadow: 0 0 12px rgba(168,85,247,0.4); transition: transform 0.2s ease;">
                <span class="material-symbols-rounded">cyclone</span>
              </button>
            </div>
          </div>

        </div>

      </div>
    </app-page-container>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background:
        linear-gradient(
          180deg,
          #040816 0%,
          #060b1f 45%,
          #070d24 100%
        );
      color: #ffffff;
      overflow: hidden;
    }

    .library-layout {
      position: relative;
      display: grid;
      grid-template-columns: minmax(0, 1fr) 340px;
      gap: 28px;
      padding: 28px;
      min-height: 100vh;
      overflow: hidden;
    }

    @media (max-width: 1200px) {
      .library-layout {
        grid-template-columns: 1fr;
      }
    }

    .library-main {
      position: relative;
      z-index: 2;
      min-width: 0;
      padding-right: 12px;
      padding-bottom: 12px;
    }

    .library-sidebar {
      position: relative;
      z-index: 2;
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .library-layout::before {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 650px;
      background: url('/nebula-bg.png') top center / cover no-repeat;
      opacity: 0.72;
      pointer-events: none;
      z-index: 0;
    }

    .library-layout::after {
      content: "";
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background:
        linear-gradient(
          180deg,
          transparent 0%,
          transparent 18%,
          rgba(4, 8, 22, 0.45) 32%,
          rgba(4, 8, 22, 0.82) 50%,
          rgba(4, 8, 22, 0.97) 65%,
          #040816 100%
        );
      pointer-events: none;
      z-index: 0;
    }

    .library-header {
      position: relative;
      margin-bottom: 28px;
      padding-top: 20px;
    }

    .library-title {
      font-size: 54px;
      font-weight: 800;
      letter-spacing: -2px;
      margin-bottom: 10px;
    }

    .library-subtitle {
      color: rgba(255,255,255,0.62);
      font-size: 17px;
    }

    .search-container {
      position: relative;
      margin-top: 38px;
      margin-bottom: 38px;
    }

    .search-container::before {
      content: "";
      position: absolute;
      inset: -70px;
      background:
        radial-gradient(
          circle,
          rgba(168, 85, 247, 0.18),
          transparent 72%
        );
      filter: blur(70px);
      z-index: -1;
    }

    .search-bar {
      height: 72px;
      border-radius: 999px;
      background:
        linear-gradient(
          180deg,
          rgba(255,255,255,0.05),
          rgba(255,255,255,0.02)
        );
      border: 1px solid rgba(255,255,255,0.08);
      backdrop-filter: blur(40px);
      display: flex;
      align-items: center;
      padding: 0 28px;
      box-shadow:
        0 10px 50px rgba(0,0,0,0.45),
        0 0 25px rgba(168,85,247,0.12);
      transition:
        transform 0.25s ease,
        border-color 0.25s ease,
        box-shadow 0.25s ease;
    }

    .search-bar:hover {
      transform: translateY(-2px);
      border-color: rgba(168,85,247,0.25);
      box-shadow:
        0 16px 60px rgba(0,0,0,0.55),
        0 0 35px rgba(168,85,247,0.18);
    }

    .search-input {
      flex: 1;
      background: transparent;
      border: none;
      outline: none;
      color: white;
      font-size: 16px;
    }

    .search-input::placeholder {
      color: rgba(255,255,255,0.42);
    }

    .search-pill__loader {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(168, 85, 247, 0.3);
      border-top-color: #a855f7;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin-right: 12px;
    }

    .search-pill__add-btn {
      width: 42px;
      height: 42px;
      border-radius: var(--radius-lg);
      background: rgba(255,255,255,0.04);
      border: 1px solid rgba(255,255,255,0.08);
      color: rgba(255, 255, 255, 0.85);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    }

    .search-pill__add-btn:hover {
      background: #a855f7;
      color: white;
      border-color: #a855f7;
      box-shadow: 0 0 12px rgba(168, 85, 247, 0.5);
    }

    @keyframes spin { to { transform: rotate(360deg); } }

    /* DISCOVERY OVERLAY */
    .search-overlay {
      position: absolute;
      top: calc(100% + 12px);
      left: 0;
      right: 0;
      background: rgba(10, 14, 30, 0.85);
      backdrop-filter: blur(30px);
      -webkit-backdrop-filter: blur(30px);
      border: 1px solid rgba(168, 85, 247, 0.3);
      border-radius: var(--radius-xl);
      box-shadow: 
        0 20px 50px rgba(0,0,0,0.6),
        0 0 30px rgba(168,85,247,0.15);
      z-index: 100;
      overflow: hidden;
      animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
    }

    @keyframes slideUp { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

    .search-overlay__header {
      padding: 12px 18px;
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: rgba(255,255,255,0.01);
    }

    .search-overlay__title { font-size: 10px; text-transform: uppercase; letter-spacing: 1px; color: rgba(255, 255, 255, 0.5); font-weight: 600; }
    .search-overlay__close { background: none; border: none; color: rgba(255, 255, 255, 0.5); cursor: pointer; display: flex; padding: 4px; border-radius: var(--radius-xs); }
    .search-overlay__close:hover { background: rgba(255,255,255,0.05); color: #ffffff; }
    .search-overlay__content { max-height: 350px; overflow-y: auto; padding: 6px; }

    .search-list { display: flex; flex-direction: column; gap: 4px; }
    .search-empty { padding: 30px 15px; text-align: center; color: rgba(255,255,255,0.5); font-size: 13px; }
    .search-empty span { font-size: 32px; margin-bottom: 8px; opacity: 0.3; }

    .skeleton-item { display: flex; align-items: center; gap: 12px; padding: 8px 12px; }
    .skeleton-thumb { width: 36px; height: 50px; background: rgba(255,255,255,0.04); border-radius: var(--radius-xs); }
    .skeleton-info { flex: 1; display: flex; flex-direction: column; gap: 6px; }
    .skeleton-line { height: 10px; background: rgba(255,255,255,0.04); border-radius: var(--radius-full); }
    .line-title { width: 50%; }
    .line-meta { width: 25%; }

    .filter-row {
      display: flex;
      gap: 14px;
      margin-bottom: 38px;
    }

    .filter-chip {
      height: 42px;
      padding: 0 22px;
      border-radius: 999px;
      background: rgba(255,255,255,0.03);
      border: 1px solid rgba(255,255,255,0.05);
      color: rgba(255,255,255,0.72);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: all 0.25s ease;
    }

    .filter-chip:hover {
      transform: translateY(-2px);
      background: rgba(255,255,255,0.06);
    }

    .filter-chip.active {
      color: white;
      background:
        linear-gradient(
          180deg,
          rgba(168,85,247,0.92),
          rgba(139,92,246,0.82)
        );
      border-color: transparent;
      box-shadow:
        0 0 24px rgba(168,85,247,0.42);
    }

    .section-title {
      font-size: 26px;
      font-weight: 700;
      margin-bottom: 22px;
    }

    .continue-row {
      display: flex;
      gap: 24px;
      overflow-x: auto;
      margin-bottom: 50px;
    }

    .continue-card {
      flex-shrink: 0;
      width: 300px;
      border-radius: 26px;
      overflow: hidden;
      background:
        linear-gradient(
          180deg,
          rgba(255,255,255,0.04),
          rgba(255,255,255,0.015)
        );
      border: 1px solid rgba(255,255,255,0.05);
      backdrop-filter: blur(40px);
      transition:
        transform 0.28s ease,
        box-shadow 0.28s ease;
    }

    .continue-card:hover {
      transform: translateY(-6px) scale(1.02);
      box-shadow:
        0 25px 60px rgba(0,0,0,0.55),
        0 0 35px rgba(168,85,247,0.18);
    }

    .continue-poster {
      width: 100%;
      aspect-ratio: 16/9;
      object-fit: cover;
    }

    .continue-content {
      padding: 18px;
    }

    .continue-title {
      font-size: 20px;
      font-weight: 700;
      margin-bottom: 8px;
    }

    .continue-subtitle {
      color: rgba(255,255,255,0.58);
      margin-bottom: 16px;
    }

    .progress-bar {
      height: 6px;
      border-radius: 999px;
      background: rgba(255,255,255,0.08);
      overflow: hidden;
    }

    .progress-fill {
      height: 100%;
      border-radius: inherit;
      background:
        linear-gradient(
          90deg,
          #8b5cf6,
          #a855f7
        );
    }

    .status-badge {
      position: absolute;
      top: 10px;
      left: 10px;
      font-size: 9px;
      font-weight: 700;
      text-transform: uppercase;
      padding: 3px 8px;
      border-radius: 6px;
      color: #ffffff;
      letter-spacing: 0.5px;
      box-shadow: 0 4px 10px rgba(0,0,0,0.3);
      border: 1px solid rgba(255, 255, 255, 0.1);
      backdrop-filter: blur(4px);
    }

    .status-badge[data-status="playing"],
    .status-badge[data-status="PLAYING"] {
      background: rgba(139, 92, 246, 0.7);
      border-color: rgba(139, 92, 246, 0.5);
    }

    .status-badge[data-status="watching"],
    .status-badge[data-status="WATCHING"] {
      background: rgba(59, 130, 246, 0.7);
      border-color: rgba(59, 130, 246, 0.5);
    }

    .status-badge[data-status="reading"],
    .status-badge[data-status="READING"] {
      background: rgba(16, 185, 129, 0.7);
      border-color: rgba(16, 185, 129, 0.5);
    }

    .status-badge[data-status="completed"],
    .status-badge[data-status="COMPLETED"] {
      background: rgba(245, 158, 11, 0.7);
      border-color: rgba(245, 158, 11, 0.5);
    }

    .status-badge[data-status="wishlist"],
    .status-badge[data-status="WISHLIST"] {
      background: rgba(107, 114, 128, 0.7);
      border-color: rgba(107, 114, 128, 0.5);
    }

    .rating-badge {
      position: absolute;
      top: 10px;
      right: 10px;
      font-size: 9px;
      font-weight: 700;
      background: rgba(10, 14, 30, 0.6);
      backdrop-filter: blur(4px);
      -webkit-backdrop-filter: blur(4px);
      color: #f59e0b;
      padding: 3px 8px;
      border-radius: 6px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      box-shadow: 0 4px 10px rgba(0,0,0,0.3);
    }

    .library-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
      gap: 24px;
    }

    .library-card {
      border-radius: 24px;
      overflow: hidden;
      background:
        linear-gradient(
          180deg,
          rgba(255,255,255,0.04),
          rgba(255,255,255,0.015)
        );
      border: 1px solid rgba(255,255,255,0.05);
      backdrop-filter: blur(40px);
      transition:
        transform 0.28s ease,
        box-shadow 0.28s ease;
    }

    .library-card:hover {
      transform: translateY(-6px) scale(1.02);
      box-shadow:
        0 25px 60px rgba(0,0,0,0.55),
        0 0 35px rgba(168,85,247,0.18);
    }

    .library-poster {
      width: 100%;
      aspect-ratio: 2/3;
      object-fit: cover;
    }

    .library-card-content {
      padding: 16px;
    }

    .library-card-title {
      font-size: 17px;
      font-weight: 700;
      margin-bottom: 6px;
    }

    .library-card-subtitle {
      color: rgba(255,255,255,0.55);
    }

    .sidebar-widget {
      border-radius: 28px;
      padding: 22px;
      background:
        linear-gradient(
          180deg,
          rgba(255,255,255,0.04),
          rgba(255,255,255,0.015)
        );
      border: 1px solid rgba(255,255,255,0.05);
      backdrop-filter: blur(50px);
      box-shadow:
        0 12px 45px rgba(0,0,0,0.38);
    }

    .sidebar-title {
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 24px;
    }

    .stat-item {
      margin-bottom: 22px;
    }

    .stat-label {
      color: rgba(255,255,255,0.58);
      margin-bottom: 6px;
    }

    .stat-value {
      font-size: 30px;
      font-weight: 800;
    }

    ::-webkit-scrollbar {
      width: 8px;
      height: 8px;
    }

    ::-webkit-scrollbar-thumb {
      background: rgba(168,85,247,0.35);
      border-radius: 999px;
    }
  `],
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
