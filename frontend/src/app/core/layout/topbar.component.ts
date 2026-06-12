import { Component, inject, output } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter, map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ThemeToggleComponent } from '../../shared/components/theme-toggle.component';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [ThemeToggleComponent],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.css',
})
export class TopbarComponent {
  menuToggle = output<void>();
  private router = inject(Router);
  pageTitle = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(e => {
        const segment = e.urlAfterRedirects.split('/')[1] || 'dashboard';
        return segment.charAt(0).toUpperCase() + segment.slice(1);
      })
    ),
    { initialValue: 'Dashboard' }
  );
}
