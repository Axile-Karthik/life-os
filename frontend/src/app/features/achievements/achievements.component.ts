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
  templateUrl: './achievements.component.html',
  styleUrl: './achievements.component.css',
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
