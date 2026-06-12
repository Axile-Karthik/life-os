import { Component, inject } from '@angular/core';
import { PageContainerComponent } from '../../shared/components/page-container.component';
import { AppCardComponent } from '../../shared/components/app-card.component';
import { SectionHeaderComponent } from '../../shared/components/section-header.component';
import { ThemeService } from '../../core/theme/theme.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [PageContainerComponent, AppCardComponent, SectionHeaderComponent],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css',
})
export class SettingsComponent {
  themeService = inject(ThemeService);
}
