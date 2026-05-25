import { Component, inject, OnInit } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { AppCardComponent } from '../../shared/components/app-card.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { ProgressBarComponent } from '../../shared/components/progress-bar.component';
import { AchievementsApiService } from '../../core/api/services/achievements-api.service';
import { Achievement } from '../../shared/models/activity.model';

@Component({
  selector: 'app-achievements',
  standalone: true,
  imports: [PageContainerComponent, AppCardComponent, SectionHeaderComponent, ProgressBarComponent],
  template: `
    <app-page-container>
      <app-section-header title="Achievements" subtitle="Your milestones" />
      <div class="ach-stats">
        <div class="ach-stat"><span class="ach-stat__val">{{ unlocked }}</span><span class="ach-stat__label">Unlocked</span></div>
        <div class="ach-stat"><span class="ach-stat__val">{{ total }}</span><span class="ach-stat__label">Total</span></div>
        <div class="ach-stat"><span class="ach-stat__val">{{ pct }}%</span><span class="ach-stat__label">Completion</span></div>
      </div>
      <div class="ach-grid">
        @for (a of achievements; track a.id) {
          <app-card [hoverable]="true" padding="compact">
            <div class="ach-item" [class.ach-item--locked]="!a.unlocked">
              <span class="ach-icon">{{ a.icon }}</span>
              <div class="ach-info">
                <h4 class="ach-name">{{ a.title }}</h4>
                <p class="ach-desc">{{ a.description }}</p>
                @if (!a.unlocked && a.maxProgress) {
                  <app-progress-bar [value]="(a.progress||0)/(a.maxProgress)*100" />
                  <span class="ach-prog">{{ a.progress }}/{{ a.maxProgress }}</span>
                }
                @if (a.unlocked) { <span class="ach-unlocked">✓ Unlocked {{ a.unlockedAt }}</span> }
              </div>
            </div>
          </app-card>
        }
        @if (achievements.length === 0) {
          <div class="empty-state">No achievements recorded yet.</div>
        }
      </div>
    </app-page-container>
  `,
  styles: [`
    .ach-stats { display: flex; gap: 24px; margin-bottom: 24px; padding: 16px 24px; background: var(--card-bg); border: 1px solid var(--card-border); border-radius: var(--radius-lg); }
    .ach-stat { text-align: center; }
    .ach-stat__val { display: block; font-size: 24px; font-weight: 700; color: var(--accent-primary); }
    .ach-stat__label { font-size: 12px; color: var(--text-secondary); }
    .ach-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 12px; }
    .ach-item { display: flex; gap: 14px; align-items: flex-start; }
    .ach-item--locked { opacity: 0.5; }
    .ach-icon { font-size: 32px; flex-shrink: 0; }
    .ach-info { flex: 1; min-width: 0; }
    .ach-name { font-size: 14px; font-weight: 600; color: var(--text-primary); }
    .ach-desc { font-size: 12px; color: var(--text-secondary); margin: 2px 0 6px; }
    .ach-prog { font-size: 11px; color: var(--text-tertiary); margin-top: 4px; display: block; }
    .ach-unlocked { font-size: 11px; color: var(--accent-emerald); font-weight: 500; }
    .empty-state { text-align: center; padding: 40px; color: var(--text-secondary); font-size: 14px; grid-column: 1 / -1; }
  `],
})
export class AchievementsComponent implements OnInit {
  private achievementsApi = inject(AchievementsApiService);

  achievements: Achievement[] = [];

  ngOnInit() {
    this.achievementsApi.getAchievements().subscribe({
      next: (data) => this.achievements = data,
      error: () => this.achievements = []
    });
  }

  get unlocked() { return this.achievements.filter(a => a.unlocked).length; }
  get total() { return this.achievements.length; }
  get pct() { return this.total > 0 ? Math.round((this.unlocked / this.total) * 100) : 0; }
}
